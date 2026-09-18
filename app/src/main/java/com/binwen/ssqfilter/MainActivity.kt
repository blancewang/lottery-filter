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

        // Prefer complete assembled HTML from parts if present and looks valid
        val fromParts = tryLoadParts()
        if (fromParts != null && fromParts.contains("</script>") && fromParts.contains("开始过滤")) {
            webView.loadDataWithBaseURL("file:///android_asset/", fromParts, "text/html", "UTF-8", null)
        } else {
            webView.loadUrl("file:///android_asset/index.html")
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun tryLoadParts(): String? {
        return try {
            val names = assets.list("parts")?.filter { it.endsWith(".txt") }?.sorted().orEmpty()
            if (names.isEmpty()) return null
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
        } catch (_: Exception) {
            null
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
