package com.rha.ai_gallery.models

data class VideoDetection(
    val fromTime: Int,
    val toTime: Int,
    val detectionClasses: List<Pair<String, Float>>
)
