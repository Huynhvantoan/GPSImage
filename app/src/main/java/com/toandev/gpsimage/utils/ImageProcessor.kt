package com.toandev.gpsimage.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log

import com.toandev.demogpsimage.BuildConfig
import com.toandev.demogpsimage.R
import com.toandev.gpsimage.GPSImageService
import com.toandev.gpsimage.MainActivity

import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

import java.io.File
import java.io.IOException
import java.util.LinkedList
import java.util.Queue

class ImageProcessor(private val mContext: Context) {
    private val mImageDatas: Queue<ImageData>
    private var mIsDataProcessing: Boolean = false

    class ImageData(val inputPath: String, val latitude: Double, val longitude: Double, val time: String, val date: String)

    @SuppressLint("StaticFieldLeak")
    internal inner class ProcessImageTask : AsyncTask<Any, Void, Void>() {

        override fun doInBackground(vararg params: Any): Void? {
            val pref = MySharedPreferences.getInstance(this@ImageProcessor.mContext)
            val imageData = params[0] as ImageData
            val outputPath = params[1] as String
            MySpeedTest.start()
            val matInput = Imgcodecs.imread(imageData.inputPath)
            var mapHeight = 1280
            if (1280 > matInput.height()) {
                mapHeight = matInput.height()
            }
            MySpeedTest.stop("load input image")
            MySpeedTest.start()
            try {
                val ggMap = MyUtils.getMap(imageData.latitude, imageData.longitude, pref.mapZoom, pref.mapWidth, mapHeight / 2, 0.0f, this@ImageProcessor.mContext.resources.getString(R.string.key))
                MySpeedTest.stop("download map")
                MySpeedTest.start()
                val matMap = Mat()
                Utils.bitmapToMat(ggMap, matMap, true)
                Imgproc.cvtColor(matMap, matMap, 4)
                val matMapResizeWidth = matMap.width().toFloat() * (matInput.height().toFloat() / matMap.height().toFloat())
                if (matMap.height() < matInput.height()) {
                    Imgproc.resize(matMap, matMap, Size(matMapResizeWidth.toDouble(), matInput.height().toDouble()), 0.0, 0.0, 1)
                }
                MySpeedTest.stop("resize map")
                MySpeedTest.start()
                val bmpUnitInfo = MyUtils.drawTextToBitmap(pref.unitInfo, 130.0f, matInput.width())
                val matUnitInfo = Mat()
                Utils.bitmapToMat(bmpUnitInfo, matUnitInfo)
                Imgproc.cvtColor(matUnitInfo, matUnitInfo, 4)
                MySpeedTest.stop("create unit info")
                val matUnitInfoGray = Mat()
                val unitInfoMask = Mat()
                Imgproc.cvtColor(matUnitInfo, matUnitInfoGray, 6)
                Imgproc.threshold(matUnitInfoGray, unitInfoMask, 10.0, 255.0, 0)
                MySpeedTest.start()
                val bmpImageInfo = MyUtils.drawTextToBitmap(this@ImageProcessor.getImageText(imageData.longitude, imageData.latitude, imageData.date, imageData.time), matInput.width())
                val matImageInfo = Mat()
                Utils.bitmapToMat(bmpImageInfo, matImageInfo)
                Imgproc.cvtColor(matImageInfo, matImageInfo, 4)
                MySpeedTest.stop("create image info")
                val matTextGray = Mat()
                val imageInfoMask = Mat()
                Imgproc.cvtColor(matImageInfo, matTextGray, 6)
                Imgproc.threshold(matTextGray, imageInfoMask, 10.0, 255.0, 0)
                MySpeedTest.start()
                val mat = Mat(matInput.height(), matInput.width() + matMap.width(), CvType.CV_8UC3)
                matInput.copyTo(Mat(mat, Rect(0, 0, matInput.width(), matInput.height())))
                matMap.copyTo(Mat(mat, Rect(matInput.width(), 0, matMap.width(), matMap.height())))
                matUnitInfo.copyTo(Mat(mat, Rect(0, 0, matUnitInfo.width(), matUnitInfo.height())), unitInfoMask)
                matImageInfo.copyTo(Mat(mat, Rect(0, matInput.height() - matImageInfo.height(), matImageInfo.width(), matImageInfo.height())), imageInfoMask)
                MySpeedTest.stop("combine image, map and text")
                MySpeedTest.start()
                Imgcodecs.imwrite(outputPath, mat, MatOfInt(1, pref.imageQuality))
                MySpeedTest.stop("write image")
                MySpeedTest.start()
                MyUtils.copyExifData(File(imageData.inputPath), File(outputPath), null, imageData.longitude, imageData.latitude)
                MySpeedTest.stop("copy exif data")
                val file = File(imageData.inputPath)
                if (file.exists()) {
                    file.delete()
                }
                pref.newestImagePath = outputPath
                Log.i("hson", "Output: " + outputPath)
                val activity = (this@ImageProcessor.mContext as GPSImageService).connectedActivity
                activity?.updateGPSImageView(outputPath)
                return null
            } catch (e: IOException) {
                MyUtils.showToast(this@ImageProcessor.mContext, this@ImageProcessor.mContext.getString(R.string.alert_cannot_connect_internet), 1)
                this@ImageProcessor.mImageDatas.clear()
                this@ImageProcessor.mIsDataProcessing = false
                return null
            }

        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)
            if (this@ImageProcessor.mImageDatas.isEmpty()) {
                this@ImageProcessor.mIsDataProcessing = false
                return
            }
            val data = this@ImageProcessor.mImageDatas.poll() as ImageData
            ProcessImageTask().execute(*arrayOf(data, this@ImageProcessor.genPath(data.date)))
        }
    }

    init {
        if (!OpenCVLoader.initDebug()) {
            Log.i("hson", "error")
        }
        this.mIsDataProcessing = false
        this.mImageDatas = LinkedList()
    }

    fun process(imageData: ImageData) {
        this.mImageDatas.add(imageData)
        if (!this.mIsDataProcessing) {
            this.mIsDataProcessing = true
            val data = this.mImageDatas.poll() as ImageData
            ProcessImageTask().execute(*arrayOf(data, genPath(data.date)))
        }
    }

    private fun getImageText(longitude: Double, latitude: Double, currentDate: String, currentTime: String): String {
        val lonDimen = if (longitude > 0.0) "E" else "W"
        val latdimen = if (latitude > 0.0) "N" else "S"
        val lon = MyUtils.convertToDegree(longitude)
        val lat = MyUtils.convertToDegree(latitude)
        return String.format(this.mContext.getString(R.string.text_image_location_info), *arrayOf<Any>(lonDimen, lon, latdimen, lat, currentTime, currentDate))
    }

    private fun genPath(currentDate: String): String {
        val rootFolder = File(MySharedPreferences.getInstance(this.mContext).outputFilePath!!)
        if (!rootFolder.isDirectory && !rootFolder.mkdirs()) {
            return BuildConfig.FLAVOR
        }
        val folder = File(rootFolder.absolutePath + "/" + currentDate)
        if (!folder.isDirectory && !folder.mkdirs()) {
            return BuildConfig.FLAVOR
        }
        var filePath: String
        var number = 1
        val fileNameBegin = "Hinh"
        do {
            filePath = folder.absolutePath + "/" + fileNameBegin + number + ".jpg"
            number++
        } while (File(filePath).exists())
        return filePath
    }
}
