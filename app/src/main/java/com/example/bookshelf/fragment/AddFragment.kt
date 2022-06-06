package com.example.bookshelf.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import com.example.bookshelf.R
import com.example.bookshelf.utils.Utils
import com.example.bookshelf.utils.Utils.PERMISSION_REQUEST_CODE
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text.TextBlock
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class AddFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    var REQUEST_CAMERA : Int = -1
    lateinit var imageView : ImageView
    lateinit var textInputEditTextAuthor : TextInputEditText
    lateinit var textInputEditTextTitle: TextInputEditText
    lateinit var spinner: Spinner
    lateinit var textInputEditTextDescription: TextInputEditText
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val takePhotoButton : ImageButton = view.findViewById(R.id.imageButton)
        takePhotoButton.setOnClickListener {
            if (Utils.checkPermission(requireActivity())) {
                REQUEST_CAMERA = 0
                dispatchTakePictureIntent()
            } else {
                REQUEST_CAMERA = 0
                Utils.requestPermission(requireActivity())
            }
        }

        val takePhotoDesc : ImageButton = view.findViewById(R.id.imageButtonDesc)
        takePhotoDesc.setOnClickListener {
            if (Utils.checkPermission(requireActivity())) {
                REQUEST_CAMERA = 1
                dispatchTakePictureIntent()
            } else {
                REQUEST_CAMERA = 1
                Utils.requestPermission(requireActivity())
            }
        }

        return view
    }

    override fun onResume() {
        if( !Utils.isNetworkAvailable(requireContext()) ) {
            AlertDialogFragment().errorHandling(requireContext())
        }

        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            if (REQUEST_CAMERA == 0) {
                imageView.setImageBitmap(imageBitmap)
            }

            processImage(imageBitmap)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    ApplicationProvider.getApplicationContext<Context>(),
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()

                // main logic
            } else {
                Toast.makeText(
                    ApplicationProvider.getApplicationContext<Context>(),
                    "Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        Utils.showMessageOKCancel(requireActivity(), "You need to allow access permissions",
                            DialogInterface.OnClickListener { dialog, which ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Utils.requestPermission(requireActivity())
                                }
                            })
                    }
                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Log.d("ERROR", "Camera not found (activity)")

            Utils.showMessageOKCancel(requireActivity(), "Can't open Camera!",
                DialogInterface.OnClickListener { dialog, which ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Utils.requestPermission(requireActivity())
                    }
                })
        }
    }

    fun processImage(image : Bitmap) {
        val inputImage : InputImage = InputImage.fromBitmap(image, 0)

        val result = recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (REQUEST_CAMERA == 1) {
                    val resultText = visionText.text

                    if (resultText.isNotEmpty()) {
                        Log.d("RES", resultText)
                        textInputEditTextDescription.setText(resultText)

                    }
                    else {
                        Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                            Toast.LENGTH_LONG).show()
                    }
                }

                if (REQUEST_CAMERA == 0) {
                    val textBlocks : List<TextBlock> = visionText.textBlocks

                    if (textBlocks.isNotEmpty()) {
                        val sortedTextBlocks = textBlocks.sortedByDescending {
                            it.boundingBox?.width()?.times(it.boundingBox?.height()!!)
                        }

                        textInputEditTextTitle.setText(sortedTextBlocks[0].text)
                        textInputEditTextAuthor.setText(sortedTextBlocks[1].text)
                    }
                    else {
                        Toast.makeText(requireActivity(), "Could not retrieve text from image. Try again!",
                            Toast.LENGTH_LONG).show()
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
}