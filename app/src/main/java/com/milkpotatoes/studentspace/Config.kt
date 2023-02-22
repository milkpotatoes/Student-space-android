package com.milkpotatoes.studentspace

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log

class Config {
    private val CONFIG_PREFERERENCE_NAME = "config"
    private val configs: SharedPreferences
    private val context: Context
    private val NIGHT_MODE_CONFIG = "DarkMode"
    private val DEBUG_OVER_USB = "DebugOverUSB"

    constructor(context: Context) {
        this.context = context
        this.configs =
            this.context.getSharedPreferences(CONFIG_PREFERERENCE_NAME, Context.MODE_PRIVATE)
        nightMode = this.configs.getInt(NIGHT_MODE_CONFIG, Configuration.UI_MODE_NIGHT_UNDEFINED)
        debugOverUSBEnabled = this.configs.getBoolean(DEBUG_OVER_USB, false)
    }

    var nightMode: Int = 0
        set(value) {
            if (value == Configuration.UI_MODE_NIGHT_NO || value == Configuration.UI_MODE_NIGHT_UNDEFINED || value == Configuration.UI_MODE_NIGHT_YES) {
                field = value
                configs.edit().putInt(NIGHT_MODE_CONFIG, value).commit()
            }
        }
        get() {
            return this.configs.getInt(NIGHT_MODE_CONFIG, Configuration.UI_MODE_NIGHT_UNDEFINED)
        }

    var isNightMode: Boolean = false
        get() {
            return if (nightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) {
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            } else {
                nightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
        }

    var debugOverUSBEnabled: Boolean = false
        set(value) {
            configs.edit().putBoolean(DEBUG_OVER_USB, value).commit()
            field = value
        }
}
