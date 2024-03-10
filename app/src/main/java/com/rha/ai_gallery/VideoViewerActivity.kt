package com.rha.ai_gallery

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Video
import android.util.Log
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rha.ai_gallery.adapters.TimestampEntriesAdapter
import com.rha.ai_gallery.databinding.VideoViewerBinding
import com.rha.ai_gallery.models.VideoDetection


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
            val detectionsList: ArrayList<VideoDetection> = Gson().fromJson(it, object : TypeToken<ArrayList<VideoDetection>>() {}.type)
            Log.i(TAG, "detectionsList: $detectionsList")
            detectionsList?.let { updateUI(it) }
        }
    }

    private fun updateUI(detections: List<VideoDetection>) {
        binding.recyclerViewTimestamps.layoutManager  = LinearLayoutManager(this)
        binding.recyclerViewTimestamps.adapter = TimestampEntriesAdapter(detections)
    }
}