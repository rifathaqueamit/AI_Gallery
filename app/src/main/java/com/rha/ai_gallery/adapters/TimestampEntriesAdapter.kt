package com.rha.ai_gallery.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rha.ai_gallery.R
import com.rha.ai_gallery.models.VideoDetection
import com.rha.ai_gallery.viewholders.TimestampEntryViewHolder

class TimestampEntriesAdapter(private val detections: List<VideoDetection>):
    RecyclerView.Adapter<TimestampEntryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimestampEntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timestamp_entry, parent, false)
        return  TimestampEntryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return detections.size
    }

    override fun onBindViewHolder(holder: TimestampEntryViewHolder, position: Int) {
        holder.setData(detections[position])
    }
}