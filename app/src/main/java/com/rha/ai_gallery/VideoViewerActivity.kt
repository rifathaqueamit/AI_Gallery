package com.rha.ai_gallery

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import com.google.gson.Gson
import com.rha.ai_gallery.databinding.ActivityMainBinding
import com.rha.ai_gallery.databinding.VideoViewerBinding
import com.rha.ai_gallery.models.VideoGridItem

class VideoViewerActivity : AppCompatActivity() {

    private val TAG = "AI_Gallery_VideoViewerActivity"

    private lateinit var binding: VideoViewerBinding
    private lateinit var mediaController: MediaController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VideoViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        mediaController = MediaController(this)
        val url = intent?.getStringExtra("url")
        url?.let {
            Log.i(TAG, "init(), $url")
            binding.videoView.setVideoURI(Uri.parse(url))
            mediaController.setAnchorView(binding.videoView)
            mediaController.setMediaPlayer(binding.videoView)
            binding.videoView.setMediaController(mediaController)
            binding.videoView.start()
        }
        val detections = intent?.getStringExtra("detections")
        detections?.let {
            val detectionsList = Gson().fromJson(it, ArrayList::class.java)
            Log.i(TAG, "detectionsList: $detectionsList")
        }
    }
}