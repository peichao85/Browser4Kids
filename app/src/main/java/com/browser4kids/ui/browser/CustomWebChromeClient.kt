package com.browser4kids.ui.browser

import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * 自定义WebChromeClient - 处理页面加载进度和标题更新
 */
class CustomWebChromeClient(
    private val onProgressChanged: (Int) -> Unit,
    private val onTitleChanged: (String?) -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        onTitleChanged(title)
    }
}
