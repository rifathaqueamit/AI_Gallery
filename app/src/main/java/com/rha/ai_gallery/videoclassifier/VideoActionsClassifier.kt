package com.rha.ai_gallery.videoclassifier

import android.content.Context
import android.util.Log
import com.rha.ai_gallery.utilities.CommonUtilities
import org.pytorch.LiteModuleLoader
import org.pytorch.Module

class VideoActionsClassifier(private val context: Context) {
    private val TAG = "VideoActionsClassifier"

    private var module: Module? = null
    private var modelClasses: List<String>? = null

    fun initialize(modelFile: String) {
        try {
            module = LiteModuleLoader.load(CommonUtilities.getAssetFilePath(context, modelFile))
            modelClasses = CommonUtilities.readTextFileAsList(context, "classes.txt")
        } catch (e: Exception) {
            Log.i(TAG, "initialize(), error : ${e.message}")
            e.printStackTrace()
        }
    }

    fun destroy() {
        module?.destroy()
    }
}