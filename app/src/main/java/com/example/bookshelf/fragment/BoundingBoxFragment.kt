package com.example.bookshelf.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bookshelf.R
import com.example.bookshelf.utils.Utils
import com.example.bookshelf.viewmodel.BoundingBoxViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class BoundingBoxFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser

    lateinit var imageView: ImageView
    lateinit var checkButton : FloatingActionButton
    lateinit var switchButton : FloatingActionButton

    lateinit var viewModel: BoundingBoxViewModel //by viewModels({requireParentFragment()})

    lateinit var imageBitmap : Bitmap
    lateinit var title : String
    lateinit var author : String
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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
        val view = inflater.inflate(R.layout.fragment_bounding_box, container, false)

        imageView = view.findViewById(R.id.imageBoundingBox)
        checkButton = view.findViewById(R.id.checkButton)
        switchButton = view.findViewById(R.id.switchButton)


        viewModel.image.observe(viewLifecycleOwner, Observer {
            imageBitmap = viewModel.image.value!!
            processImage()
        })

        return view
    }

    fun processImage() {
        val inputImage : InputImage = InputImage.fromBitmap(imageBitmap, 0)

        val result = recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->

                val textBlocks : List<Text.TextBlock> = visionText.textBlocks

                if (textBlocks.isNotEmpty()) {
                    val sortedTextBlocks = textBlocks.sortedByDescending {
                        it.boundingBox?.width()?.times(it.boundingBox?.height()!!)
                    }

                    // Create a mutable bitmap
                    //imageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)

                    // Get the block bounding box
                    val boundingBox = sortedTextBlocks[0].boundingBox
                    val canvas = Canvas(imageBitmap)
                    val paintBoundingBox = Paint()
                    paintBoundingBox.color = Color.RED
                    paintBoundingBox.style = Paint.Style.STROKE
                    paintBoundingBox.strokeWidth = 1F

                    val paintTitle = Paint()
                    paintTitle.color = Color.RED
                    paintTitle.textSize = 15F

                    // Draw the rectangle around the text recognized
                    if (boundingBox != null) {
                        canvas.drawRect(boundingBox!!, paintBoundingBox)
                        canvas.drawText("title", boundingBox.left.toFloat(), boundingBox.top.toFloat(), paintTitle)
                    }

                    imageView.setImageBitmap(imageBitmap)

                    title = Utils.capitalizeFirstLetters(sortedTextBlocks[0].text)
                    author = Utils.capitalizeFirstLetters(sortedTextBlocks[1].text)

                    viewModel.result.value?.title = title
                    viewModel.result.value?.author = author

                }
                else {
                    Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                        Toast.LENGTH_LONG).show()
                }

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.d("ERROR", e.toString())

                Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                    Toast.LENGTH_LONG).show()
            }
    }

}