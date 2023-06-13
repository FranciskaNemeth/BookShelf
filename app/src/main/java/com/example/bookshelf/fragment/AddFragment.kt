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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.bookshelf.R
import com.example.bookshelf.database.DatabaseManager
import com.example.bookshelf.interfaces.GetGenreInterface
import com.example.bookshelf.model.Book
import com.example.bookshelf.utils.Utils
import com.example.bookshelf.viewmodel.BoundingBoxViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream


class AddFragment : Fragment() {
    lateinit var imageView : ImageView
    lateinit var textInputEditTextAuthor : TextInputEditText
    lateinit var textInputEditTextTitle: TextInputEditText
    lateinit var spinner: Spinner
    lateinit var textInputEditTextDescription: TextInputEditText
    lateinit var buttonSave : Button


    private lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser

    var genres : MutableList<String> = ArrayList()
    lateinit var bookGenre: String

    lateinit var book : Book

    lateinit var dataB : ByteArray
    lateinit var mountainsRef : StorageReference
    var baos = ByteArrayOutputStream()

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val storage = Firebase.storage
    var imgURL : String? = null

    lateinit var imageBitmap : Bitmap

    lateinit var viewModel: BoundingBoxViewModel

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
        buttonSave = view.findViewById(R.id.buttonSave)
        spinner = view.findViewById(R.id.spinner)

        viewModel.description.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                textInputEditTextDescription.setText(it)
            }
        })

        viewModel.result.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                textInputEditTextTitle.setText(it.title)
                textInputEditTextAuthor.setText(it.author)
            }
        })

        viewModel.imageCover.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                imageBitmap = it
                imageView.setImageBitmap(imageBitmap)
            }
        })

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
                        val imageurl = Uri.toString()
                        Glide.with(requireActivity())
                            .load(imageurl)
                            .into(imageView)
                    }
                    val title = Utils.capitalizeFirstLetters(book.title)
                    val author = Utils.capitalizeFirstLetters(book.author)

                    textInputEditTextTitle.setText(title)
                    textInputEditTextAuthor.setText(author)
                    textInputEditTextDescription.setText(book.description)

                    spinner.post(Runnable {
                        spinner.setSelection(genres.indexOf(book.genre))
                    })
                }
            }
        })

        val takePhotoButton: ImageButton = view.findViewById(R.id.imageButton)
        takePhotoButton.setOnClickListener {
            viewModel.CAMERA_REQUEST.value = 0
            view?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_addFragment_to_imageCaptureFragment) }
        }

        val takePhotoDesc: ImageButton = view.findViewById(R.id.imageButtonDesc)
        takePhotoDesc.setOnClickListener {
            viewModel.CAMERA_REQUEST.value = 1
            view?.let { it1 -> Navigation.findNavController(it1).navigate(R.id.action_addFragment_to_imageCaptureFragment) }

        }

        imageView.setOnClickListener {
            showPicture()
        }

        buttonSave.setOnClickListener {
            bookGenre = spinner.selectedItem.toString()

            if(!imgURL.isNullOrEmpty()) {
                if (textInputEditTextTitle.text.toString().isNullOrEmpty() ||
                    textInputEditTextAuthor.text.toString().isNullOrEmpty() ||
                    bookGenre.isNullOrEmpty() ||
                    textInputEditTextDescription.text.toString().isNullOrEmpty()) {

                    val message = "One or more fields are empty. Check again, and fill out every field!"
                    AlertDialogFragment().addBookErrorHandling(message,requireContext())
                }
                else {
                    val b = Book(
                        imgURL!!, textInputEditTextTitle.text.toString(),
                        textInputEditTextAuthor.text.toString(),
                        bookGenre, textInputEditTextDescription.text.toString(), false
                    )

                    DatabaseManager.updateBookData(currentUser.email!!, b)

                    dataB = baos.toByteArray()

                    if (dataB.size > 0) {
                        val storageRef = storage.reference
                        mountainsRef = storageRef.child("images/" + imgURL + ".jpg")
                        val uploadTask = mountainsRef.putBytes(dataB)
                        uploadTask.addOnFailureListener {
                            // Handle unsuccessful uploads
                            Toast.makeText(requireActivity(), "Upload: failed!",
                                Toast.LENGTH_LONG).show()
                        }.addOnSuccessListener { taskSnapshot ->
                            Log.d("UPLOAD", "Succes")
                            Toast.makeText(requireActivity(), "Upload: succeeded!",
                                Toast.LENGTH_LONG).show()
                            requireActivity().onBackPressed()
                        }
                    }
                    else {
                        requireActivity().onBackPressed()
                    }
                }
            }
            else {
                val message = "Image not found! Take another picture."
                AlertDialogFragment().addBookErrorHandling(message,requireContext())
            }
        }

        return view
    }
    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            val message = "Something went wrong! Please check your internet connection or try again later!"
            AlertDialogFragment().errorHandling(message, requireContext())
        }

        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        clearViewModel()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            imageBitmap = data?.extras?.get("data") as Bitmap
//
//            if (REQUEST_CAMERA == 0) {
//
//                val storageRef = storage.reference
//                val randomString = Utils.generateRandUUID()
//                mountainsRef = storageRef.child("images/" + randomString + ".jpg")
//
//                imgURL = randomString
//
////                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
////                baos = ByteArrayOutputStream()
////                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            }
//
//            viewModel.image.value = imageBitmap
//
//            view?.let { it -> Navigation.findNavController(it).navigate(R.id.action_addFragment_to_boundingBoxFragment) }
//
//            //processImage()
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        DatabaseManager.deleteSelectedBookData()
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
                val imageurl = Uri.toString()
                Glide.with(requireActivity())
                    .load(imageurl)
                    .placeholder(R.drawable.logo)
                    .into(img)
            }
        }

        if (this::imageBitmap.isInitialized){
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
        viewModel.imageCover.removeObservers(viewLifecycleOwner)
        viewModel.result.removeObservers(viewLifecycleOwner)

        viewModel.imageCover.value = null
        viewModel.result.value = null
    }

}