package com.rha.ai_gallery

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.GridLayoutManager
import com.rha.ai_gallery.adapters.GridVideosViewAdapter
import com.rha.ai_gallery.databinding.ActivityMainBinding
import com.rha.ai_gallery.models.VideoFile
import com.rha.ai_gallery.videoclassifier.VideoActionsClassifier

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gridVideosViewAdapter: GridVideosViewAdapter
    private var videoActionsClassifier: VideoActionsClassifier? = null

    private val videosList = mutableListOf<VideoFile>()
    private val REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        initClassifier()
    }

    private fun initUI() {
        setSupportActionBar(binding.toolbar)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        gridVideosViewAdapter = GridVideosViewAdapter(videosList)
        binding.recyclerView.adapter = gridVideosViewAdapter
        checkPermission()
    }

    private fun initClassifier() {
        videoActionsClassifier = VideoActionsClassifier(this)
    }

    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_VIDEO), REQUEST_CODE)
        } else {
            loadVideos()
        }
    }

    private fun loadVideos() {
        videosList.clear()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val orderBy = MediaStore.Images.Media.DATE_TAKEN
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media._ID,
        )
        val cursor = contentResolver.query(
            uri, projection, null, null,
            "$orderBy DESC"
        )
        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        while (cursor.moveToNext()) {
            val videoPath = cursor.getString(columnIndexData)
            videosList.add(VideoFile(videoPath))
            gridVideosViewAdapter.notifyItemInserted(videosList.size - 1)
        }
        cursor.close()
    }

    override fun onDestroy() {
        videoActionsClassifier?.destroy()
        super.onDestroy()
    }
}