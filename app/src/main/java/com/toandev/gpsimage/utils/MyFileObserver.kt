package com.toandev.gpsimage.utils

import android.content.Context
import android.os.FileObserver
import android.util.Log
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import org.opencv.videoio.Videoio
import java.io.File
import java.util.*

open class MyFileObserver(context: Context) {
    private var mFileObserver: FileObserver? = null
    private val mNewFilePaths = ArrayList<String>()

    init {
        val folderPath = MySharedPreferences.getInstance(context).observerFilePath
        Log.i("hson", "Observe path: " + folderPath!!)
        initFileObserver(folderPath)
    }

    private fun initFileObserver(folderPath: String?) {
        Log.i("hson", "Observe path: " + folderPath!!)
        this.mFileObserver = object : FileObserver(folderPath, Videoio.CV_CAP_PROP_XI_GPI_LEVEL) {
            override fun onEvent(event: Int, fileName: String?) {
                if (fileName != null) {
                    if (event == Video.OPTFLOW_FARNEBACK_GAUSSIAN || event == Imgproc.COLOR_BGR2YUV_IYUV) {
                        this@MyFileObserver.mNewFilePaths.add(folderPath + "/" + fileName)
                    }
                    if (event == 8 || event == 16) {
                        val filePath = folderPath + "/" + fileName
                        if (this@MyFileObserver.isAcceptFile(filePath)) {
                            Log.i("hson", "Input: " + filePath)
                            this@MyFileObserver.processFile(filePath)
                        }
                    }
                }
            }
        }
    }

    fun isAcceptFile(filePath: String): Boolean {
        if (!MyUtils.isImage(filePath) || File(filePath).length() <= 0 || !this.mNewFilePaths.contains(filePath)) {
            return false
        }
        this.mNewFilePaths.remove(filePath)
        return true
    }

    fun startWatching() {
        this.mFileObserver?.startWatching()
    }

    fun stopWatching() {
        this.mFileObserver?.stopWatching()
    }

    protected open fun processFile(filePath: String) {}

    fun updateObserverPath(observerPath: String) {
        this.mFileObserver?.apply {
            mNewFilePaths.clear()
            startWatching()
        }
        initFileObserver(observerPath)
        this.mFileObserver?.startWatching()
    }
}
