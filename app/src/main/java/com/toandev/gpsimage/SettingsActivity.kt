package com.toandev.gpsimage

import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build.VERSION
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast

import com.toandev.demogpsimage.R
import com.toandev.gpsimage.utils.MyLocation
import com.toandev.gpsimage.utils.MySharedPreferences
import com.toandev.gpsimage.utils.MyUtils

import net.rdrei.android.dirchooser.DirectoryChooserConfig
import net.rdrei.android.dirchooser.DirectoryChooserFragment

import java.io.IOException

class SettingsActivity : AppCompatActivity(), OnClickListener, OnSeekBarChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {
    private var etInputFolder: EditText? = null
    private var etOutputFolder: EditText? = null
    private var etUnitInfo: EditText? = null
    private var ivMap: ImageView? = null
    private var mDialog: DirectoryChooserFragment? = null
    private var mLocation: MyLocation? = null
    private var sbImageQuality: SeekBar? = null
    private var sbMapWidth: SeekBar? = null
    private var sbMapZoom: SeekBar? = null

    private inner class MyDownloader(private val mLatitude: Double, private val mLongitude: Double, private val mZoom: Int, private val mWidth: Int, private val mHeight: Int, private val mRotate: Int) : AsyncTask<String, Bitmap, Bitmap>() {

        override fun doInBackground(vararg params: String): Bitmap? {
            try {
                return MyUtils.getMap(this.mLatitude, this.mLongitude, this.mZoom, this.mWidth, this.mHeight, this.mRotate.toFloat(), this@SettingsActivity.resources.getString(R.string.key))
            } catch (e: IOException) {
                return null
            }

        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            Log.i("hson", "onpost execute")
            if (bitmap == null) {
                Toast.makeText(this@SettingsActivity, this@SettingsActivity.getString(R.string.alert_cannot_connect_internet), Toast.LENGTH_LONG).show()
                Log.i("hson", "show toast")
                return
            }
            this@SettingsActivity.ivMap!!.setImageBitmap(bitmap)
            Log.i("hson", "show image")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        this.mLocation = MyLocation(this)
        this.etInputFolder = findViewById<View>(R.id.etInputFolder) as EditText
        this.etOutputFolder = findViewById<View>(R.id.etOutputFolder) as EditText
        this.etUnitInfo = findViewById<View>(R.id.etUnitName) as EditText
        this.sbMapWidth = findViewById<View>(R.id.sbMapWidth) as SeekBar
        this.sbMapZoom = findViewById<View>(R.id.sbMapZoom) as SeekBar
        this.sbImageQuality = findViewById<View>(R.id.sbImageQuality) as SeekBar
        this.ivMap = findViewById<View>(R.id.ivMap) as ImageView
        val pref = MySharedPreferences.getInstance(this)
        this.etInputFolder!!.setText(pref.observerFilePath)
        this.etOutputFolder!!.setText(pref.outputFilePath)
        this.etUnitInfo!!.setText(pref.unitInfo)
        this.sbMapWidth!!.progress = pref.mapWidth - 100
        this.sbMapZoom!!.progress = pref.mapZoom
        this.sbImageQuality!!.progress = pref.imageQuality
        this.etInputFolder!!.setOnClickListener(this)
        this.etOutputFolder!!.setOnClickListener(this)
        this.etUnitInfo!!.filters = arrayOf<InputFilter>(AllCaps())
        this.etUnitInfo!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == 6) {
                this@SettingsActivity.etUnitInfo!!.clearFocus()
                (this@SettingsActivity.getSystemService("input_method") as InputMethodManager).hideSoftInputFromWindow(this@SettingsActivity.etUnitInfo!!.windowToken, 0)
            }
            false
        }
        this.sbMapWidth!!.setOnSeekBarChangeListener(this)
        this.sbMapZoom!!.setOnSeekBarChangeListener(this)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val params = LayoutParams(-1, metrics.widthPixels)
        params.setMargins(10, 10, 10, 10)
        this.ivMap!!.layoutParams = params
        this.ivMap!!.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (VERSION.SDK_INT < 16) {
                    this@SettingsActivity.ivMap!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    this@SettingsActivity.ivMap!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                this@SettingsActivity.showMap()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        this.mLocation!!.requestGPSUpdate()
    }

    override fun onStop() {
        super.onStop()
        this.mLocation!!.removeUpdates()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                val pref = MySharedPreferences.getInstance(this)
                pref.observerFilePath = this.etInputFolder!!.text.toString()
                pref.outputFilePath = this.etOutputFolder!!.text.toString()
                pref.unitInfo = this.etUnitInfo!!.text.toString()
                pref.mapWidth = this.sbMapWidth!!.progress + 100
                pref.mapZoom = this.sbMapZoom!!.progress
                pref.imageQuality = this.sbImageQuality!!.progress
                val returnIntent = Intent()
                returnIntent.putExtra(MainActivity.IS_UPDATE_SERVICE, true)
                setResult(-1, returnIntent)
                Toast.makeText(this, R.string.alert_save, Toast.LENGTH_LONG).show()
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            this.mLocation!!.removeUpdates()
        }
    }

    private fun showMap() {
        MyDownloader(this.mLocation!!.latitude, this.mLocation!!.longitude, this.sbMapZoom!!.progress, this.sbMapWidth!!.progress + 100, this.ivMap!!.height / 2, this.sbImageQuality!!.progress).execute(*arrayOfNulls(0))
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        showMap()
    }

    override fun onSelectDirectory(path: String) {
        this.mDialog!!.dismiss()
        when (Integer.parseInt(this.mDialog!!.tag)) {
            R.id.etInputFolder /*2131492954*/ -> {
                this.etInputFolder!!.setText(path)
                return
            }
            R.id.etOutputFolder /*2131492956*/ -> {
                this.etOutputFolder!!.setText(path)
                return
            }
            else -> return
        }
    }

    override fun onCancelChooser() {
        this.mDialog!!.dismiss()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.etInputFolder /*2131492954*/ -> {
                showDialog(this.etInputFolder!!.text.toString(), R.id.etInputFolder.toString())
                return
            }
            R.id.etOutputFolder /*2131492956*/ -> {
                showDialog(this.etOutputFolder!!.text.toString(), R.id.etOutputFolder.toString())
                return
            }
            else -> return
        }
    }

    fun showDialog(folderPath: String, tag: String) {
        this.mDialog = DirectoryChooserFragment.newInstance(DirectoryChooserConfig.builder().newDirectoryName(getString(R.string.new_directory_name)).initialDirectory(folderPath).allowNewDirectoryNameModification(true).build())
        this.mDialog?.show(fragmentManager, tag)
    }
}
