package com.rha.ai_gallery.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rha.ai_gallery.R
import com.rha.ai_gallery.models.VideoFile
import com.rha.ai_gallery.viewholders.GridVideoViewHolder

class GridVideosViewAdapter(private val videosList: MutableList<VideoFile>): RecyclerView.Adapter<GridVideoViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridVideoViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.grid_video_view, parent, false)
        return GridVideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return videosList.size
    }

    override fun onBindViewHolder(holder: GridVideoViewHolder, position: Int) {
        holder.setData(context, videosList[position])
    }
}