package com.qingniu.blesdkdemopro

import android.app.Application
import android.util.Log
import com.qingniu.qnplugin.QNPlugin

/**
 *@author: hyr
 *@date: 2022/8/15 13:42
 *@desc:
 */
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val mQNPlugin = QNPlugin.getInstance(this)
        val appId = "123456789"
        val assetFileName = "123456789.qn"

        mQNPlugin.initSdk(appId, assetFileName) { code, msg ->
            Log.e("hyrrr", "$code $msg")
        }
    }
}