package com.toandev.gpsimage.utils

import android.util.Log

object MySpeedTest {
    private var mStart: Long = 0

    fun start() {
        mStart = System.currentTimeMillis()
    }

    fun stop(text: String) {
        Log.i("hson", "Time: " + (System.currentTimeMillis() - mStart) + "---" + text)
    }
}
