package com.toandev.gpsimage.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment

import com.toandev.demogpsimage.BuildConfig

class MySharedPreferences private constructor() {

    var mapZoom: Int
        get() = mMapZoom
        set(mapZoom) {
            mMapZoom = mapZoom
            mPref?.apply {
                val editor =edit()
                editor.putInt(MAP_ZOOM, mMapZoom)
                editor.apply()
            }
        }

    var mapWidth: Int
        get() = mMapWidth
        set(mapWidth) {
            mMapWidth = mapWidth
            mPref?.apply {
                val editor =edit()
                editor.putInt(MAP_WIDTH, mMapWidth)
                editor.apply()
            }
        }

    var observerFilePath: String?
        get() = mObserverFilePath
        set(observerFilePath) {
            mObserverFilePath = observerFilePath
            mPref?.apply {
                val editor =edit()
                editor.putString(OBSERVER_FILE_PATH, mObserverFilePath)
                editor.apply()
            }
        }

    var outputFilePath: String?
        get() = mOutputFilePath
        set(outputFilePath) {
            mOutputFilePath = outputFilePath
            mPref?.apply {
                val editor =edit()
                editor.putString(OUTPUT_FILE_PATH, mOutputFilePath)
                editor.apply()
            }
        }

    var isServiceStarted: Boolean
        get() = mIsServiceStarted
        set(isServiceStarted) {
            mIsServiceStarted = isServiceStarted
            mPref?.apply {
                val editor =edit()
                editor.putBoolean(IS_SERVICE_STARTED, mIsServiceStarted)
                editor.apply()
            }
        }

    var imageQuality: Int
        get() = mImageQuality
        set(quality) {
            mImageQuality = quality
            mPref?.apply {
                val editor =edit()
                editor.putInt(IMAGE_QUALITY, mImageQuality)
                editor.apply()
            }
        }

    var newestImagePath: String?
        get() = mNewestImagePath
        set(newestImagePath) {
            mNewestImagePath = newestImagePath
            mPref?.apply {
                val editor =edit()
                editor.putString(NEWEST_IMAGE_PATH, mNewestImagePath)
                editor.apply()
            }
        }

    var unitInfo: String?
        get() = mUnitInfo
        set(unitInfo) {
            mUnitInfo = unitInfo
            mPref?.apply {
                val editor = edit()
                editor.putString(UNIT_INFO, mUnitInfo)
                editor.apply()
            }
        }

    companion object {
        private val IMAGE_QUALITY = "com.hson.gpsimage.MySharedPreferences.IMAGE_QUALITY"
        private var INSTANCE: MySharedPreferences? = null
        private val IS_SERVICE_STARTED = "com.hson.gpsimage.MySharedPreferences.IS_SERVICE_STARTED"
        private val MAP_WIDTH = "com.hson.gpsimage.MySharedPreferences.MAP_WIDTH"
        private val MAP_ZOOM = "com.hson.gpsimage.MySharedPreferences.MAP_ZOOM"
        private val MIN_MAP_WIDTH = 100
        private val NEWEST_IMAGE_PATH = "com.hson.gpsimage.MySharedPreferences.NEWEST_IMAGE_PATH"
        private val OBSERVER_FILE_PATH = "com.hson.gpsimage.MySharedPreferences.OBSERVER_FILE_PATH"
        private val OUTPUT_FILE_PATH = "com.hson.gpsimage.MySharedPreferences.OUTPUT_FILE_PATH"
        private val UNIT_INFO = "com.hson.gpsimage.MySharedPreferences.UNIT_INFO"
        private var mImageQuality: Int = 0
        private var mIsServiceStarted: Boolean = false
        private var mMapWidth: Int = 0
        private var mMapZoom: Int = 0
        private var mNewestImagePath: String? = null
        private var mObserverFilePath: String? = null
        private var mOutputFilePath: String? = null
        private var mPref: SharedPreferences? = null
        private var mUnitInfo: String? = null

        fun getInstance(context: Context): MySharedPreferences {
            if (INSTANCE == null) {
                INSTANCE = MySharedPreferences()
                mPref = context.getSharedPreferences("gpsimage-preferences", 0)
                mPref?.apply {
                    mIsServiceStarted = getBoolean(IS_SERVICE_STARTED, false)
                    mObserverFilePath = getString(OBSERVER_FILE_PATH, Environment.getExternalStorageDirectory().toString() + "/Download")
                    mOutputFilePath = getString(OUTPUT_FILE_PATH, Environment.getExternalStorageDirectory().toString() + "/GPSImage")
                    mImageQuality = getInt(IMAGE_QUALITY, 90)
                    mMapZoom = getInt(MAP_ZOOM, 15)
                    mMapWidth = getInt(MAP_WIDTH, MIN_MAP_WIDTH)
                    mNewestImagePath = getString(NEWEST_IMAGE_PATH, BuildConfig.FLAVOR)
                    mUnitInfo = getString(UNIT_INFO, "C\u00d4NG TY \u0110\u1ee8C VI\u1ec6T")
                }
            }
            return INSTANCE as MySharedPreferences
        }
    }
}
