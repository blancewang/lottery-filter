package com.binwen.ssqfilter

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 双色球缩水 V3.4 — 启动时拼接 assets/parts 为完整 HTML
 * 缩水只减少注数，不提高单注中奖概率。
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
        s.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.setSupportZoom(false)
        s.builtInZoomControls = false
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.textZoom = 100

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        val html = loadBundledHtml()
        webView.loadDataWithBaseURL(
            "file:///android_asset/",
            html,
            "text/html",
            "utf-8",
            null
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun loadBundledHtml(): String {
        val am = assets
        val names = (am.list("parts") ?: emptyArray()).sorted()
        if (names.isEmpty()) {
            return am.open("index.html").bufferedReader().use { it.readText() }
        }
        val sb = StringBuilder()
        for (name in names) {
            am.open("parts/$name").use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    sb.append(reader.readText())
                }
            }
        }
        return sb.toString()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
