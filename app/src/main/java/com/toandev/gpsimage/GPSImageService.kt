package com.toandev.gpsimage

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

import com.toandev.demogpsimage.R
import com.toandev.gpsimage.utils.ImageProcessor
import com.toandev.gpsimage.utils.MyFileObserver
import com.toandev.gpsimage.utils.MyLocation
import com.toandev.gpsimage.utils.MyUtils

class GPSImageService : Service() {
    private val mBinder = LocalBinder()
    var connectedActivity: MainActivity? = null
    private var mFileObserver: MyFileObserver? = null
    private var mImageProcessor: ImageProcessor? = null
    private var mLocation: MyLocation? = null

    inner class LocalBinder : Binder() {
        val service: GPSImageService
            get() = this@GPSImageService
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("hson", "service oncreate call")
        this.mLocation = MyLocation(this)
        this.mLocation?.requestGPSUpdate()
        this.mImageProcessor = ImageProcessor(this)
        this.mFileObserver = object : MyFileObserver(this) {
            override fun processFile(filePath: String) {
                super.processFile(filePath)
                val processor = this@GPSImageService.mImageProcessor
                processor?.process(ImageProcessor.ImageData(filePath, this@GPSImageService.mLocation!!.latitude, this@GPSImageService.mLocation!!.longitude, MyUtils.getImageTime(filePath), MyUtils.getImageDate(filePath)))
            }
        }
        this.mFileObserver?.startWatching()
    }

    override fun onStartCommand(intent: Intent, i: Int, j: Int): Int {
        showNotification()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        this.mLocation?.removeUpdates()
        this.mFileObserver?.stopWatching()
        Log.i("hson", "service onDestroy call")
    }

    override fun onBind(intent: Intent): IBinder? {
        return this.mBinder
    }

    private fun showNotification() {
        startForeground(1, Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_location_marker_flat)
                .setTicker(getString(R.string.noti_status_text))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.noti_content_title))
                .setContentText(getString(R.string.noti_content_text))
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)).build())
    }

    fun updateService(observerPath: String) {
        this.mFileObserver?.updateObserverPath(observerPath)
    }
}
