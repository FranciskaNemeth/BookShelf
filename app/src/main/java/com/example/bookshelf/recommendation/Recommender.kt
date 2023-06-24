package com.example.bookshelf.recommendation

import android.util.Log
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.bookshelf.api.RetrofitInstance
import com.example.bookshelf.interfaces.GetRecommendedBooksInterface
import com.example.bookshelf.model.Book
import com.example.bookshelf.model.BooksApiBookData
import com.example.bookshelf.model.BooksApiResponse
import com.example.bookshelf.model.RecommendedBook
import com.google.gson.Gson
import kotlin.time.Duration.Companion.seconds

object Recommender {

    var openAIClient : OpenAI

    init {
        openAIClient = OpenAI(
            token = "sk-<token>",
            timeout = Timeout(socket = 15.seconds),
            organization = "org-<org>"
        )
    }

    suspend fun getRecommendationFor(favoriteBooks: List<Book>,
                                            callback: GetRecommendedBooksInterface? = null) {

        if (favoriteBooks.isNullOrEmpty()) {
            callback?.onError("Favorite books list is empty!")
            return
        }

        val genresAndTitlesAndAuthors : String? = getGenreAndTitleAndAuthorStringForOpenAI(favoriteBooks)
        if (genresAndTitlesAndAuthors.isNullOrEmpty()) {
            callback?.onError("Couldn't create title and author string!")
            return
        }

        val requestString: String = createRequestString(genresAndTitlesAndAuthors)

        val (str, isSuccessful) = sendRequest(requestString)
        if (!isSuccessful) {
            callback?.onError(str)
            return
        }

        val recommendedBooks = processOpenAIResponse(str!!)

        if (recommendedBooks == null) {
            callback?.onError("Couldn't extract recommended book data from OpenAI response!")
            return
        }

        val books: List<Book> = getBookDataFor(recommendedBooks.asList())

        for (book in books) {
            Log.d("Muki", "${book.author} - ${book.title} - ${book.imageURL}")
        }

        callback?.onSuccess(books)

    }

    private fun getGenreAndTitleAndAuthorStringForOpenAI(books: List<Book>) : String? {
        var resultString = ""

        val selectedBooks: List<Book> = if (books.size > 10) {
            books.asSequence().shuffled().take(10).toList()
        } else {
            books
        }

        for (book in selectedBooks) {
            resultString += "${book.genre}: \"${book.title}\" by ${book.author}"
            resultString += ", "
        }

        // remove the last comma and replace it with a period
        if (resultString.isNotEmpty()) {
            resultString = resultString.substring(0, resultString.lastIndex - 2) + "."
            return resultString
        }

        return null
    }

    private fun createRequestString(genreAndTitlesAndAuthors: String) : String {
        return "Give 10 book suggestions, based on the following books and their genres:  " +
                "$genreAndTitlesAndAuthors " +
                "The suggested books should be in JSON format containing the title, author and genre."
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun sendRequest(requestString: String): Pair<String?, Boolean> {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            n = 1,
            temperature = 0.2,
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a helpful librarian that suggests both English and Hungarian books."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = requestString
                )
            )
        )
        val choices: List<ChatChoice>
        try {
            choices = openAIClient.chatCompletion(chatCompletionRequest).choices
        }
        catch (e: Exception) {
            return Pair(e.message, false)
        }

        val message: ChatMessage?
        if (choices.isNotEmpty()) {
            message = choices[0].message
        }
        else {
            return Pair("Couldn't extract message from OpenAI response!", false)
        }

        val content: String?
        if (message != null) {
            content = message.content
        }
        else {
            return Pair("Couldn't extract content from OpenAI response!", false)
        }

        return Pair(content, true)
    }

    private fun processOpenAIResponse(response: String) : Array<RecommendedBook>? {
        val regex = """[{\[]{1}([,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]|".*?")+[}\]]{1}""".toRegex()
        val matchResult = regex.find(response)

        val jsonString: String

        if (matchResult != null) {
            jsonString = matchResult.value
        }
        else {
            Log.e("ERROR", "Regex doesn't match JSON in the following response:\n $response")
            return null
        }

        val gson = Gson()
        return try {
            gson.fromJson(jsonString, Array<RecommendedBook>::class.java)
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
            null
        }
    }

    private suspend fun getBookDataFor(recommendedBooks: List<RecommendedBook>) : List<Book> {
        val bookData : MutableList<Book> = ArrayList()

        for (recommendedBook in recommendedBooks) {
            val queryString = createQueryString(recommendedBook)

            val book = getBook(queryString, recommendedBook,"<google_api_key>")

            if (book != null) {
                bookData.add(book)
            }

        }

        return bookData
    }

    private fun createQueryString(recommendedBook: RecommendedBook): String {

        val modifiedAuthor = recommendedBook.author.replace(" ", "+")
        val modifiedTitle = recommendedBook.title.replace(" ", "+")

        return "intitle:{$modifiedTitle}+inauthor:{$modifiedAuthor}"
    }

    private suspend fun getBook(queryString: String,
                                recommendedBook: RecommendedBook,
                                key: String): Book? {
        lateinit var booksApiResponse: BooksApiResponse

        try {
            booksApiResponse = RetrofitInstance.api.getBooks(queryString, key)
        }
        catch (e : Exception) {
            Log.e("ERROR", e.message.toString())
            return null
        }

        val bookData : BooksApiBookData
        if (booksApiResponse.totalItems > 0) {
            bookData = booksApiResponse.items!![0]
        }
        else {
            Log.w("WARNING", "No candidates for query: $queryString")
            return null
        }

        val volumeInfo = bookData.volumeInfo

        val title = volumeInfo.title
        val author = volumeInfo.authors.joinToString(", ")
        val description = volumeInfo.description
        val genre: String
        if (volumeInfo.categories != null) {
            genre = volumeInfo.categories.joinToString(", ")
        }
        else {
            genre = recommendedBook.genre
            Log.d("Muki", "Category not received for book: ${recommendedBook.title}")
        }

        val imageUrl: String
        if (volumeInfo.imageLinks != null) {
            imageUrl = volumeInfo.imageLinks.thumbnail
        }
        else {
            imageUrl = ""
        }

        return Book(imageUrl, title, author, genre, description, false)
    }

}