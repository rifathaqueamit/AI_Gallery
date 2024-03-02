package com.rha.ai_gallery

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.rha.ai_gallery.adapters.GridVideosViewAdapter
import com.rha.ai_gallery.databinding.ActivityMainBinding
import com.rha.ai_gallery.managers.VideoMetaDataManager
import com.rha.ai_gallery.models.VideoGridItem
import com.rha.ai_gallery.models.VideoMetaData
import com.rha.ai_gallery.videoclassifier.VideoActionsClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity_AI_Gallery"

    private lateinit var binding: ActivityMainBinding
    private lateinit var gridVideosViewAdapter: GridVideosViewAdapter
    private var videoActionsClassifier: VideoActionsClassifier? = null

    private val videosList = mutableListOf<VideoGridItem>()
    private val videosMetaData = HashMap<VideoGridItem,  VideoMetaData>()

    private val videoMetaDataManager = VideoMetaDataManager()

    private val REQUEST_CODE = 123

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

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
            processVideos()
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
            videosList.add(VideoGridItem(videoPath, 0.0, true))   // Duration will be calculated later
            gridVideosViewAdapter.notifyItemInserted(videosList.size - 1)
        }
        cursor.close()
    }

    override fun onDestroy() {
        videoActionsClassifier?.destroy()
        super.onDestroy()
    }

    private fun showProcessing(videoIndex: Int, show: Boolean) {
        runOnUiThread {
            videosList[videoIndex].processing = show
            gridVideosViewAdapter.notifyItemChanged(videoIndex)
        }
    }

    private fun processVideos() {
        if (job != null) {
            if (!job!!.isCancelled) {
                job!!.cancel()
            }
            job = null
        }
        job = scope.launch {
            videosMetaData.clear()
            videosList.forEachIndexed { index, video ->
                val metaData = videoMetaDataManager.getVideoMetaData(video.videoFullPath)
                if (metaData != null) {
                    videosMetaData[video] = metaData
                    showProcessing(index, false)
                    Log.i(TAG, "processVideos() video : ${video.videoFullPath}, duration : ${metaData.duration}")
                }
            }
        }
    }
}