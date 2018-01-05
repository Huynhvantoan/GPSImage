package com.toandev.gpsimage

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import com.toandev.demogpsimage.BuildConfig
import com.toandev.demogpsimage.R
import com.toandev.gpsimage.utils.MyDialog
import com.toandev.gpsimage.utils.MySharedPreferences
import com.toandev.gpsimage.utils.MyUtils
import com.toandev.gpsimage.utils.RPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(){
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            this@MainActivity.mService = (service as GPSImageService.LocalBinder).service
            this@MainActivity.mService?.connectedActivity = this@MainActivity
            if (this@MainActivity.mIsUpdateService) {
                this@MainActivity.mIsUpdateService = false
                this@MainActivity.mService?.updateService(this@MainActivity.mPref?.observerFilePath!!)
            }
            Log.i("hson", "service connected")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.i("hson", "service disconnected")
            this@MainActivity.mService = null
        }
    }
    private var isServiceStarted = true
    private var mIsBound: Boolean = false
    private var mIsUpdateService: Boolean = false
    private var mPref: MySharedPreferences? = null
    private var mService: GPSImageService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RPermission.instance.checkPermission(this, arrayOf(Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE))
        setContentView(R.layout.activity_main)
        this.mPref = MySharedPreferences.getInstance(this)
        initGPSImage()
        showEnableDeviceDialogs()
        mBtnStart.isChecked=isServiceStarted
        setBtnStartImage(isServiceStarted)
        Log.i("hson", "Activity create")
        mBtnStart.setOnCheckedChangeListener({ _, _ ->
            isServiceStarted=!isServiceStarted
            setBtnStartImage(isServiceStarted)
        })
    }

    private fun checkService(isServiceStarted: Boolean){
        if (isServiceStarted) {
            startService()
        } else {
            stopService()
        }
    }
    private fun initGPSImage() {
        this.mIvGPSImage?.setOnClickListener {
            try {
                val imagePath = this@MainActivity.mPref!!.newestImagePath
                if (imagePath != null && !imagePath.isEmpty()) {
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    // intent.setDataAndType(Uri.fromFile(File(imagePath)), "image/*")
                    intent.setDataAndType(FileProvider.getUriForFile(this@MainActivity,
                            BuildConfig.APPLICATION_ID + ".provider", File(imagePath)), "image/*")
                    this@MainActivity.startActivity(intent)
                }
            }catch (e:Exception){e.printStackTrace()}
        }
    }

    private fun showEnableDeviceDialogs() {
        if (MyUtils.isNetworkAvailable(this)) {
            showGPSDialog()
        } else {
            MyDialog.showAlertDialog(this, getString(R.string.alert_network_title), getString(R.string.alert_network_message), object : MyDialog.AlertDialogClickEvent {
                override fun onNegativeClick() {
                    this@MainActivity.showGPSDialog()
                }

                override fun onPositiveClick() {
                    this@MainActivity.startActivity(Intent("android.settings.WIRELESS_SETTINGS"))
                    this@MainActivity.showGPSDialog()
                }
            })
        }
    }

    private fun showGPSDialog() {
        if (!MyUtils.isLocationEnabled(this)) {
            MyDialog.showAlertDialog(this, getString(R.string.alert_gps_title), getString(R.string.alert_gps_message), object : MyDialog.AlertDialogClickEvent {
                override fun onPositiveClick() {
                    this@MainActivity.startActivity(Intent("android.settings.LOCATION_SOURCE_SETTINGS"))
                }
                override fun onNegativeClick() {}
            })
        }
    }

    override fun onStart() {
        super.onStart()
        doBindService()
        Log.i("hson", "Activity start")
    }

    override fun onPause() {
        super.onPause()
        Log.i("hson", "Activity pause")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings /*2131492998*/ -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_ACTIVITY)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.apply {
            if (requestCode == SETTINGS_ACTIVITY && resultCode == -1 && getBooleanExtra(IS_UPDATE_SERVICE, false) && isServiceStarted) {
                mIsUpdateService = true
            }
        }
    }

    private fun setBtnStartImage(isServiceStarted: Boolean) {
        checkService(isServiceStarted)
        if (isServiceStarted) {
            this.mTvGPSImageRunning?.visibility = View.VISIBLE
            this.mTvGPSImageStopped?.visibility = View.INVISIBLE
            return
        }
        this.mTvGPSImageRunning?.visibility = View.INVISIBLE
        this.mTvGPSImageStopped?.visibility = View.VISIBLE
    }

    private fun startService() {
        startService(Intent(this, GPSImageService::class.java))
    }

    private fun stopService() {
        this.mService?.apply {
            stopForeground(true)
            stopSelf()
        }
    }

    private fun doBindService() {
        bindService(Intent(this, GPSImageService::class.java), this.mConnection, SETTINGS_ACTIVITY)
        this.mIsBound = true
    }

    private fun doUnbindService() {
        if (this.mIsBound) {
            unbindService(this.mConnection)
            this.mIsBound = false
        }
    }

    override fun onStop() {
        super.onStop()
        doUnbindService()
        Log.i("hson", "Activity stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("hson", "Activity destroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("hson", "Activity restart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("hson", "Activity resume")
    }

    fun updateGPSImageView(outputPath: String) {
        MyUtils.setImage(this.mIvGPSImage!!, outputPath)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        MyUtils.setImage(this.mIvGPSImage!!, this.mPref?.newestImagePath!!)
        Log.i("hson", "Activity onWindowFocusChanged")
    }

    companion object {
        val IS_UPDATE_SERVICE = "com.hson.gpsimage.activities.MainActivity.IS_UPDATE_SERVICE"
        private val SETTINGS_ACTIVITY = 1
    }
}
