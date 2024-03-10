package com.rha.ai_gallery.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rha.ai_gallery.databinding.TimestampEntryBinding
import com.rha.ai_gallery.models.VideoDetection

class TimestampEntryViewHolder(itemView: View) : ViewHolder(itemView) {
    private val binding = TimestampEntryBinding.bind(itemView)
    private var listener: ((VideoDetection) -> Unit)? = null
    private var data: VideoDetection? = null

    init {
        binding.root.setOnClickListener {
            data?.let { listener?.invoke(data!!) }
        }
    }

    fun setData(detection: VideoDetection) {
        data = detection
        binding.txtViewFrom.text = "From time : ${detection.fromTime}"
        binding.txtViewTo.text = "To time : ${detection.toTime}"
        binding.txtViewDetections.text = "Detections :  " + detection.detectionClasses.map {
            return@map it.first + ": Score : " + it.second
        }.joinToString("\n")
    }

    fun setOnClickListener(callback: ((VideoDetection) -> Unit)?) {
        listener = callback
    }
}