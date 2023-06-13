package com.example.bookshelf.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookshelf.model.BoundingBoxResult

class BoundingBoxViewModel : ViewModel() {
    val imageCover = MutableLiveData<Bitmap>()
    val result = MutableLiveData<BoundingBoxResult>()
    val CAMERA_REQUEST = MutableLiveData<Int>()
    val description = MutableLiveData<String>()
    val imageDesc = MutableLiveData<Bitmap>()
}