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
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.bookshelf.R
import com.example.bookshelf.model.BoundingBoxResult
import com.example.bookshelf.model.BoundingTextBlock
import com.example.bookshelf.model.CameraRequest
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
    private lateinit var swapButton : FloatingActionButton

    private lateinit var viewModel: BoundingBoxViewModel

    lateinit var imageBitmap : Bitmap
    private lateinit var backupImageBitmap: Bitmap

    lateinit var title : String
    lateinit var author : String
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    lateinit var sortedTextBlocks : List<TextBlock>

    private lateinit var boundingTextBlock1: BoundingTextBlock  // book title by default
    private lateinit var boundingTextBlock2: BoundingTextBlock  // book author by default

    private var CAMERA_REQUEST : CameraRequest? = null
    lateinit var description : String

    private val COLOR_TITLE = Color.rgb(0, 102, 255)
    private val COLOR_AUTHOR = Color.rgb(255, 102, 0)

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

        viewModel.images.observe(viewLifecycleOwner) {
            if (it != null) {
                CAMERA_REQUEST = it.CAMERA_REQUEST

                if (CAMERA_REQUEST == CameraRequest.COVER) {
                    backupImageBitmap = it.imageCover!!
                    imageBitmap = it.imageCover!!
                } else {
                    backupImageBitmap = it.imageDesc!!
                    imageBitmap = it.imageDesc!!
                }

                processImage()
            }
        }

        swapButton.setOnClickListener {
            swapBoundingBoxes()
            draw(boundingTextBlock1, boundingTextBlock2)
        }

        checkButton.setOnClickListener {
            if (CAMERA_REQUEST == CameraRequest.COVER) {
                val boundingBoxResult = BoundingBoxResult(title, author)
                viewModel.result.value = boundingBoxResult

                view?.let { it1 ->
                    Navigation.findNavController(it1).popBackStack(R.id.addFragment, false)
                }
            }
            else if (CAMERA_REQUEST == CameraRequest.DESCRIPTION) {
                viewModel.description.value = description

                view?.let { it1 ->
                    Navigation.findNavController(it1).popBackStack(R.id.addFragment, false)
                }
            }

        }

        return view
    }

    private fun processImage() {
        val inputImage : InputImage = InputImage.fromBitmap(imageBitmap, 0)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (CAMERA_REQUEST == CameraRequest.DESCRIPTION) {
                    val resultText = visionText.text
                    val resultTextBlocks : List<TextBlock> = visionText.textBlocks

                    if (resultText.isNotEmpty()) {
                        val left : Float = resultTextBlocks[0].boundingBox?.left!!.toFloat()
                        val top : Float = resultTextBlocks[0].boundingBox?.top!!.toFloat()
                        val right : Float = resultTextBlocks[resultTextBlocks.lastIndex].boundingBox?.right!!.toFloat()
                        val bottom : Float = resultTextBlocks[resultTextBlocks.lastIndex].boundingBox?.bottom!!.toFloat()

                        val resultTextRect = Rect(left, top, right, bottom)

                        description = resultText
                        viewModel.description.value = resultText

                        drawBitmap()
                        drawBoundingBoxDescWithLabel(resultTextRect, "description", COLOR_TITLE)

                        swapButton.isEnabled = false
                        checkButton.isEnabled = true

                    }
                    else {
                        Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                            Toast.LENGTH_LONG).show()
                    }
                }

                if (CAMERA_REQUEST == CameraRequest.COVER) {
                    val textBlocks : List<TextBlock> = visionText.textBlocks

                    if (textBlocks.isNotEmpty() && textBlocks.size > 1) {
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

                        drawBitmap()

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
                canvas.drawRect(boundingBox, paintBoundingBox)
                canvas.drawText(label, boundingBox.left.toFloat(), boundingBox.top.toFloat() - 15, paintLabel)
            }
        }

    }

    private fun drawBoundingBoxDescWithLabel(rect : Rect, label : String, color : Int) {
        if(this::canvas.isInitialized) {
            val paintBoundingBox = Paint()

            paintBoundingBox.color = color
            paintBoundingBox.style = Paint.Style.STROKE
            paintBoundingBox.strokeWidth = 20F

            val paintLabel = Paint()
            paintLabel.color = color
            paintLabel.textSize = 150F

            // Draw the rectangle around the text recognized
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paintBoundingBox)
            canvas.drawText(label, rect.left, rect.top - 15, paintLabel)
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