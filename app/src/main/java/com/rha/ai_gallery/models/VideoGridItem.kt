package com.rha.ai_gallery.models

data class VideoGridItem(
    val videoFullPath: String,
    val duration: Double,
    var processing: Boolean
)
