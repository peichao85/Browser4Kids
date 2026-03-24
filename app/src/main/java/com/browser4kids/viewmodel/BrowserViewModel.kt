package com.browser4kids.viewmodel

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.browser4kids.Browser4KidsApplication
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.repository.AccessLogRepository
import com.browser4kids.repository.BrowsingHistoryRepository
import com.browser4kids.repository.SettingsRepository
import com.browser4kids.repository.WhitelistRepository
import com.browser4kids.util.AccessControlManager
import com.browser4kids.util.UrlMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrowserUiState(
    val currentUrl: String = "",
    val pageTitle: String? = null,
    val loadingProgress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val blockedUrl: String? = null,
    val errorState: BrowserError? = null,
    val showHomePage: Boolean = true
)

data class BrowserError(
    val errorCode: Int,
    val description: String
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as Browser4KidsApplication
    private val whitelistRepository = WhitelistRepository(app.database.whitelistDao())
    private val accessLogRepository = AccessLogRepository(app.database.accessLogDao())
    private val browsingHistoryRepository = BrowsingHistoryRepository(app.database.browsingHistoryDao())
    val settingsRepository = SettingsRepository(application)
    val accessControlManager = AccessControlManager(whitelistRepository)

    init {
        // 初始化时加载白名单规则缓存
        viewModelScope.launch {
            accessControlManager.refreshRulesCache()
        }
    }

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val whitelistRules = whitelistRepository.getAllRules()

    private var webView: WebView? = null

    fun setWebView(webView: WebView) {
        this.webView = webView
    }

    /**
     * 用户在地址栏输入URL后尝试导航
     */
    fun navigateToUrl(input: String) {
        val url = if (input.startsWith("http://") || input.startsWith("https://")) {
            input
        } else {
            "https://$input"
        }
        checkAndLoadUrl(url)
    }

    /**
     * 检查URL是否允许访问,允许则加载,否则弹出密码框
     */
    fun checkAndLoadUrl(url: String) {
        viewModelScope.launch {
            val allowed = accessControlManager.isUrlAllowed(url)
            if (allowed) {
                loadUrl(url)
            } else {
                _uiState.value = _uiState.value.copy(
                    showPasswordDialog = true,
                    blockedUrl = url
                )
            }
        }
    }

    /**
     * 直接加载URL(已通过验证)
     */
    fun loadUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            showHomePage = false,
            errorState = null,
            showPasswordDialog = false,
            blockedUrl = null
        )
        webView?.loadUrl(url)
    }

    /**
     * 密码验证成功后解锁当前被拦截的URL
     * @param durationMinutes 授权时长(分钟)，0表示永久添加到白名单
     */
    fun onPasswordVerified(durationMinutes: Int = 0) {
        val blockedUrl = _uiState.value.blockedUrl ?: return

        viewModelScope.launch {
            if (durationMinutes == 0) {
                // 永久: 添加域名到白名单
                val domain = com.browser4kids.util.UrlMatcher.extractDomain(blockedUrl)
                if (domain != null) {
                    whitelistRepository.addRule(
                        WhitelistRule(
                            pattern = domain,
                            type = com.browser4kids.data.model.RuleType.DOMAIN,
                            description = "家长授权添加"
                        )
                    )
                    accessControlManager.refreshRulesCache()
                }
            } else {
                // 临时: 按时长解锁域名
                accessControlManager.unlockUrl(blockedUrl, durationMinutes)
            }
            accessLogRepository.addLog(blockedUrl)
        }
        loadUrl(blockedUrl)
    }

    /**
     * 取消密码输入
     */
    fun onPasswordDialogDismissed() {
        _uiState.value = _uiState.value.copy(
            showPasswordDialog = false,
            blockedUrl = null
        )
        if (_uiState.value.currentUrl.isEmpty()) {
            _uiState.value = _uiState.value.copy(showHomePage = true)
        }
    }

    // WebViewClient回调处理

    fun onUrlBlocked(url: String) {
        checkAndLoadUrl(url)
    }

    fun onPageStarted(url: String) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            isLoading = true,
            errorState = null,
            showHomePage = false
        )
    }

    fun onPageFinished(url: String, title: String?) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            pageTitle = title,
            isLoading = false,
            canGoBack = webView?.canGoBack() ?: false,
            canGoForward = webView?.canGoForward() ?: false
        )
        viewModelScope.launch {
            browsingHistoryRepository.addHistory(url, title)
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.value = _uiState.value.copy(
            loadingProgress = progress,
            isLoading = progress < 100
        )
    }

    fun onTitleChanged(title: String?) {
        _uiState.value = _uiState.value.copy(pageTitle = title)
    }

    fun onError(errorCode: Int, description: String) {
        _uiState.value = _uiState.value.copy(
            errorState = BrowserError(errorCode, description),
            isLoading = false
        )
    }

    // 导航操作

    fun goBack() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            _uiState.value = _uiState.value.copy(showHomePage = true)
        }
    }

    fun goForward() {
        if (webView?.canGoForward() == true) {
            webView?.goForward()
        }
    }

    fun refresh() {
        webView?.reload()
    }

    fun goHome() {
        val homeUrl = settingsRepository.getHomeUrl()
        if (homeUrl.isNotBlank()) {
            checkAndLoadUrl(homeUrl)
        } else {
            _uiState.value = _uiState.value.copy(
                showHomePage = true,
                currentUrl = "",
                pageTitle = null
            )
            webView?.loadUrl("about:blank")
        }
    }

    fun stopLoading() {
        webView?.stopLoading()
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorState = null)
    }
}
