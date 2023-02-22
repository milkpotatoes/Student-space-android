package com.milkpotatoes.studentspace

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import com.milkpotatoes.studentspace.databinding.ActivityFullscreenBinding





/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var mWebView: WebView
    private var serverPort: Int = 0
    private lateinit var mJsBridge: JsBridge
    private val context = this
    private lateinit var mConfig: Config
    private lateinit var mFileServer: FileServer
    val ENABLE_WEBVIEW_DEBUG = "ENABLE_WEBVIEW_DEBUG"

    @Suppress("DEPRECATION")
    fun hideNavigationBar(hideNavigationBar: Boolean, nightMode: Boolean) {
        Log.d(TAG, "hideNavigationBar, nightMode = $nightMode")
        if (hideNavigationBar) {
            if (nightMode)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            else
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mJsBridge = JsBridge(this)
        mConfig = Config((this))
        mFileServer = FileServer(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            hideNavigationBar(true, mConfig.isNightMode)
            window.statusBarColor = android.R.color.transparent
            window.navigationBarColor = android.R.color.transparent
        }


        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalLayoutUtils(this)
        initWebView()

        supportActionBar?.hide()


        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
//        toastMessage(appLinkAction + appLinkData)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val appLinkAction = intent?.action
        val appLinkData = intent?.data
        Log.d(TAG, appLinkAction.toString())
//        toastMessage(appLinkAction + appLinkData)
    }

    @SuppressLint("ServiceCast")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (mConfig.nightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                Log.d(TAG, "UI_MODE_NIGHT_YES ${mConfig.nightMode.toString()}")
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES)
//                hideNavigationBar(true, nightMode = false)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                Log.d(TAG, "UI_MODE_NIGHT_NO ${mConfig.nightMode}")
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO)
//                hideNavigationBar(true, nightMode = true)

            }
            else -> {
                Log.d(TAG, "ELSE ${mConfig.nightMode}")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        hideNavigationBar(true, mConfig.isNightMode)

        if (mConfig.nightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) {
            Log.d(TAG, "Night Mode is auto")
            mWebView.evaluateJavascript(
                "fitDayNightMode(undefined)",
                null
            )
        }
    }

    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openWebsite(url: Uri) {
//        val mCustomTabsIntent:
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, url)
    }

    @SuppressLint("IntentReset")
    private fun openScheme(uri: Uri) {
        when (uri.scheme) {
            "mailto" -> {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SENDTO, uri), "发送邮件"))
            }
            "tel" -> {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_DIAL, uri), "拨打电话"))
            }
            "sms" -> {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.type = "vnd.android-dir/mms-sms"
                startActivity(intent)
            }
            "blob" -> {
                return
            }
            else -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: Exception) {
                    toastMessage("打开链接失败")
                }
            }
        }
    }

    fun enableWebViewDebug(debug: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.run {
                Log.d(TAG, "trying to switch webview debug status: ${debug.toString()}")
                setWebContentsDebuggingEnabled(debug)
            }
        }
    }

    fun dismissBootOverlay() {
        val bootOverlay = findViewById<ConstraintLayout>(R.id.boot_overlay)
        bootOverlay.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    private fun initWebView() {
        mWebView = findViewById(R.id.app_webview)
        enableWebViewDebug(mConfig.debugOverUSBEnabled)

        //下面这些直接复制就好
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                super.shouldOverrideUrlLoading(view, request)
                return if (request != null) {
                    if (request.url.host == "stusp.localhost") {
                        false
                    } else {
                        if ((request.url.scheme == "http") or (request.url.scheme == "https")) openWebsite(
                            request.url
                        )
                        else openScheme(request.url)
                        true
                    }
                } else true
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?,
            ): WebResourceResponse? {
                val url = request!!.url
                val path = url.path
//                Log.d(TAG, url?.path.toString())
                if (url.host == "stusp.localhost")
                    return when (path) {
                        "/" -> {
                            mFileServer.assetsWeb("/index.html")
                        }
                        "/apiv2/answercard/" -> {
                            url.encodedQuery?.let { mFileServer.answerCardCache(it) }
                        }
                        else -> {
                            mFileServer.assetsWeb(url?.path.toString())
                        }
                    }

                return super.shouldInterceptRequest(view, request)
            }
        }

        var mWebSettings = mWebView.settings
        mWebSettings.javaScriptEnabled = true  // 开启 JavaScript 交互
        mWebSettings.setAppCacheEnabled(false) // 启用或禁用缓存
        mWebSettings.cacheMode = WebSettings.LOAD_NO_CACHE // 只要缓存可用就加载缓存, 哪怕已经过期失效 如果缓存不可用就从网络上加载数据
//        mWebSettings.setAppCachePath(cacheDir.path) // 设置应用缓存路径

        // 缩放操作
        mWebSettings.setSupportZoom(false) // 支持缩放 默认为true 是下面那个的前提
        mWebSettings.builtInZoomControls = false // 设置内置的缩放控件 若为false 则该WebView不可缩放
        mWebSettings.displayZoomControls = true // 隐藏原生的缩放控件

        mWebSettings.blockNetworkImage = false // 禁止或允许WebView从网络上加载图片
        mWebSettings.loadsImagesAutomatically = true // 支持自动加载图片

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWebSettings.safeBrowsingEnabled = true // 是否开启安全模式
        }

        mWebSettings.javaScriptCanOpenWindowsAutomatically = false // 支持通过JS打开新窗口
        mWebSettings.domStorageEnabled = true // 启用或禁用DOM缓存
        mWebSettings.setSupportMultipleWindows(false) // 设置WebView是否支持多窗口

        // 设置自适应屏幕, 两者合用
        mWebSettings.useWideViewPort = true  // 将图片调整到适合webview的大小
        mWebSettings.loadWithOverviewMode = true  // 缩放至屏幕的大小
        mWebSettings.allowFileAccess = true // 设置可以访问文件

        mWebSettings.setGeolocationEnabled(false) // 是否使用地理位置

        mWebView.addJavascriptInterface(mJsBridge, "stusp")
//        mWebView.
        mWebView.fitsSystemWindows = true
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        mWebView.loadUrl("https://stusp.localhost/#/home")

    }

    //设置返回键的监听
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (mWebView.canGoBack()) {
                mWebView.goBack()  //返回上一个页面
                true
            } else {
                finish()
                true
            }
        }
        return false
    }

    @SuppressLint("ServiceCast")
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
//        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

        mWebView.evaluateJavascript(
            "fitSystemConfig()",
            null
        )
    }
}

