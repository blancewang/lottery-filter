package com.binwen.ssqfilter

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * 双色球缩水 V3.4 — WebView + Kotlin 原生过滤引擎
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.allowFileAccess = true
        s.allowContentAccess = true
        s.allowFileAccessFromFileURLs = true
        s.allowUniversalAccessFromFileURLs = true
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.cacheMode = WebSettings.LOAD_NO_CACHE
        s.setSupportZoom(false)
        s.builtInZoomControls = false
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.mediaPlaybackRequiresUserGesture = false
        s.javaScriptCanOpenWindowsAutomatically = true
        webView.isClickable = true
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        // 原生过滤桥：window.SsqNative.runFilter(jsonString)
        webView.addJavascriptInterface(SsqBridge(), "SsqNative")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript(
                    "(function(){try{window.__SSQ_NATIVE__=true;document.body.style.webkitUserSelect='none';}catch(e){}})();",
                    null
                )
            }
        }
        webView.webChromeClient = WebChromeClient()

        webView.loadUrl("file:///android_asset/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    /** JS 可调用：SsqNative.runFilter(configJson) → resultJson */
    class SsqBridge {
        @JavascriptInterface
        fun runFilter(configJson: String): String {
            return FilterEngine.run(configJson)
        }

        @JavascriptInterface
        fun ping(): String = "ok"
    }
}
