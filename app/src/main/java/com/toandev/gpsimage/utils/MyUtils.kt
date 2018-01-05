package com.toandev.gpsimage.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory.Options
import android.graphics.Paint.Align
import android.location.LocationManager
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import org.apache.sanselan.ImageReadException
import org.apache.sanselan.ImageWriteException
import org.apache.sanselan.Sanselan
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter
import org.apache.sanselan.formats.tiff.TiffImageMetadata
import org.apache.sanselan.formats.tiff.constants.TagInfo
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory
import org.apache.sanselan.formats.tiff.write.TiffOutputField
import org.apache.sanselan.formats.tiff.write.TiffOutputSet
import org.opencv.android.LoaderCallbackInterface
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.Videoio
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MyUtils {

    fun BITMAP_RESIZER(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888)
        val ratioX = newWidth.toFloat() / bitmap.width.toFloat()
        val ratioY = newHeight.toFloat() / bitmap.height.toFloat()
        val middleX = newWidth.toFloat() / 2.0f
        val middleY = newHeight.toFloat() / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap)
        canvas.matrix = scaleMatrix
        canvas.drawBitmap(bitmap, middleX - (bitmap.width / 2).toFloat(), middleY - (bitmap.height / 2).toFloat(), Paint(2))
        return scaledBitmap
    }

    companion object {
        val isCameraUsebyApp: Boolean
            get() = throw UnsupportedOperationException("Method not decompiled: com.hson.gpsimage.MyUtils.isCameraUsebyApp():boolean")

        fun getBitmapFromURL(src: String): Bitmap? {
            try {
                val connection = URL(src).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                return BitmapFactory.decodeStream(connection.inputStream)
            } catch (e: IOException) {
                return null
            }

        }

        fun getBitmapFromFile(file: File): Bitmap? {
            var myBitmap: Bitmap? = null
            if (file.exists()) {
                myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (myBitmap == null) {
                    Log.i("hson", "decode file null - " + file.absolutePath)
                }
            }
            return myBitmap
        }

        fun combineImage(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
            Log.i("hson", if (bmp1 == null) "null" else "not null")
            Log.i("hson", if (bmp2 == null) "null" else "not null")
            val resultImage = Bitmap.createBitmap(bmp1.width + bmp2.width, bmp1.height, bmp1.config)
            val canvas = Canvas(resultImage)
            canvas.drawBitmap(bmp1, 0.0f, 0.0f, null)
            canvas.drawBitmap(bmp2, bmp1.width.toFloat(), 0.0f, null)
            return resultImage
        }

        @SuppressLint("WrongConstant")
        fun isNetworkAvailable(context: Context): Boolean {
            val activeNetworkInfo = (context.getSystemService("connectivity") as ConnectivityManager).activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        @SuppressLint("WrongConstant")
        fun isLocationEnabled(context: Context): Boolean {
            val manager = context.getSystemService("location") as LocationManager
            return manager.isProviderEnabled("gps") || manager.isProviderEnabled("network")
        }

        fun saveBitmap(bmp: Bitmap, filename: String) {
            var e: Exception
            val th: Throwable
            var out: FileOutputStream? = null
            try {
                val out2 = FileOutputStream(filename)
                try {
                    bmp.compress(CompressFormat.JPEG, 100, out2)
                    if (out2 != null) {
                        try {
                            out2.close()
                        } catch (e2: IOException) {
                            e2.printStackTrace()
                            out = out2
                            return
                        }

                    }
                    out = out2
                } catch (e3: Exception) {
                    e = e3
                    out = out2
                    try {
                        e.printStackTrace()
                        if (out != null) {
                            try {
                                out.close()
                            } catch (e22: IOException) {
                                e22.printStackTrace()
                            }

                        }
                    } catch (th2: Throwable) {
                        th = th2
                        if (out != null) {
                            try {
                                out.close()
                            } catch (e222: IOException) {
                                e222.printStackTrace()
                            }

                        }
                        try {
                            throw th
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }

                    }

                } catch (th3: Throwable) {
                    th = th3
                    out = out2
                    if (out != null) {
                        out.close()
                    }
                    try {
                        throw th
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }

                }

            } catch (e4: Exception) {
                e = e4
                e.printStackTrace()
                if (out != null) {
                    try {
                        out.close()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }

                }
            }

        }

        fun copyExifData(sourceFile: File, destFile: File, excludedFields: List<TagInfo>?, longitude: Double, latitude: Double): Boolean {
            var exception: ImageReadException
            var th: Throwable
            var exception2: ImageWriteException
            var exception3: IOException
            var tempFile: File? = null
            var tempStream: OutputStream? = null
            try {
                val tempFile2 = File(destFile.absolutePath + ".tmp")
                try {
                    val sourceSet = getSanselanOutputSet(sourceFile, 73)
                    val destSet = getSanselanOutputSet(destFile, sourceSet.byteOrder)
                    if (sourceSet.byteOrder != destSet.byteOrder) {
                        if (tempStream != null) {
                            try {
                                tempStream.close()
                            } catch (e: IOException) {
                            }

                        }
                        if (tempFile2 != null && tempFile2.exists()) {
                            tempFile2.delete()
                        }
                        tempFile = tempFile2
                        return false
                    }
                    destSet.orCreateExifDirectory
                    val sourceDirectories = sourceSet.directories
                    for (i in sourceDirectories.indices) {
                        val sourceDirectory = sourceDirectories[i] as TiffOutputDirectory
                        val destinationDirectory = getOrCreateExifDirectory(destSet, sourceDirectory)
                        if (destinationDirectory != null) {
                            val sourceFields = sourceDirectory.fields
                            for (j in sourceFields.indices) {
                                val sourceField = sourceFields[j] as TiffOutputField
                                if (excludedFields != null) {
                                    if (excludedFields.contains(sourceField.tagInfo)) {
                                        destinationDirectory.removeField(sourceField.tagInfo)
                                    }
                                }
                                destinationDirectory.removeField(sourceField.tagInfo)
                                destinationDirectory.add(sourceField)
                            }
                        }
                    }
                    destSet.setGPSInDegrees(longitude, latitude)
                    val tempStream2 = BufferedOutputStream(FileOutputStream(tempFile2))
                    try {
                        ExifRewriter().updateExifMetadataLossless(destFile, tempStream2, destSet)
                        tempStream2.close()
                        if (destFile.delete()) {
                            tempFile2.renameTo(destFile)
                        }
                        if (tempStream2 != null) {
                            try {
                                tempStream2.close()
                            } catch (e2: IOException) {
                            }

                        }
                        if (tempFile2 != null && tempFile2.exists()) {
                            tempFile2.delete()
                        }
                        tempStream = tempStream2
                        tempFile = tempFile2
                        return true
                    } catch (e3: ImageReadException) {
                        exception = e3
                        tempStream = tempStream2
                        tempFile = tempFile2
                        try {
                            exception.printStackTrace()
                            if (tempStream != null) {
                                try {
                                    tempStream.close()
                                } catch (e4: IOException) {
                                }

                            }
                            tempFile.delete()
                            return false
                        } catch (th2: Throwable) {
                            th = th2
                            if (tempStream != null) {
                                try {
                                    tempStream.close()
                                } catch (e5: IOException) {
                                }

                            }
                            tempFile.delete()
                            throw th
                        }

                    } catch (e6: ImageWriteException) {
                        exception2 = e6
                        tempStream = tempStream2
                        tempFile = tempFile2
                        exception2.printStackTrace()
                        if (tempStream != null) {
                            try {
                                tempStream.close()
                            } catch (e7: IOException) {
                            }

                        }
                        tempFile.delete()
                        return false
                    } catch (e8: IOException) {
                        exception3 = e8
                        tempStream = tempStream2
                        tempFile = tempFile2
                        exception3.printStackTrace()
                        if (tempStream != null) {
                            try {
                                tempStream.close()
                            } catch (e9: IOException) {
                            }

                        }
                        tempFile.delete()
                        return false
                    } catch (th3: Throwable) {
                        th = th3
                        tempStream = tempStream2
                        tempFile = tempFile2
                        if (tempStream != null) {
                            tempStream.close()
                        }
                        tempFile.delete()
                        throw th
                    }

                } catch (e10: ImageReadException) {
                    exception = e10
                    tempFile = tempFile2
                    exception.printStackTrace()
                    if (tempStream != null) {
                        tempStream.close()
                    }
                    tempFile.delete()
                    return false
                } catch (e11: ImageWriteException) {
                    exception2 = e11
                    tempFile = tempFile2
                    exception2.printStackTrace()
                    if (tempStream != null) {
                        tempStream.close()
                    }
                    tempFile.delete()
                    return false
                } catch (e12: IOException) {
                    exception3 = e12
                    tempFile = tempFile2
                    exception3.printStackTrace()
                    if (tempStream != null) {
                        tempStream.close()
                    }
                    tempFile.delete()
                    return false
                } catch (th4: Throwable) {
                    th = th4
                    tempFile = tempFile2
                    if (tempStream != null) {
                        tempStream.close()
                    }
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete()
                    }
                    try {
                        throw th
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }

                }

            } catch (e15: IOException) {
                exception3 = e15
                exception3.printStackTrace()
                if (tempStream != null) {
                    try {
                        tempStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete()
                }
                return false
            }

            return false
        }

        @Throws(IOException::class, ImageReadException::class, ImageWriteException::class)
        private fun getSanselanOutputSet(jpegImageFile: File, defaultByteOrder: Int): TiffOutputSet {
            var defaultByteOrder = defaultByteOrder
            var exif: TiffImageMetadata? = null
            var outputSet: TiffOutputSet? = null
            val jpegMetadata = Sanselan.getMetadata(jpegImageFile) as JpegImageMetadata
            if (jpegMetadata != null) {
                exif = jpegMetadata.exif
                if (exif != null) {
                    outputSet = exif.outputSet
                }
            }
            if (outputSet == null) {
                if (exif != null) {
                    defaultByteOrder = exif.contents.header.byteOrder
                }
                outputSet = TiffOutputSet(defaultByteOrder)
            }
            return outputSet
        }

        private fun getOrCreateExifDirectory(outputSet: TiffOutputSet, outputDirectory: TiffOutputDirectory): TiffOutputDirectory? {
            var result: TiffOutputDirectory? = outputSet.findDirectory(outputDirectory.type)
            if (result != null) {
                return result
            }
            result = TiffOutputDirectory(outputDirectory.type)
            try {
                outputSet.addDirectory(result)
                return result
            } catch (e: ImageWriteException) {
                return null
            }

        }

        fun getImageOrientation(path: String): Int {
            var exif: ExifInterface? = null
            try {
                exif = ExifInterface(path)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return exif!!.getAttributeInt("Orientation", 0)
        }

        fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
            val matrix = Matrix()
            when (orientation) {
                Videoio.VIDEOWRITER_PROP_FRAMEBYTES /*2*/ -> matrix.setScale(-1.0f, 1.0f)
                Videoio.CV_CAP_PROP_FRAME_WIDTH /*3*/ -> matrix.setRotate(180.0f)
                Videoio.CV_CAP_PROP_FRAME_HEIGHT /*4*/ -> {
                    matrix.setRotate(180.0f)
                    matrix.postScale(-1.0f, 1.0f)
                }
                Videoio.CAP_PVAPI_PIXELFORMAT_RGB24 /*5*/ -> {
                    matrix.setRotate(90.0f)
                    matrix.postScale(-1.0f, 1.0f)
                }
                Videoio.CAP_PVAPI_PIXELFORMAT_BGR24 /*6*/ -> matrix.setRotate(90.0f)
                Videoio.CAP_PVAPI_PIXELFORMAT_RGBA32 /*7*/ -> {
                    matrix.setRotate(-90.0f)
                    matrix.postScale(-1.0f, 1.0f)
                }
                Videoio.CAP_PVAPI_PIXELFORMAT_BGRA32 /*8*/ -> matrix.setRotate(-90.0f)
                else -> return bitmap
            }
            try {
                val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                return bmRotated
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                return null
            }

        }

        fun isImage(fileName: String): Boolean {
            for (extension in arrayOf(".jpg", ".png", ".gif", ".jpeg")) {
                if (fileName.toLowerCase().endsWith(extension)) {
                    return true
                }
            }
            return false
        }

        fun captureView(view: View): Bitmap {
            val image = Bitmap.createBitmap(Videoio.CAP_QT, Videoio.CV_CAP_ANDROID, Config.ARGB_8888)
            view.draw(Canvas(image))
            return image
        }

        fun convertToBitmap(layout: View): Bitmap {
            layout.isDrawingCacheEnabled = true
            layout.buildDrawingCache()
            return layout.drawingCache
        }

        @Throws(IOException::class)
        fun getMap(lat: Double, lon: Double, zoom: Int, width: Int, height: Int, rotate: Float, key: String): Bitmap {
            val connection = URL(String.format("http://maps.google.com/maps/api/staticmap?markers=%f,%f&zoom=%d&size=%dx%d&scale=2&key=%s", *arrayOf(java.lang.Double.valueOf(lat), java.lang.Double.valueOf(lon), Integer.valueOf(zoom), Integer.valueOf(width), Integer.valueOf(height), key))).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            return BitmapFactory.decodeStream(connection.inputStream)
        }

        fun resizeBitMapImage1(filePath: String, targetWidth: Int, targetHeight: Int): Bitmap? {
            var bitMapImage: Bitmap? = null
            try {
                val options = Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(filePath, options)
                var sampleSize = 0.0
                val scaleByHeight = java.lang.Boolean.valueOf(Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth - targetWidth))
                if (options.outHeight * options.outWidth * 2 >= 1638) {
                    sampleSize = Math.pow(2.0, Math.floor(Math.log(if (scaleByHeight) (options.outHeight / targetHeight).toDouble() else (options.outWidth / targetWidth).toDouble()) / Math.log(2.0))).toInt().toDouble()
                }
                options.inJustDecodeBounds = false
                options.inTempStorage = ByteArray(Imgproc.COLOR_BGR2YUV_IYUV)
                while (true) {
                    try {
                        options.inSampleSize = sampleSize.toInt()
                        bitMapImage = BitmapFactory.decodeFile(filePath, options)
                        break
                    } catch (e: Exception) {
                        sampleSize *= 2.0
                    }

                }
            } catch (e2: Exception) {
            }

            return bitMapImage
        }

        fun resize(filePath: String): Bitmap {
            val options = Options()
            options.inPreferredConfig = Config.ARGB_8888
            val bitmap = BitmapFactory.decodeFile(filePath, options)
            val out = ByteArrayOutputStream()
            bitmap.compress(CompressFormat.JPEG, 100, out)
            return BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
        }

        val currentDate: String
            get() = SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().time)

        val currentTime: String
            get() = SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance().time)

        fun convertToDegree(number: Double): String {
            val firstNumber = "100"
            val secondNumber = "400"
            val secondTemp = (number - getDoubleBeforePoint(number).toDouble()) * 60.0
            val thirdNumber = getDoubleBeforePoint((secondTemp - getDoubleBeforePoint(secondTemp).toDouble()) * 60.0)
            return String.format("%d.%d.%d", *arrayOf<Any>(Integer.valueOf(firstNumber), Integer.valueOf(secondNumber), Integer.valueOf(thirdNumber)))
        }

        fun getDoubleBeforePoint(number: Double): Int {
            return Integer.parseInt(number.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        }

        fun drawTextToBitmap(mText: String, width: Int): Bitmap? {
            val paddedWidth = Math.round(width.toFloat() * 0.99f)
            try {
                val paint = Paint(1)
                paint.color = Color.rgb(LoaderCallbackInterface.INIT_FAILED, LoaderCallbackInterface.INIT_FAILED, 0)
                paint.textAlign = Align.LEFT
                var textSize = 57.0f
                val bounds = Rect()
                paint.textSize = 57.0f
                paint.getTextBounds(mText, 0, mText.length, bounds)
                if (bounds.width() > paddedWidth) {
                    do {
                        textSize -= 1.0f
                        paint.textSize = textSize
                        paint.getTextBounds(mText, 0, mText.length, bounds)
                    } while (bounds.width() > paddedWidth)
                } else if (bounds.width() < paddedWidth) {
                    do {
                        textSize += 1.0f
                        paint.textSize = textSize
                        paint.getTextBounds(mText, 0, mText.length, bounds)
                    } while (bounds.width() < paddedWidth)
                    paint.textSize = textSize - 1.0f
                    paint.getTextBounds(mText, 0, mText.length, bounds)
                }
                val bitmap = Bitmap.createBitmap(width, Math.round(bounds.height().toFloat() * 1.15f), Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawText(mText, 0.toFloat(), ((canvas.height / 2).toFloat() - (paint.descent() + paint.ascent()) / 2.0f).toInt().toFloat(), paint)
                return bitmap
            } catch (e: Exception) {
                return null
            }

        }

        fun drawTextToBitmap(text: String?, size: Float, width: Int): Bitmap? {
            var text = text
            try {
                text = text!!.trim { it <= ' ' }
                if (text == null || text.isEmpty()) {
                    return Bitmap.createBitmap(10, 10, Config.ARGB_8888)
                }
                val paint = Paint(1)
                paint.color = Color.rgb(LoaderCallbackInterface.INIT_FAILED, LoaderCallbackInterface.INIT_FAILED, 0)
                paint.textAlign = Align.LEFT
                var textSize = size
                val bounds = Rect()
                paint.textSize = textSize
                paint.getTextBounds(text, 0, text.length, bounds)
                while (bounds.width() > width) {
                    textSize -= 1.0f
                    paint.textSize = textSize
                    paint.getTextBounds(text, 0, text.length, bounds)
                }
                val bitmap = Bitmap.createBitmap(width, Math.round(bounds.height().toFloat() * 1.5f), Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawText(text, ((canvas.width - bounds.width()) / 2).toFloat(), ((canvas.height / 2).toFloat() - (paint.descent() + paint.ascent()) / 2.0f).toInt().toFloat(), paint)
                return bitmap
            } catch (e: Exception) {
                return null
            }

        }

        fun showToast(context: Context, text: CharSequence, duration: Int) {
            Handler(Looper.getMainLooper()).post { Toast.makeText(context.applicationContext, text, duration).show() }
        }

        fun getImageTime(filePath: String): String {
            try {
                val time = ExifInterface(filePath).getAttribute("DateTime")
                return SimpleDateFormat("HH:mm:ss").format(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(time))
            } catch (e: Exception) {
                return "00:00:00"
            }

        }

        fun getImageDate(filePath: String): String {
            try {
                val date = ExifInterface(filePath).getAttribute("DateTime")
                return SimpleDateFormat("dd-MM-yyyy").format(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(date))
            } catch (e: Exception) {
                return "00-00-00"
            }

        }

        fun getLargeBitmapFromFile(newestImagePath: String, width: Int, height: Int): Bitmap {
            val options = Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(newestImagePath, options)
            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(newestImagePath, options)
        }

        fun getLargeBitmapFromFile(context: Context, imageId: Int, width: Int, height: Int): Bitmap {
            val options = Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, imageId, options)
            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeResource(context.resources, imageId, options)
        }

        private fun calculateInSampleSize(options: Options, reqWidth: Int, reqHeight: Int): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        @SuppressLint("StaticFieldLeak")
        fun setImage(imageview: ImageView, newestImagePath: String) {
            if (File(newestImagePath).exists()) {
                object : AsyncTask<Void, Void, Bitmap>() {
                    internal var height: Int = 0
                    internal var width: Int = 0

                    override fun onPreExecute() {
                        super.onPreExecute()
                        this.width = imageview.measuredWidth
                        this.height = imageview.measuredHeight
                    }

                    override fun doInBackground(vararg params: Void): Bitmap {
                        return MyUtils.getLargeBitmapFromFile(newestImagePath, this.width, this.height)
                    }

                    override fun onPostExecute(bitmap: Bitmap) {
                        super.onPostExecute(bitmap)
                        imageview.setImageBitmap(bitmap)
                    }
                }.execute(*arrayOfNulls(0))
            }
        }

        @SuppressLint("StaticFieldLeak")
        fun setImage(imageview: ImageView, context: Context, imageId: Int, defaultImagePath: String) {
            object : AsyncTask<Void, Void, Bitmap>() {
                internal var height: Int = 0
                internal var width: Int = 0

                override fun onPreExecute() {
                    super.onPreExecute()
                    this.width = imageview.measuredWidth
                    this.height = imageview.measuredHeight
                }

                override fun doInBackground(vararg params: Void): Bitmap {
                    return MyUtils.getLargeBitmapFromFile(context, imageId, this.width, this.height)
                }

                override fun onPostExecute(bitmap: Bitmap) {
                    super.onPostExecute(bitmap)
                    imageview.setImageBitmap(bitmap)
                }
            }.execute(*arrayOfNulls(0))
        }
    }
}
