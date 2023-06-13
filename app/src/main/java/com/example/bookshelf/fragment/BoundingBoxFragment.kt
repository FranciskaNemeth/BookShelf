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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.bookshelf.R
import com.example.bookshelf.model.BoundingBoxResult
import com.example.bookshelf.model.BoundingTextBlock
import com.example.bookshelf.utils.Utils
import com.example.bookshelf.viewmodel.BoundingBoxViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text.TextBlock
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class BoundingBoxFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser

    lateinit var imageView: ImageView
    lateinit var checkButton : FloatingActionButton
    lateinit var swapButton : FloatingActionButton

    lateinit var viewModel: BoundingBoxViewModel //by viewModels({requireParentFragment()})

    lateinit var imageBitmap : Bitmap
    lateinit var backupImageBitmap: Bitmap

    lateinit var title : String
    lateinit var author : String
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    lateinit var sortedTextBlocks : List<TextBlock>

    lateinit var boundingTextBlock1: BoundingTextBlock  // book title by default
    lateinit var boundingTextBlock2: BoundingTextBlock  // book author by default

    var CAMERA_REQUEST : Int? = null
    lateinit var description : String

//    val COLOR_TITLE = Color.rgb(237, 187, 153)
//    val COLOR_AUTHOR = Color.rgb(84, 153, 199)
    val COLOR_TITLE = Color.rgb(0, 102, 255)
    val COLOR_AUTHOR = Color.rgb(255, 102, 0)

    lateinit var canvas : Canvas

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
        swapButton = view.findViewById(R.id.swapButton)


        viewModel.CAMERA_REQUEST.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                CAMERA_REQUEST = it
            }
        })

        viewModel.imageDesc.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                imageBitmap = it
                backupImageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
                processImage()
            }
        })

        viewModel.imageCover.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                imageBitmap = it
                backupImageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
                processImage()
            }
        })

        swapButton.setOnClickListener {
            swapBoundingBoxes()
            draw(boundingTextBlock1, boundingTextBlock2)
        }

        checkButton.setOnClickListener {
            if (CAMERA_REQUEST == 0) {
                val boundingBoxResult = BoundingBoxResult(title, author)
                viewModel.result.value = boundingBoxResult

                view?.let { it1 ->
                    Navigation.findNavController(it1).popBackStack(R.id.addFragment, false)
                }
            }
            else if (CAMERA_REQUEST == 1) {
                viewModel.description.value = description

                view?.let { it1 ->
                    Navigation.findNavController(it1).popBackStack(R.id.addFragment, false)
                }
            }

        }

        return view
    }

    fun processImage() {
        val inputImage : InputImage = InputImage.fromBitmap(imageBitmap, 0)

        val result = recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (CAMERA_REQUEST == 1) {
                    val resultText = visionText.text

                    if (resultText.isNotEmpty()) {
                        Log.d("RES", resultText)
                        description = resultText
                        viewModel.description.value = resultText

                        drawBitmap()

                        swapButton.isEnabled = false
                        checkButton.isEnabled = true
                    }
                    else {
                        Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                            Toast.LENGTH_LONG).show()
                    }
                }

                if (CAMERA_REQUEST == 0) {
                    val textBlocks : List<TextBlock> = visionText.textBlocks

                    if (textBlocks.isNotEmpty()) {
                        sortedTextBlocks = textBlocks.sortedByDescending {
                            it.boundingBox?.width()?.times(it.lines[0].boundingBox?.height()!!)
                        }

                        boundingTextBlock1 = BoundingTextBlock(sortedTextBlocks[0], "title", COLOR_TITLE)
                        boundingTextBlock2 = BoundingTextBlock(sortedTextBlocks[1], "author", COLOR_AUTHOR)

                        draw(boundingTextBlock1, boundingTextBlock2)

                        title = Utils.capitalizeFirstLetters(sortedTextBlocks[0].text)
                        author = Utils.capitalizeFirstLetters(sortedTextBlocks[1].text)

                        swapButton.isEnabled = true
                        checkButton.isEnabled = true

                    }
                    else {
                        Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                            Toast.LENGTH_LONG).show()

                        swapButton.isEnabled = false
                        checkButton.isEnabled = false

                    }
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.d("ERROR", e.toString())

                Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun draw(boundingTextBlock1: BoundingTextBlock, boundingTextBlock2: BoundingTextBlock) {
        //clearCanvas()
        drawBitmap()
        drawBoundingBoxWithLabel(boundingTextBlock1.textBlock,
            boundingTextBlock1.label,
            boundingTextBlock1.color)
        drawBoundingBoxWithLabel(boundingTextBlock2.textBlock,
            boundingTextBlock2.label,
            boundingTextBlock2.color)
    }

    private fun drawBitmap() {
        imageBitmap = backupImageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(imageBitmap)
        imageView.setImageBitmap(imageBitmap)
    }
    private fun drawBoundingBoxWithLabel(textBlock : TextBlock, label : String, color : Int) {
        if(this::canvas.isInitialized) {
            val boundingBox = textBlock.boundingBox

            val paintBoundingBox = Paint()

            paintBoundingBox.color = color
            paintBoundingBox.style = Paint.Style.STROKE
            paintBoundingBox.strokeWidth = 20F

            val paintLabel = Paint()
            paintLabel.color = color
            paintLabel.textSize = 150F

            // Draw the rectangle around the text recognized
            if (boundingBox != null) {
                canvas.drawRect(boundingBox!!, paintBoundingBox)
                canvas.drawText(label, boundingBox.left.toFloat(), boundingBox.top.toFloat() - 15, paintLabel)
            }
        }

    }

    private fun swapBoundingBoxes() {
        val textBlock1 = boundingTextBlock1.textBlock
        val textBlock2 = boundingTextBlock2.textBlock

        boundingTextBlock1.textBlock = textBlock2
        boundingTextBlock2.textBlock = textBlock1

        val t = title
        title = author
        author = t

    }

    override fun onStop() {
        super.onStop()

        getFragmentManager()?.beginTransaction()?.remove(this)?.commit()
    }

}