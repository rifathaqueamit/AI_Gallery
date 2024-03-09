package com.rha.ai_gallery.viewholders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rha.ai_gallery.databinding.GridVideoViewBinding
import com.rha.ai_gallery.models.VideoGridItem
import java.io.File

class GridVideoViewHolder(itemView: View) : ViewHolder(itemView) {
    private val viewBinding = GridVideoViewBinding.bind(itemView)
    private var data: VideoGridItem? = null
    private var listener: ((VideoGridItem?) -> Unit)? = null

    init {
        viewBinding.root.setOnClickListener {
            listener?.invoke(data)
        }
    }

    fun setData(context: Context, videoGridItem: VideoGridItem) {
        data = videoGridItem
        viewBinding.title.text = File(videoGridItem.videoFullPath).name
        Glide.with(context)
            .load(videoGridItem.videoFullPath)
            .sizeMultiplier(0.6f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(viewBinding.imageView)
        viewBinding.loading.visibility = if (videoGridItem.processing) View.VISIBLE else View.GONE
    }

    fun setClickListener(callback: ((VideoGridItem?) -> Unit)?) {
        listener = callback
    }
}