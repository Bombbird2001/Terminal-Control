package com.bombbird.terminalcontrol

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.bombbird.terminalcontrol.utilities.BrowserInterface

class AndroidBrowserOpener(private val activity: Activity): BrowserInterface {
    override fun openBrowser(link: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
        }

        activity.startActivity(intent)
    }
}