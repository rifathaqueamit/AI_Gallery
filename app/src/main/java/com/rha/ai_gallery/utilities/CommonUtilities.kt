package com.rha.ai_gallery.utilities

import android.content.Context
import com.google.gson.Gson
import com.rha.ai_gallery.models.VideoDetection
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

object CommonUtilities {

    fun getAssetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }

    fun readTextFileAsList(context: Context, textFileName: String): List<String> {
        val br = BufferedReader(InputStreamReader(context.assets.open(textFileName)))
        val text = br.readText()
        return text.lines()
    }

    fun detectionsToString(detections: ArrayList<VideoDetection>): String {
        return Gson().toJson(detections)
    }
}