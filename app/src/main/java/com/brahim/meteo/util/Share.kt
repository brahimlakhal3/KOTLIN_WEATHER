package com.brahim.meteo.util

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes

fun Context.shareText(text: String, @StringRes chooserTitle: Int) {
  val send =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
      }
  startActivity(Intent.createChooser(send, getString(chooserTitle)))
}
