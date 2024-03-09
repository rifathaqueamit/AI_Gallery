package com.rha.ai_gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.rha.ai_gallery.adapters.GridVideosViewAdapter
import com.rha.ai_gallery.databinding.ActivityMainBinding
import com.rha.ai_gallery.managers.VideoMetaDataManager
import com.rha.ai_gallery.models.VideoGridItem
import com.rha.ai_gallery.models.VideoMetaData
import com.rha.ai_gallery.utilities.CommonUtilities
import com.rha.ai_gallery.videoclassifier.VideoActionsClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Arrays
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity_AI_Gallery"

    private lateinit var binding: ActivityMainBinding
    private lateinit var gridVideosViewAdapter: GridVideosViewAdapter
    private var videoActionsClassifier: VideoActionsClassifier? = null

    private val videosList = mutableListOf<VideoGridItem>()
    private val videosMetaData = HashMap<String,  VideoMetaData>()

    private val videoMetaDataManager = VideoMetaDataManager()

    private val REQUEST_CODE = 123
    private val DETECTION_PER_SECOND = 10   // Produce a detection result per 10 seconds
    private val TARGET_VIDEO_SIZE = 160
    private val VIDEO_PATH_FILTER = "poc_test_videos"
    private val TOP_COUNT = 5
    private val MIN_NUM_FRAMES = 4
    private val DETECTION_THRESHOLD = 5

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        initClassifier()
    }

    private fun onVideoClick(videoGridItem: VideoGridItem?) {
        videoGridItem?.let {
            val metaData = videosMetaData[it.videoFullPath]
            if (!it.processing && metaData != null) {
                val intent = Intent(this, VideoViewerActivity::class.java)
                intent.putExtra("url", it.videoFullPath)
                intent.putExtra("detections", metaData.detections)
                startActivity(intent)
            }
        }
    }

    private fun initUI() {
        setSupportActionBar(binding.toolbar)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        gridVideosViewAdapter = GridVideosViewAdapter(videosList) {
            onVideoClick(it)
        }
        binding.recyclerView.adapter = gridVideosViewAdapter
        checkPermission()
    }

    private fun initClassifier() {
        videoActionsClassifier = VideoActionsClassifier(this)
        videoActionsClassifier?.initialize("video_classification.ptl", "classes.txt", TARGET_VIDEO_SIZE, MIN_NUM_FRAMES)
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
            if (videoPath.contains(VIDEO_PATH_FILTER, false)) {
                videosList.add(VideoGridItem(videoPath, 0.0, true))   // Duration will be calculated later
                gridVideosViewAdapter.notifyItemInserted(videosList.size - 1)
            }
        }
        cursor.close()
    }

    override fun onDestroy() {
        cancelProcess()
        videoActionsClassifier?.destroy()
        super.onDestroy()
    }

    private fun showProcessing(videoIndex: Int, show: Boolean) {
        runOnUiThread {
            videosList[videoIndex].processing = show
            gridVideosViewAdapter.notifyItemChanged(videoIndex)
        }
    }

    private fun cancelProcess() {
        if (job != null) {
            if (!job!!.isCancelled) {
                job!!.cancel()
            }
            job = null
        }
    }

    private fun processVideos() {
        cancelProcess()
        job = scope.launch {
            videosMetaData.clear()
            videosList.forEachIndexed { index, video ->
                videoMetaDataManager.setDataSource(video.videoFullPath)
                val videoDetections = arrayListOf<Pair<Int, List<Pair<String, Float>>>>()
                val metaData = videoMetaDataManager.getVideoMetaData()
                if (metaData != null) {
                    videosMetaData[video.videoFullPath] = metaData
                    // Log.i(TAG, "processVideos() video : ${video.videoFullPath}, duration : ${metaData.duration}")

                    if (metaData.detections.isNullOrEmpty()) {

                        val durationInSeconds = ceil(metaData.duration / 1000).toInt()
                        var countOfFrames = DETECTION_PER_SECOND
                        if (durationInSeconds < DETECTION_PER_SECOND)  {
                            countOfFrames = durationInSeconds
                        }
                        // Log.i(TAG, "processVideos() countOfFrames : $countOfFrames")
                        videoActionsClassifier?.reset(countOfFrames)

                        val frames = mutableListOf<Bitmap>()
                        var timeStamp = 0
                        for (i in 0 until durationInSeconds) {
                            val fromMs = i * 1000
                            var toMs = (i + 1) * 1000
                            if (i == durationInSeconds - 1)  toMs = (durationInSeconds * 1000) - 1
                            val timeUs = (1000 * (fromMs + ((toMs - fromMs) * i / (countOfFrames - 1.0)).toInt())).toLong()
                            val bitmap = videoMetaDataManager .getVideoFrame(timeUs)
                            bitmap?.let {
                                val ratio = Math.min(bitmap.width, bitmap.height) / TARGET_VIDEO_SIZE.toFloat()
                                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width / ratio).toInt(), (bitmap.height / ratio).toInt(), true)
                                val centerCroppedBitmap = Bitmap.createBitmap(
                                    resizedBitmap,
                                    if (resizedBitmap.width > resizedBitmap.height) (resizedBitmap.width - resizedBitmap.height) / 2 else 0,
                                    if (resizedBitmap.height > resizedBitmap.width) (resizedBitmap.height - resizedBitmap.width) / 2 else 0,
                                    TARGET_VIDEO_SIZE,
                                    TARGET_VIDEO_SIZE
                                )
                                frames.add(centerCroppedBitmap)
                                resizedBitmap.recycle()
                                it.recycle()
                            }
                            if  ((i + 1) % DETECTION_PER_SECOND == 0 || i == durationInSeconds - 1) {
                                // Log.i(TAG, "processVideos() inference frames count : ${frames.size}")
                                videoActionsClassifier?.addInferenceFrames(frames)
                                val scores = videoActionsClassifier?.processFrames()

                                scores?.let {
                                    // Sort
                                    var scoresWithClasses = scores.mapIndexed { index, fl ->
                                        return@mapIndexed Pair(index, fl)
                                    }
                                    scoresWithClasses = scoresWithClasses.sortedWith(Comparator { o1, o2 ->
                                        return@Comparator o2.second.compareTo(o1.second)
                                    })
                                    // Threshold
                                    scoresWithClasses = scoresWithClasses.filter {
                                        return@filter it.second >= DETECTION_THRESHOLD
                                    }
                                    // Top
                                    scoresWithClasses =  scoresWithClasses.take(TOP_COUNT)

                                    val classes = videoActionsClassifier?.getClassesList()
                                    if (classes != null) {
                                        val detectionsWithScores = scoresWithClasses.map {
                                            return@map Pair(classes[it.first], it.second)
                                        }
                                        // Log.i(TAG, "result with classes : $detectionsWithScores")
                                        videoDetections.add(
                                            Pair(timeStamp, detectionsWithScores)
                                        )
                                    }
                                }

                                timeStamp = i + 1
                                countOfFrames = durationInSeconds - (i + 1)
                                videoActionsClassifier?.reset(countOfFrames)

                                frames.forEach {
                                    it.recycle()
                                }
                                frames.clear()
                            }
                        }

                        Log.i(TAG, "video : ${video.videoFullPath}")
                        val detectionsStr = CommonUtilities.detectionsToString(videoDetections)
                        metaData.detections = detectionsStr
                        videosMetaData[video.videoFullPath] = metaData
                        videoMetaDataManager.writeDetectionsMetaData(detectionsStr)
                        showProcessing(index, false)
                        Log.i(TAG, "detections : ${videosMetaData[video.videoFullPath]}")
                    } else {
                        // Already have detections metadata
                        Log.i(TAG, "detections : ${videosMetaData[video.videoFullPath]}")
                        showProcessing(index, false)
                    }
                }
            }
        }
    }
}