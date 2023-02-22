package com.milkpotatoes.studentspace

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.nfc.Tag
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.webkit.JavascriptInterface
import com.google.gson.Gson

class JsBridge {
    private var context: Context
    private val gson: Gson = Gson()
    private var mScreenInfo: Map<String, Float> = emptyMap()
    private val mConfig: Config

    constructor(context: Context) {
        this.context = context
        this.mConfig = Config(context)
    }

    private fun getResourcesValue(resourcesName: String): Int {
        val resources: Resources = this.context.resources
        val resourceId: Int = resources.getIdentifier(resourcesName, "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    //    获取顶部（Status Bar） 高度
//    @JavascriptInterface
//    fun getStatusBarHeight(): Int = getResourcesValue("status_bar_height")


    @JavascriptInterface
    fun getStatusBarHeight(): Float {
        val activity = context as Activity
        return if (activity.isInMultiWindowMode) 12.toFloat()
        else ((getResourcesValue("status_bar_height") / getDeviceScreenInfo()["density"]!! * 100.0).toInt() / 100.0).toFloat()
    }

    //    获取底部 (Navigation Bar) 高度
    @JavascriptInterface
    fun getNavigationBarHeight(): Float {
        val activity: Activity = context as Activity
        return if (activity.isInMultiWindowMode) 12.toFloat()
        else ((getResourcesValue("navigation_bar_height") / getDeviceScreenInfo()["density"]!! * 100.0).toInt() / 100.0).toFloat()

    }

    @Suppress("DEPRECATION")
    private fun getDeviceScreenInfo(): Map<String, Float> {
        if (mScreenInfo.isEmpty()) {
            val dm = DisplayMetrics()
            val windowManager: WindowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(dm)
            mScreenInfo = mapOf("density" to dm.density, "xdpi" to dm.xdpi, "ydpi" to dm.ydpi)
//        mScreenInfo["density"]
        }
        return mScreenInfo
    }

    @JavascriptInterface
    fun getScreenInfo(): String = gson.toJson(getDeviceScreenInfo())

    @JavascriptInterface
    fun isNightMode(): Boolean {
        Log.d(TAG, mConfig.isNightMode.toString())
        return mConfig.isNightMode
    }

    @JavascriptInterface
    fun setDayNightMode(mode: Int) {
        when (mode) {
            1 -> mConfig.nightMode = Configuration.UI_MODE_NIGHT_YES
            0 -> mConfig.nightMode = Configuration.UI_MODE_NIGHT_NO
            -1 -> mConfig.nightMode = Configuration.UI_MODE_NIGHT_UNDEFINED
        }
        val ma = context as MainActivity
        ma.runOnUiThread {
            ma.hideNavigationBar(true, mConfig.isNightMode)
        }
    }

    @JavascriptInterface
    fun enableUSBDebug(debug: Boolean) {
        val ma = context as MainActivity
        ma.runOnUiThread { ma.enableWebViewDebug(debug) }
        mConfig.debugOverUSBEnabled = debug
    }

    @JavascriptInterface
    fun getDebugStatus(): Boolean {
        return mConfig.debugOverUSBEnabled
    }

    @JavascriptInterface
    fun dismissBootOverlay() {
        val ma = context as MainActivity
        ma.runOnUiThread { ma.dismissBootOverlay() }
    }
}