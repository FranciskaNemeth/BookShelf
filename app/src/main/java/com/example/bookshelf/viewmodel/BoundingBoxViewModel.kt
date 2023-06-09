package com.example.bookshelf.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookshelf.model.BoundingBoxResult

class BoundingBoxViewModel : ViewModel() {
    val image = MutableLiveData<Bitmap>()
    val result = MutableLiveData<BoundingBoxResult>()
}