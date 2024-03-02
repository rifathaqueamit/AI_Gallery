package com.rha.ai_gallery.managers

import android.media.MediaMetadataRetriever
import android.util.Log
import com.rha.ai_gallery.models.VideoMetaData

class VideoMetaDataManager {
    private val TAG = "VideoMetaDataManager"

    private val metaDataRetriever = MediaMetadataRetriever()
    private val videoDetectionsMetaData = HashMap<String, String>() // Later, this may be read/kept in file
    fun getVideoMetaData(filePath: String): VideoMetaData? {
        try {
            metaDataRetriever.setDataSource(filePath)
            val stringDetections = videoDetectionsMetaData[filePath] ?: ""
            val stringDuration = metaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (stringDuration != null) {
                return VideoMetaData(stringDuration.toDouble(), stringDetections)
            }
        } catch (e: Exception) {
            Log.i(TAG, "getVideoMetaData() $filePath, error : ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    fun writeDetectionsMetaData(filePath: String, detections: String) {
        // TODO
        videoDetectionsMetaData[filePath] = detections
    }
}