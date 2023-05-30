package com.volio.vn.callscreen.receivers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.volio.vn.callscreen.BuildConfig

fun getStartIntent(context: Context): Intent {
    // Let the configure Button show the Log
    val openAppIntent = Intent()
    openAppIntent.component = ComponentName(context, "com.volio.vn.b1_project.ui.call.CallActivity")
    openAppIntent.flags =
        Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    return openAppIntent
}