package com.rha.ai_gallery.videoclassifier

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.rha.ai_gallery.utilities.CommonUtilities
import com.rha.ai_gallery.utilities.Constants
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.nio.FloatBuffer

class VideoActionsClassifier(private val context: Context) {
    private val TAG = "VideoActionsClassifier"

    private var module: Module? = null
    private var modelClasses: List<String>? = null
    private var targetVideoSize: Int = 160
    private var countOfFramesPerInference = 15
    private var inTensorBuffer: FloatBuffer? = null
    private var minNumFrames = 4

    fun initialize(modelFile: String, classesFile: String, videoSize: Int, minAcceptableFrames: Int) {
        try {
            module = LiteModuleLoader.load(CommonUtilities.getAssetFilePath(context, modelFile))
            modelClasses = CommonUtilities.readTextFileAsList(context, classesFile)
            targetVideoSize = videoSize
            minNumFrames = minAcceptableFrames
        } catch (e: Exception) {
            Log.i(TAG, "initialize(), error : ${e.message}")
            e.printStackTrace()
        }
    }

    fun getClassesList(): List<String>? {
        return modelClasses
    }

    fun reset(countOfFrames: Int) {
        if (countOfFrames < minNumFrames) {
            countOfFramesPerInference = minNumFrames
            inTensorBuffer = Tensor.allocateFloatBuffer(countOfFramesPerInference * 3 * targetVideoSize * targetVideoSize)
        } else {
            countOfFramesPerInference = countOfFrames
            inTensorBuffer = Tensor.allocateFloatBuffer(countOfFramesPerInference * 3 * targetVideoSize * targetVideoSize)
        }
    }

    fun addInferenceFrames(frames: List<Bitmap>) {
        frames.forEachIndexed { index, bitmap ->
            TensorImageUtils.bitmapToFloatBuffer(
                bitmap,
                0,
                0,
                targetVideoSize,
                targetVideoSize,
                Constants.MEAN_RGB,
                Constants.STD_RGB,
                inTensorBuffer,
                index * 3 * targetVideoSize * targetVideoSize
            )
        }

        // Fill up the rest of the frames so that atleast minNumFrames is covered
        if (frames.size < countOfFramesPerInference) {
            for (i in 0 until(countOfFramesPerInference - frames.size)) {
                TensorImageUtils.bitmapToFloatBuffer(
                    frames[frames.size - 1],
                    0,
                    0,
                    targetVideoSize,
                    targetVideoSize,
                    Constants.MEAN_RGB,
                    Constants.STD_RGB,
                    inTensorBuffer,
                    (frames.size + i) * 3 * targetVideoSize * targetVideoSize
                )
            }
        }
    }

    fun processFrames():  List<Float>? {
        val inputTensor = Tensor.fromBlob(
            inTensorBuffer,
            longArrayOf(1, 3, countOfFramesPerInference.toLong(), targetVideoSize.toLong(), targetVideoSize.toLong())
        )
        val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
        if (outputTensor != null) {
            val scores = outputTensor.dataAsFloatArray
            return scores.asList()
        } else {
            return null
        }
    }

    fun destroy() {
        inTensorBuffer?.clear()
        module?.destroy()
    }
}