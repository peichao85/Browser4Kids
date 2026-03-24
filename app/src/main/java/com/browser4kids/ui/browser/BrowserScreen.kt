package com.browser4kids.ui.browser

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.ui.theme.ProgressBarColor
import com.browser4kids.util.UrlMatcher
import com.browser4kids.viewmodel.BrowserViewModel

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val whitelistRules by viewModel.whitelistRules.collectAsState(initial = emptyList())

    Column(modifier = modifier.fillMaxSize()) {
        // 顶部: 地址栏 + 导航按钮 + 设置按钮
        AddressBar(
            currentUrl = uiState.currentUrl,
            canGoBack = uiState.canGoBack,
            canGoForward = uiState.canGoForward,
            isLoading = uiState.isLoading,
            onNavigate = { url -> viewModel.navigateToUrl(url) },
            onHome = { viewModel.goHome() },
            onBack = { viewModel.goBack() },
            onForward = { viewModel.goForward() },
            onRefresh = { viewModel.refresh() },
            onStop = { viewModel.stopLoading() },
            onSettingsClick = onSettingsClick
        )

        // 加载进度条和提示
        AnimatedVisibility(visible = uiState.isLoading) {
            Column(modifier = Modifier.fillMaxWidth()) {
                @Suppress("DEPRECATION")
                LinearProgressIndicator(
                    progress = uiState.loadingProgress / 100f,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = ProgressBarColor,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ 正在加载...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 主内容区
        Box(modifier = Modifier.weight(1f)) {
            // WebView
            BrowserWebView(viewModel = viewModel)

            // 主页覆盖层
            if (uiState.showHomePage) {
                HomePage(
                    whitelistRules = whitelistRules,
                    onSiteClick = { url -> viewModel.checkAndLoadUrl(url) }
                )
            }

            // 错误页面覆盖层
            uiState.errorState?.let { error ->
                ErrorPage(
                    errorCode = error.errorCode,
                    onRetry = { viewModel.refresh() },
                    onGoHome = { viewModel.goHome() }
                )
            }

            // 密码拦截对话框
            if (uiState.showPasswordDialog) {
                com.browser4kids.ui.password.PasswordDialog(
                    blockedUrl = uiState.blockedUrl ?: "",
                    settingsRepository = viewModel.settingsRepository,
                    onPasswordVerified = { durationMinutes ->
                        viewModel.onPasswordVerified(durationMinutes)
                    },
                    onDismiss = { viewModel.onPasswordDialogDismissed() }
                )
            }
        }
    }
}

@Composable
fun AddressBar(
    currentUrl: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLoading: Boolean,
    onNavigate: (String) -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onStop: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // 当URL变化时更新显示
    if (!isEditing) {
        inputText = currentUrl
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 主页按钮
        IconButton(
            onClick = onHome,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "主页",
                modifier = Modifier.size(22.dp)
            )
        }

        // 后退按钮
        IconButton(
            onClick = onBack,
            enabled = canGoBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "后退",
                modifier = Modifier.size(22.dp)
            )
        }

        // 前进按钮
        IconButton(
            onClick = onForward,
            enabled = canGoForward,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "前进",
                modifier = Modifier.size(22.dp)
            )
        }

        // 刷新/停止按钮
        IconButton(
            onClick = if (isLoading) onStop else onRefresh,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                contentDescription = if (isLoading) "停止" else "刷新",
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 地址输入框
        OutlinedTextField(
            value = if (isEditing) inputText else currentUrl,
            onValueChange = {
                inputText = it
                isEditing = true
            },
            modifier = Modifier.weight(1f).height(56.dp),
            placeholder = { Text("输入网址...") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    if (UrlMatcher.isValidUrl(inputText)) {
                        onNavigate(inputText.trim())
                        isEditing = false
                    }
                }
            ),
            trailingIcon = {
                if (isEditing && inputText.isNotEmpty()) {
                    IconButton(onClick = {
                        inputText = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "清除")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.width(4.dp))

        // 设置按钮
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // 配置WebView设置
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    // 安全设置
                    allowFileAccess = false
                    allowContentAccess = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }

                // 设置自定义Client
                webViewClient = CustomWebViewClient(
                    accessControlManager = viewModel.accessControlManager,
                    onUrlBlocked = { url -> viewModel.onUrlBlocked(url) },
                    onPageStarted = { url -> viewModel.onPageStarted(url) },
                    onPageFinished = { url, title -> viewModel.onPageFinished(url, title) },
                    onError = { code, desc -> viewModel.onError(code, desc) }
                )

                webChromeClient = CustomWebChromeClient(
                    onProgressChanged = { progress -> viewModel.onProgressChanged(progress) },
                    onTitleChanged = { title -> viewModel.onTitleChanged(title) }
                )

                viewModel.setWebView(this)

                // 恢复之前浏览的页面(从设置页返回时WebView会重建)
                val lastUrl = viewModel.uiState.value.currentUrl
                if (lastUrl.isNotBlank()) {
                    loadUrl(lastUrl)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun HomePage(
    whitelistRules: List<WhitelistRule>,
    onSiteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 从白名单中提取域名规则用于显示快捷方式
    val domainRules = whitelistRules
        .filter { it.type == com.browser4kids.data.model.RuleType.DOMAIN }
        .take(12)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Browser4Kids",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "安全浏览,快乐成长",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (domainRules.isNotEmpty()) {
            Text(
                text = "常用网站",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(domainRules) { rule ->
                    SiteShortcut(
                        domain = rule.pattern,
                        description = rule.description,
                        onClick = { onSiteClick("https://${rule.pattern}") }
                    )
                }
            }
        } else {
            Text(
                text = "还没有添加网站\n请让家长在设置中添加允许访问的网站",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SiteShortcut(
    domain: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = domain,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorPage(
    errorCode: Int,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, message) = when {
        errorCode == -2 || errorCode == -6 -> "网络连接有问题" to "请检查一下网络连接,再试试看"
        errorCode == -8 -> "连接超时了" to "网络好像有点慢,再试一次吧"
        errorCode == -14 -> "这个网页找不到了" to "网页可能已经不在了"
        else -> "打不开这个网页" to "出了一点小问题,再试试看"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            androidx.compose.material3.Button(onClick = onRetry) {
                Text("再试一次")
            }
            androidx.compose.material3.OutlinedButton(onClick = onGoHome) {
                Text("回到主页")
            }
        }
    }
}
