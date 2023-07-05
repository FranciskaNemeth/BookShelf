package com.example.bookshelf.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.bookshelf.R
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.interfaces.GetGenreInterface
import com.example.bookshelf.interfaces.UpdateBookDataInterface
import com.example.bookshelf.interfaces.UploadBookCoverImageInterface
import com.example.bookshelf.model.Book
import com.example.bookshelf.model.CameraRequest
import com.example.bookshelf.model.Images
import com.example.bookshelf.utils.Utils
import com.example.bookshelf.viewmodel.BoundingBoxViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class AddFragment : Fragment() {
    lateinit var imageView : ImageView
    lateinit var textInputEditTextAuthor : TextInputEditText
    lateinit var textInputEditTextTitle: TextInputEditText
    lateinit var spinner: Spinner
    lateinit var textInputEditTextDescription: TextInputEditText
    lateinit var buttonSave : Button
    lateinit var takePhotoButton: ImageButton
    lateinit var takePhotoDesc: ImageButton
    lateinit var borrowedCheckBox : CheckBox
    lateinit var textInputEditTextBorrowed: TextInputEditText
    lateinit var textInputEditTextShelf: TextInputEditText
    lateinit var textInputEditTextRow: TextInputEditText


    private lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser

    var genres : MutableList<String> = ArrayList()
    lateinit var bookGenre: String

    lateinit var book : Book

    val storage = Firebase.storage
    var imgURL : String? = null
    var coverImageHasChanged: Boolean = false

    lateinit var imageBitmap : Bitmap

    lateinit var viewModel: BoundingBoxViewModel

    lateinit var loadingLayout : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        viewModel = ViewModelProvider(requireActivity()).get(BoundingBoxViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.add_screen, container, false)

        imageView = view.findViewById(R.id.imageView)
        textInputEditTextAuthor = view.findViewById(R.id.textInputEditTextAuthor)
        textInputEditTextTitle = view.findViewById(R.id.textInputEditTextTitle)
        textInputEditTextDescription = view.findViewById(R.id.textInputEditTextDescription)
        takePhotoButton =  view.findViewById(R.id.imageButton)
        takePhotoDesc = view.findViewById(R.id.imageButtonDesc)
        buttonSave = view.findViewById(R.id.buttonSave)
        spinner = view.findViewById(R.id.spinner)
        loadingLayout = view.findViewById(R.id.loading_layout)
        textInputEditTextBorrowed = view.findViewById(R.id.textInputEditTextBorrowed)
        borrowedCheckBox = view.findViewById(R.id.borrowedCheckBox)
        textInputEditTextShelf = view.findViewById(R.id.textInputEditTextShelf)
        textInputEditTextRow = view.findViewById(R.id.textInputEditTextRow)

        textInputEditTextShelf.text = null
        textInputEditTextRow.text = null

        hideLoading()

        viewModel.description.observe(viewLifecycleOwner) {
            if (it != null) {
                textInputEditTextDescription.setText(it)
            }
        }

        viewModel.result.observe(viewLifecycleOwner) {
            if (it != null) {
                textInputEditTextTitle.setText(it.title)
                textInputEditTextAuthor.setText(it.author)
            }
        }

        viewModel.images.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.CAMERA_REQUEST == CameraRequest.COVER && it.imageCover != null) {
                    imageBitmap = it.imageCover!!
                    coverImageHasChanged = true
                }

                if (this::imageBitmap.isInitialized) {
                    imageView.setImageBitmap(imageBitmap)
                }
            }
        }

        DatabaseManager.getGenresData(object : GetGenreInterface {
            override fun getGenre(genres: MutableList<String>) {
                this@AddFragment.genres = genres
                val spinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item, this@AddFragment.genres
                )

                spinner.adapter = spinnerAdapter

                val selectedBook = DatabaseManager.getSelectedBookData()

                if(selectedBook != null ) {
                    book = selectedBook
                    imgURL = book.imageURL
                    val ref = storage.reference.child("images/" + book.imageURL + ".jpg")
                    ref.downloadUrl.addOnSuccessListener { Uri ->
                        val imageUrl = Uri.toString()
                        Thread {
                            imageBitmap = Glide.with(requireActivity())
                                .asBitmap()
                                .load(imageUrl)
                                .submit()
                                .get()

                            view.post {
                                imageView.setImageBitmap(imageBitmap)
                            }
                        }.start()
                    }

                    val title = Utils.capitalizeFirstLetters(book.title)
                    val author = Utils.capitalizeFirstLetters(book.author)

                    textInputEditTextTitle.setText(title)
                    textInputEditTextAuthor.setText(author)
                    textInputEditTextDescription.setText(book.description)

                    spinner.post {
                        spinner.setSelection(genres.indexOf(book.genre))
                    }

                    borrowedCheckBox.isChecked = book.isBorrowed
                    textInputEditTextBorrowed.isEnabled = book.isBorrowed

                    if (book.isBorrowed) {

                        textInputEditTextBorrowed.setText(book.borrowedTo)
                    }
                    else {
                        textInputEditTextBorrowed.text?.clear()
                    }

                    textInputEditTextShelf.setText(book.shelf?.toString())
                    textInputEditTextRow.setText(book.row?.toString())
                }
                else {
                    borrowedCheckBox.isChecked = false
                    textInputEditTextBorrowed.isEnabled = false
                    textInputEditTextBorrowed.text?.clear()
                    textInputEditTextShelf.text = null
                    textInputEditTextRow.text = null
                }
            }
        })

        borrowedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            textInputEditTextBorrowed.isEnabled = isChecked

            if (!isChecked) {
                textInputEditTextBorrowed.text?.clear()
            }
        }

        takePhotoButton.setOnClickListener {
            viewModel.images.value = Images(null, null, CameraRequest.COVER)
            view?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_addFragment_to_imageCaptureFragment) }
        }

        takePhotoDesc.setOnClickListener {
            viewModel.images.value = Images(null, null, CameraRequest.DESCRIPTION)
            view?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_addFragment_to_imageCaptureFragment) }

        }

        imageView.setOnClickListener {
            showPicture()
        }

        buttonSave.setOnClickListener {
            showLoading()

            bookGenre = spinner.selectedItem.toString()

            if (textInputEditTextTitle.text.toString().isNullOrEmpty() ||
                textInputEditTextAuthor.text.toString().isNullOrEmpty() ||
                bookGenre.isNullOrEmpty() ||
                textInputEditTextDescription.text.toString().isNullOrEmpty() ||
                (borrowedCheckBox.isChecked == true && textInputEditTextBorrowed.text.toString().isNullOrEmpty())) {
                hideLoading()

                val message = "One or more fields are empty. Check again, and fill out every field!"
                AlertDialogFragment().addBookErrorHandling(message,requireContext())

                return@setOnClickListener
            }

            if (coverImageHasChanged && !this::imageBitmap.isInitialized) {
                hideLoading()

                val message = "Image not found! Take another picture."
                AlertDialogFragment().addBookErrorHandling(message,requireContext())

                return@setOnClickListener
            }

            val shelf : Long?
            val row : Long?

            if(textInputEditTextShelf.text.isNullOrEmpty()) {
                shelf = null
            }
            else {
                shelf = textInputEditTextShelf.text?.toString()?.toLong()
            }

            if (textInputEditTextRow.text.isNullOrEmpty()) {
                row = null
            }
            else {
                row = textInputEditTextRow.text?.toString()?.toLong()
            }

            // if the book is initialized it means that we are updating
            if (this::book.isInitialized) {
                book.title = textInputEditTextTitle.text.toString()
                book.author = textInputEditTextAuthor.text.toString()
                book.genre = bookGenre
                book.description = textInputEditTextDescription.text.toString()
                // isFav is not modified here
                book.isBorrowed = borrowedCheckBox.isChecked
                book.borrowedTo = textInputEditTextBorrowed.text.toString()
                book.shelf = shelf
                book.row = row

            }
            else {
                book = Book(
                     "",  // empty string is set for the time being; will update once image is uploaded
                     textInputEditTextTitle.text.toString(),
                     textInputEditTextAuthor.text.toString(),
                     bookGenre,
                     textInputEditTextDescription.text.toString(),
                     false,
                     borrowedCheckBox.isChecked,
                     textInputEditTextBorrowed.text.toString(),
                     shelf,
                     row
                )
            }

            if (coverImageHasChanged) {
                DatabaseManager.uploadBookCoverImage(imageBitmap, imgURL, object: UploadBookCoverImageInterface {
                    override fun onSuccess(imageUrl: String) {
                        book.imageURL = imageUrl

                        DatabaseManager.updateBookData(currentUser.email!!, book, object: UpdateBookDataInterface {
                            override fun onSuccess() {
                                Toast.makeText(requireActivity(), "Upload succeeded!",
                                    Toast.LENGTH_LONG).show()

                                view?.let { it1 ->
                                    Navigation.findNavController(it1).popBackStack(R.id.mainFragment, false)
                                }
                            }

                            override fun onError(reason: String?) {
                                hideLoading()

                                val message = "$reason"
                                AlertDialogFragment().addBookErrorHandling(message,requireContext())
                            }

                        })
                    }

                    override fun onError(reason: String?) {
                        hideLoading()

                        val message = "$reason"
                        AlertDialogFragment().addBookErrorHandling(message,requireContext())
                    }

                })
            }
            else {
                DatabaseManager.updateBookData(currentUser.email!!, book, object: UpdateBookDataInterface {
                    override fun onSuccess() {
                        Toast.makeText(requireActivity(), "Upload succeeded!",
                            Toast.LENGTH_LONG).show()

                        view?.let { it1 ->
                            Navigation.findNavController(it1).popBackStack(R.id.mainFragment, false)
                        }
                    }

                    override fun onError(reason: String?) {
                        hideLoading()

                        val message = "$reason"
                        AlertDialogFragment().addBookErrorHandling(message,requireContext())
                    }

                })
            }

        }

        return view
    }
    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            hideLoading()

            val message = "Something went wrong! Please check your internet connection or try again later!"
            AlertDialogFragment().errorHandling(message, requireContext())
        }

        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DatabaseManager.deleteSelectedBookData()
        clearViewModel()

        Log.d("DELETE", "delete selected book")
    }

    private fun showPicture() {
        val alertAdd = AlertDialog.Builder(requireContext())
        val factory = LayoutInflater.from(requireContext())
        val view: View = factory.inflate(R.layout.image_dialog, null)
        val img = view.findViewById<ImageView>(R.id.imageDialog)

        if(this::book.isInitialized) {
            val ref = storage.reference.child("images/" + book.imageURL + ".jpg")
            ref.downloadUrl.addOnSuccessListener { Uri ->
                val imageUrl = Uri.toString()
                Glide.with(requireActivity())
                    .load(imageUrl)
                    .placeholder(R.drawable.logo)
                    .into(img)
            }
        }
        else if (this::imageBitmap.isInitialized){
            Glide.with(requireActivity())
                .load(imageBitmap)
                .placeholder(R.drawable.logo)
                .into(img)
        }

        alertAdd.setView(view)
        alertAdd.setNeutralButton("Close") { _, _ ->

        }

        alertAdd.show()
    }

    private fun clearViewModel() {
        viewModel.images.removeObservers(viewLifecycleOwner)
        viewModel.result.removeObservers(viewLifecycleOwner)
        viewModel.description.removeObservers(viewLifecycleOwner)

        viewModel.images.value = null
        viewModel.result.value = null
        viewModel.description.value = null

    }

    private fun showLoading() {
        if (this::loadingLayout.isInitialized) {
            enableFields(false)
            loadingLayout.setVisibility(View.VISIBLE)
        }
    }

    private fun hideLoading() {
        if (this::loadingLayout.isInitialized) {
            enableFields(true)
            loadingLayout.setVisibility(View.GONE)
        }
    }

    private fun enableFields(enable : Boolean) {
        imageView.isClickable = enable
        textInputEditTextTitle.isEnabled = enable
        textInputEditTextAuthor.isEnabled = enable
        spinner.isEnabled= enable
        textInputEditTextDescription.isEnabled = enable
        takePhotoButton.isClickable = enable
        takePhotoDesc.isClickable = enable
        buttonSave.isClickable = enable
        borrowedCheckBox.isEnabled = enable
        textInputEditTextBorrowed.isEnabled = enable
        textInputEditTextShelf.isEnabled = enable
        textInputEditTextRow.isEnabled = enable
    }

}