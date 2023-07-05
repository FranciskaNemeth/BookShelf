package com.example.bookshelf.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookshelf.model.BoundingBoxResult
import com.example.bookshelf.model.Images

class BoundingBoxViewModel : ViewModel() {
    val images = MutableLiveData<Images>()
    val result = MutableLiveData<BoundingBoxResult>()
    val description = MutableLiveData<String>()

}