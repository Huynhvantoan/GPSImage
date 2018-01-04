package com.toandev.gpsimage.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat

import org.opencv.videoio.Videoio

class MyLocation @SuppressLint("WrongConstant")
constructor(private val mContext: Context) {
    var latitude: Double = 0.toDouble()
        private set
    private val mLocationListener = MyLocationListener()
    private val mLocationManager: LocationManager?
    var longitude: Double = 0.toDouble()
        private set

    private inner class MyLocationListener internal constructor() : LocationListener {

        override fun onLocationChanged(location: Location) {
            this@MyLocation.longitude = location.longitude
            this@MyLocation.latitude = location.latitude
        }

        override fun onProviderDisabled(s: String) {}

        override fun onProviderEnabled(s: String) {}

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    }

    init {
        this.mLocationManager = mContext.getSystemService("location") as LocationManager
        if (checkPermission()) {
            val gpsLocation = this.mLocationManager.getLastKnownLocation("gps")
            val netLocation = this.mLocationManager.getLastKnownLocation("network")
            if (gpsLocation != null) {
                this.longitude = gpsLocation.longitude
                this.latitude = gpsLocation.latitude
            } else if (netLocation != null) {
                this.longitude = netLocation.longitude
                this.latitude = netLocation.latitude
            }
        }
    }

    fun requestGPSUpdate() {
        if (checkPermission()) {
            this.mLocationManager!!.requestLocationUpdates("gps", 0, 0.0f, this.mLocationListener)
        }
    }

    fun requestNetworkUpdate() {
        if (checkPermission()) {
            this.mLocationManager!!.requestLocationUpdates("network", 0, 0.0f, this.mLocationListener)
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_COARSE_LOCATION") == 0
    }

    fun removeUpdates() {
        if (checkPermission() && this.mLocationManager != null && this.mLocationListener != null) {
            this.mLocationManager.removeUpdates(this.mLocationListener)
        }
    }

    protected fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            return true
        }
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > 120000
        val isSignificantlyOlder = timeDelta < -120000
        val isNewer = timeDelta > 0
        if (isSignificantlyNewer) {
            return true
        }
        if (isSignificantlyOlder) {
            return false
        }
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > Videoio.CAP_VFW
        val isFromSameProvider = isSameProvider(location.provider, currentBestLocation.provider)
        return isMoreAccurate || isNewer && !isLessAccurate || isNewer && !isSignificantlyLessAccurate && isFromSameProvider
    }

    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else {
            provider1 == provider2
        }
    }

    companion object {
        private val TWO_MINUTES = 120000
    }
}
