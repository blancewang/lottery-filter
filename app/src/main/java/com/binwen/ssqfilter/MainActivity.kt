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
 * 双色球缩水 — WebView 加载 assets/index.html
 * 若存在 assets/parts/p*.txt，启动时按文件名排序拼接后加载。
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
        s.useWideViewPort = true
        s.loadWithOverviewMode = true

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        val html = loadBundledHtml()
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun loadBundledHtml(): String {
        val am = assets
        val names = (am.list("parts") ?: emptyArray()).filter { it.endsWith(".txt") }.sorted()
        if (names.isNotEmpty()) {
            val sb = StringBuilder()
            for (name in names) {
                am.open("parts/$name").use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { sb.append(it.readText()) }
                }
            }
            return sb.toString()
        }
        return am.open("index.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
