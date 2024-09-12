package com.moviematch.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.moviematch.R

object ProgressBar {

    private var dialog: Dialog? = null

    fun showProgressBar(context: Context) {
        if (dialog == null) {
            dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
            val view = LayoutInflater.from(context).inflate(R.layout.progress_bar, null)
            dialog?.setContentView(view)
            dialog?.setCancelable(false)
        }
        dialog?.show()
    }

    fun hideProgressBar() {
        dialog?.hide()
    }

    fun dismissProgressBar() {
        dialog?.dismiss()
        dialog = null
    }
}
