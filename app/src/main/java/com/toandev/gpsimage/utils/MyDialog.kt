package com.toandev.gpsimage.utils

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.content.DialogInterface.OnClickListener
import android.support.v7.app.AlertDialog

import com.toandev.demogpsimage.R

object MyDialog {
    private var mIsShow: Boolean = false

    interface AlertDialogClickEvent {
        fun onNegativeClick()

        fun onPositiveClick()
    }

    fun showAlertDialog(context: Context, title: String, message: String, clickEvent: AlertDialogClickEvent?) {
        if (!mIsShow) {
            AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(R.string.alertdialog_settings) { dialog, which ->
                MyDialog.mIsShow = false
                clickEvent?.onPositiveClick()
            }.setNegativeButton(R.string.alertdialog_cancel) { dialog, which ->
                MyDialog.mIsShow = false
                clickEvent?.onNegativeClick()
            }.setOnCancelListener {
                MyDialog.mIsShow = false
                clickEvent?.onNegativeClick()
            }.show()
            mIsShow = true
        }
    }
}
