package com.browser4kids.ui.browser

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.browser4kids.util.AccessControlManager

/**
 * 自定义WebViewClient - 拦截URL加载,集成访问控制
 */
class CustomWebViewClient(
    private val accessControlManager: AccessControlManager,
    private val onUrlBlocked: (String) -> Unit,
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String, String?) -> Unit,
    private val onError: (Int, String) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        val scheme = request.url?.scheme?.lowercase()

        // 只处理 http/https，其他 scheme（如 baiduboxapp://、intent://）直接拦截不弹框
        if (scheme != "http" && scheme != "https") {
            return true
        }

        // 同步检查白名单和已解锁域名
        if (accessControlManager.isUrlAllowedSync(url)) {
            return false // 允许加载
        }

        // 阻止加载,通知ViewModel弹出密码框
        onUrlBlocked(url)
        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { onPageFinished(it, view?.title) }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val errorCode = error?.errorCode ?: -1
            val description = error?.description?.toString() ?: "未知错误"
            onError(errorCode, description)
        }
    }
}
