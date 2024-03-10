package com.rha.ai_gallery.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rha.ai_gallery.databinding.TimestampEntryBinding
import com.rha.ai_gallery.models.VideoDetection

class TimestampEntryViewHolder(itemView: View) : ViewHolder(itemView) {
    private val binding = TimestampEntryBinding.bind(itemView)

    fun setData(detection: VideoDetection) {
        binding.txtViewFrom.text = "From time : ${detection.fromTime}"
        binding.txtViewTo.text = "To time : ${detection.toTime}"
        binding.txtViewDetections.text = "Detections :  " + detection.detectionClasses.map {
            return@map it.first + ": Score : " + it.second
        }.joinToString("\n")
    }
}