package com.rha.ai_gallery.managers

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import com.rha.ai_gallery.models.VideoMetaData

class VideoMetaDataManager {
    private val TAG = "VideoMetaDataManager"

    private val metaDataRetriever = MediaMetadataRetriever()
    private val videoDetectionsMetaData = HashMap<String, String>() // Later, this may be read/kept in file
    private var inputFile: String? = null

    fun setDataSource(filePath: String) {
        try {
            inputFile = filePath
            metaDataRetriever.setDataSource(filePath)
        } catch (e: Exception) {
            Log.i(TAG, "setDataSource() $filePath, error : ${e.message}")
            e.printStackTrace()
            inputFile = null
        }
    }

    fun getVideoMetaData(): VideoMetaData? {
        if (inputFile == null) return null

        try {
            val stringDetections = videoDetectionsMetaData[inputFile] ?: ""
            val stringDuration = metaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (stringDuration != null) {
                return VideoMetaData(stringDuration.toDouble(), stringDetections)
            }
        } catch (e: Exception) {
            Log.i(TAG, "getVideoMetaData() $inputFile, error : ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    fun getVideoFrame(timeUs: Long): Bitmap? {
        if (inputFile == null) return null
        try {
            return metaDataRetriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            Log.i(TAG, "getVideoFrame() $inputFile, error : ${e.message}")
            e.printStackTrace()
            inputFile = null
        }
        return null
    }

    fun writeDetectionsMetaData(detections: String) {
        if (inputFile == null) return
        videoDetectionsMetaData[inputFile!!] = detections
    }
}