package com.toandev.gpsimage

import android.app.Application
import android.util.Log
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class App : Application(),LoaderCallbackInterface{

    override fun onCreate() {
        super.onCreate()
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,this)) {
            Log.i("OpenCVLoader", "error")
        }
    }

    override fun onManagerConnected(status: Int) {
        Log.d("onManagerConnected=",status.toString())
    }

    override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {
        Log.d("onPackageInstall=",operation.toString())
    }

}