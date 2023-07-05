package com.example.bookshelf.model

import com.google.mlkit.vision.text.Text.TextBlock

data class BoundingTextBlock(
   var textBlock: TextBlock,
   var label: String,
   var color: Int
) {}