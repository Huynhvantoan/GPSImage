package com.toandev.gpsimage

import android.app.Application
import android.util.Log
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class App : Application(){

    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCVLoader", "error")
        }
    }
}