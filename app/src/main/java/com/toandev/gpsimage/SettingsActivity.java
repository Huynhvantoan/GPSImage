package com.toandev.gpsimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputFilter.AllCaps;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.toandev.demogpsimage.R;
import com.toandev.gpsimage.utils.MyLocation;
import com.toandev.gpsimage.utils.MySharedPreferences;
import com.toandev.gpsimage.utils.MyUtils;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity implements OnClickListener, OnSeekBarChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {
    private EditText etInputFolder;
    private EditText etOutputFolder;
    private EditText etUnitInfo;
    private ImageView ivMap;
    private DirectoryChooserFragment mDialog;
    private MyLocation mLocation;
    private SeekBar sbImageQuality;
    private SeekBar sbMapWidth;
    private SeekBar sbMapZoom;

    private class MyDownloader extends AsyncTask<String, Bitmap, Bitmap> {
        private int mHeight;
        private double mLatitude;
        private double mLongitude;
        private int mRotate;
        private int mWidth;
        private int mZoom;

        public MyDownloader(double lat, double lon, int zoom, int mapWidth, int mapHeight, int rotate) {
            this.mLongitude = lon;
            this.mLatitude = lat;
            this.mZoom = zoom;
            this.mWidth = mapWidth;
            this.mHeight = mapHeight;
            this.mRotate = rotate;
        }

        protected Bitmap doInBackground(String... params) {
            try {
                return MyUtils.getMap(this.mLatitude, this.mLongitude, this.mZoom, this.mWidth, this.mHeight, (float) this.mRotate, SettingsActivity.this.getResources().getString(R.string.key));
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.i("hson", "onpost execute");
            if (bitmap == null) {
                Toast.makeText(SettingsActivity.this, SettingsActivity.this.getString(R.string.alert_cannot_connect_internet), Toast.LENGTH_LONG).show();
                Log.i("hson", "show toast");
                return;
            }
            SettingsActivity.this.ivMap.setImageBitmap(bitmap);
            Log.i("hson", "show image");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        this.mLocation = new MyLocation(this);
        this.etInputFolder = (EditText) findViewById(R.id.etInputFolder);
        this.etOutputFolder = (EditText) findViewById(R.id.etOutputFolder);
        this.etUnitInfo = (EditText) findViewById(R.id.etUnitName);
        this.sbMapWidth = (SeekBar) findViewById(R.id.sbMapWidth);
        this.sbMapZoom = (SeekBar) findViewById(R.id.sbMapZoom);
        this.sbImageQuality = (SeekBar) findViewById(R.id.sbImageQuality);
        this.ivMap = (ImageView) findViewById(R.id.ivMap);
        MySharedPreferences pref = MySharedPreferences.Companion.getInstance(this);
        this.etInputFolder.setText(pref.getObserverFilePath());
        this.etOutputFolder.setText(pref.getOutputFilePath());
        this.etUnitInfo.setText(pref.getUnitInfo());
        this.sbMapWidth.setProgress(pref.getMapWidth() - 100);
        this.sbMapZoom.setProgress(pref.getMapZoom());
        this.sbImageQuality.setProgress(pref.getImageQuality());
        this.etInputFolder.setOnClickListener(this);
        this.etOutputFolder.setOnClickListener(this);
        this.etUnitInfo.setFilters(new InputFilter[]{new AllCaps()});
        this.etUnitInfo.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 6) {
                    SettingsActivity.this.etUnitInfo.clearFocus();
                    ((InputMethodManager) SettingsActivity.this.getSystemService("input_method")).hideSoftInputFromWindow(SettingsActivity.this.etUnitInfo.getWindowToken(), 0);
                }
                return false;
            }
        });
        this.sbMapWidth.setOnSeekBarChangeListener(this);
        this.sbMapZoom.setOnSeekBarChangeListener(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LayoutParams params = new LayoutParams(-1, metrics.widthPixels);
        params.setMargins(10, 10, 10, 10);
        this.ivMap.setLayoutParams(params);
        this.ivMap.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (VERSION.SDK_INT < 16) {
                    SettingsActivity.this.ivMap.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    SettingsActivity.this.ivMap.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                SettingsActivity.this.showMap();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        this.mLocation.requestGPSUpdate();
    }

    protected void onStop() {
        super.onStop();
        this.mLocation.removeUpdates();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save :
                MySharedPreferences pref = MySharedPreferences.Companion.getInstance(this);
                pref.setObserverFilePath(this.etInputFolder.getText().toString());
                pref.setOutputFilePath(this.etOutputFolder.getText().toString());
                pref.setUnitInfo(this.etUnitInfo.getText().toString());
                pref.setMapWidth(this.sbMapWidth.getProgress() + 100);
                pref.setMapZoom(this.sbMapZoom.getProgress());
                pref.setImageQuality(this.sbImageQuality.getProgress());
                Intent returnIntent = new Intent();
                returnIntent.putExtra(MainActivity.Companion.getIS_UPDATE_SERVICE(), true);
                setResult(-1, returnIntent);
                Toast.makeText(this, R.string.alert_save, Toast.LENGTH_LONG).show();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            this.mLocation.removeUpdates();
        }
    }

    private void showMap() {
        new MyDownloader(this.mLocation.getLatitude(), this.mLocation.getLongitude(), this.sbMapZoom.getProgress(), this.sbMapWidth.getProgress() + 100, this.ivMap.getHeight() / 2, this.sbImageQuality.getProgress()).execute(new String[0]);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        showMap();
    }

    public void onSelectDirectory(@NonNull String path) {
        this.mDialog.dismiss();
        switch (Integer.parseInt(this.mDialog.getTag())) {
            case R.id.etInputFolder /*2131492954*/:
                this.etInputFolder.setText(path);
                return;
            case R.id.etOutputFolder /*2131492956*/:
                this.etOutputFolder.setText(path);
                return;
            default:
                return;
        }
    }

    public void onCancelChooser() {
        this.mDialog.dismiss();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etInputFolder /*2131492954*/:
                showDialog(this.etInputFolder.getText().toString(), String.valueOf(R.id.etInputFolder));
                return;
            case R.id.etOutputFolder /*2131492956*/:
                showDialog(this.etOutputFolder.getText().toString(), String.valueOf(R.id.etOutputFolder));
                return;
            default:
                return;
        }
    }

    public void showDialog(String folderPath, String tag) {
        this.mDialog = DirectoryChooserFragment.newInstance(DirectoryChooserConfig.builder().newDirectoryName(getString(R.string.new_directory_name)).initialDirectory(folderPath).allowNewDirectoryNameModification(true).build());
        this.mDialog.show(getFragmentManager(), tag);
    }
}
