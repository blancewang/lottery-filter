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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * 双色球缩水 V3.4 — 优先拼接 assets/parts 得到完整 HTML，避免大文件损坏。
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

        val html = loadBundledHtml()
        webView.loadDataWithBaseURL(
            "file:///android_asset/",
            html,
            "text/html",
            "UTF-8",
            null
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    /** 优先按序拼接 assets/parts/p00.txt…，否则读 index.html */
    private fun loadBundledHtml(): String {
        return try {
            val names = assets.list("parts")?.filter { it.endsWith(".txt") }?.sorted().orEmpty()
            if (names.isNotEmpty()) {
                val sb = StringBuilder()
                for (name in names) {
                    assets.open("parts/$name").use { ins ->
                        BufferedReader(InputStreamReader(ins, StandardCharsets.UTF_8)).use { br ->
                            var line: String?
                            while (br.readLine().also { line = it } != null) {
                                sb.append(line).append('\n')
                            }
                        }
                    }
                }
                sb.toString()
            } else {
                assets.open("index.html").use { ins ->
                    ins.bufferedReader(StandardCharsets.UTF_8).readText()
                }
            }
        } catch (e: Exception) {
            "<html><body style='background:#0b0f14;color:#e8eaed;padding:16px'>加载失败: ${e.message}</body></html>"
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    class SsqBridge {
        @JavascriptInterface
        fun runFilter(configJson: String): String = FilterEngine.run(configJson)

        @JavascriptInterface
        fun ping(): String = "ok"
    }
}
