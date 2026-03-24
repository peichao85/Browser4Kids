package com.browser4kids.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.browser4kids.data.model.AccessLog
import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.ui.theme.WhitelistDomainColor
import com.browser4kids.ui.theme.WhitelistExactColor
import com.browser4kids.ui.theme.WhitelistWildcardColor
import com.browser4kids.util.PasswordManager
import com.browser4kids.util.AccessControlManager
import com.browser4kids.util.TemporaryAuthorization
import com.browser4kids.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    accessControlManager: AccessControlManager,
    onBack: () -> Unit
) {
    var currentPage by remember { mutableStateOf("main") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (currentPage) {
            "main" -> AnimatedVisibility(
                visible = currentPage == "main",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
            ) {
                SettingsMainPage(
                    viewModel = viewModel,
                    onBack = onBack,
                    onNavigate = { currentPage = it },
                    modifier = Modifier.padding(padding)
                )
            }
            "password" -> AnimatedVisibility(
                visible = currentPage == "password",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                PasswordManagementPage(
                    viewModel = viewModel,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
            "whitelist" -> AnimatedVisibility(
                visible = currentPage == "whitelist",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                WhitelistManagementPage(
                    viewModel = viewModel,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
            "logs" -> AnimatedVisibility(
                visible = currentPage == "logs",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                AccessLogPage(
                    viewModel = viewModel,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
            "history" -> AnimatedVisibility(
                visible = currentPage == "history",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                BrowsingHistoryPage(
                    viewModel = viewModel,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
            "presets" -> AnimatedVisibility(
                visible = currentPage == "presets",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                PresetSitesPage(
                    viewModel = viewModel,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
            "tempauth" -> AnimatedVisibility(
                visible = currentPage == "tempauth",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                TemporaryAuthorizationPage(
                    accessControlManager = accessControlManager,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
            "appsettings" -> AnimatedVisibility(
                visible = currentPage == "appsettings",
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                AppSettingsPage(
                    viewModel = viewModel,
                    onBack = { currentPage = "main" },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMainPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ruleCount by viewModel.ruleCount.collectAsState(initial = 0)
    val logCount by viewModel.logCount.collectAsState(initial = 0)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("家长设置") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ListItem(
                    headlineContent = { Text("密码管理") },
                    supportingContent = { Text("修改家长密码") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("password") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("白名单管理") },
                    supportingContent = { Text("管理允许访问的网站 ($ruleCount 条规则)") },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("whitelist") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("访问日志") },
                    supportingContent = { Text("查看解锁记录 ($logCount 条)") },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("logs") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("临时授权") },
                    supportingContent = { Text("管理当前有效的临时访问授权") },
                    leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("tempauth") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("浏览历史") },
                    supportingContent = { Text("查看访问过的网站") },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("history") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("推荐网站") },
                    supportingContent = { Text("添加儿童友好网站") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("presets") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("应用设置") },
                    supportingContent = { Text("主页、清除数据") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigate("appsettings") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordManagementPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val validation = PasswordManager.validatePasswordStrength(newPassword)
    val passwordsMatch = newPassword == confirmPassword && confirmPassword.isNotEmpty()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("密码管理") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Column(
            modifier = Modifier.padding(24.dp).width(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("旧密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("新密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = newPassword.isNotEmpty() && !validation.isValid,
                supportingText = if (newPassword.isNotEmpty() && !validation.isValid) {
                    { Text(validation.errors.first(), color = MaterialTheme.colorScheme.error) }
                } else null,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("确认新密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    { Text("两次密码不一致", color = MaterialTheme.colorScheme.error) }
                } else null,
                shape = RoundedCornerShape(16.dp)
            )

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(msg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val success = viewModel.changePassword(oldPassword, newPassword)
                    if (success) {
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    } else {
                        errorMessage = "旧密码不正确"
                    }
                },
                enabled = validation.isValid && passwordsMatch && oldPassword.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("保存修改")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhitelistManagementPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.whitelistRules.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<WhitelistRule?>(null) }
    var deletingRule by remember { mutableStateOf<WhitelistRule?>(null) }
    var testingRule by remember { mutableStateOf<WhitelistRule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("白名单管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加规则")
            }
        },
        modifier = modifier
    ) { padding ->
        if (rules.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("还没有白名单规则", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("点击右下角 + 添加规则", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // 按类型分组
            val domainRules = rules.filter { it.type == RuleType.DOMAIN }
            val exactRules = rules.filter { it.type == RuleType.EXACT }
            val wildcardRules = rules.filter { it.type == RuleType.WILDCARD }

            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (domainRules.isNotEmpty()) {
                    item {
                        Text(
                            "域名规则 (${domainRules.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = WhitelistDomainColor
                        )
                    }
                    items(domainRules, key = { it.id }) { rule ->
                        RuleItem(
                            rule,
                            onEdit = { editingRule = rule },
                            onDelete = { deletingRule = rule },
                            onTest = { testingRule = rule }
                        )
                    }
                }
                if (exactRules.isNotEmpty()) {
                    item {
                        Text(
                            "精确URL (${exactRules.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = WhitelistExactColor
                        )
                    }
                    items(exactRules, key = { it.id }) { rule ->
                        RuleItem(
                            rule,
                            onEdit = { editingRule = rule },
                            onDelete = { deletingRule = rule },
                            onTest = { testingRule = rule }
                        )
                    }
                }
                if (wildcardRules.isNotEmpty()) {
                    item {
                        Text(
                            "通配符规则 (${wildcardRules.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = WhitelistWildcardColor
                        )
                    }
                    items(wildcardRules, key = { it.id }) { rule ->
                        RuleItem(
                            rule,
                            onEdit = { editingRule = rule },
                            onDelete = { deletingRule = rule },
                            onTest = { testingRule = rule }
                        )
                    }
                }
            }
        }
    }

    // 添加规则对话框
    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { pattern, type, desc ->
                viewModel.addRule(pattern, type, desc)
                showAddDialog = false
            }
        )
    }

    // 编辑规则对话框
    editingRule?.let { rule ->
        EditRuleDialog(
            rule = rule,
            onDismiss = { editingRule = null },
            onSave = { updated ->
                viewModel.updateRule(updated)
                editingRule = null
            }
        )
    }

    // 删除确认对话框
    deletingRule?.let { rule ->
        AlertDialog(
            onDismissRequest = { deletingRule = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除规则 \"${rule.pattern}\" 吗?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRule(rule)
                    deletingRule = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRule = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 测试规则对话框
    testingRule?.let { rule ->
        RuleTestDialog(
            rule = rule,
            viewModel = viewModel,
            onDismiss = { testingRule = null }
        )
    }
}

@Composable
private fun RuleItem(
    rule: WhitelistRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit = {}
) {
    ListItem(
        headlineContent = { Text(rule.pattern) },
        supportingContent = {
            if (rule.description.isNotBlank()) Text(rule.description)
        },
        trailingContent = {
            Row {
                IconButton(onClick = onTest, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "测试", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, RuleType, String) -> Unit
) {
    var pattern by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(RuleType.DOMAIN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加白名单规则") },
        text = {
            Column {
                // 规则类型选择
                Text("规则类型", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                RuleType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedType = type }.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                when (type) {
                                    RuleType.DOMAIN -> "域名 (推荐)"
                                    RuleType.EXACT -> "精确URL"
                                    RuleType.WILDCARD -> "通配符"
                                }
                            )
                            Text(
                                when (type) {
                                    RuleType.DOMAIN -> "允许整个网站,如 youtube.com"
                                    RuleType.EXACT -> "仅允许特定页面"
                                    RuleType.WILDCARD -> "高级匹配,如 *.example.com"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL / 域名") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                RuleType.DOMAIN -> "example.com"
                                RuleType.EXACT -> "https://example.com/page"
                                RuleType.WILDCARD -> "*.example.com"
                            }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注 (可选)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(pattern, selectedType, description) },
                enabled = pattern.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun EditRuleDialog(
    rule: WhitelistRule,
    onDismiss: () -> Unit,
    onSave: (WhitelistRule) -> Unit
) {
    var pattern by remember { mutableStateOf(rule.pattern) }
    var description by remember { mutableStateOf(rule.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑规则") },
        text = {
            Column {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL / 域名") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注 (可选)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(rule.copy(pattern = pattern, description = description)) },
                enabled = pattern.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessLogPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.accessLogs.collectAsState(initial = emptyList())
    var showClearDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val filteredLogs = if (searchQuery.isEmpty()) {
        logs
    } else {
        logs.filter { it.url.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("访问日志") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                if (logs.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text("清除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )

        // 搜索栏
        if (logs.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("搜索URL") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        if (filteredLogs.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(if (searchQuery.isEmpty()) "暂无访问日志" else "没有匹配的记录", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredLogs, key = { it.id }) { log ->
                    ListItem(
                        headlineContent = { Text(log.url, maxLines = 1) },
                        supportingContent = {
                            Column {
                                log.title?.let { Text(it, maxLines = 1) }
                                Text(dateFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除") },
            text = { Text("确定要清除所有访问日志吗?此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAccessLogs()
                    showClearDialog = false
                }) {
                    Text("清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetSitesPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existingRules by viewModel.whitelistRules.collectAsState(initial = emptyList())
    val existingPatterns = existingRules.map { it.pattern }.toSet()
    var selectedSites by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("推荐网站") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(SettingsViewModel.PRESET_SITES) { site ->
                val alreadyAdded = existingPatterns.contains(site.pattern)
                val isSelected = selectedSites.contains(site.pattern)

                ListItem(
                    headlineContent = { Text(site.pattern) },
                    supportingContent = { Text(site.description) },
                    leadingContent = {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        if (alreadyAdded) {
                            Text("已添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            androidx.compose.material3.Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedSites = if (checked) {
                                        selectedSites + site.pattern
                                    } else {
                                        selectedSites - site.pattern
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        if (!alreadyAdded) {
                            selectedSites = if (isSelected) {
                                selectedSites - site.pattern
                            } else {
                                selectedSites + site.pattern
                            }
                        }
                    }
                )
            }
        }

        if (selectedSites.isNotEmpty()) {
            Button(
                onClick = {
                    val rulesToAdd = SettingsViewModel.PRESET_SITES
                        .filter { selectedSites.contains(it.pattern) }
                    viewModel.addRules(rulesToAdd)
                    selectedSites = emptySet()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("添加 ${selectedSites.size} 个网站到白名单")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSettingsPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var homeUrl by remember { mutableStateOf(viewModel.settingsRepository.getHomeUrl()) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("应用设置") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Column(modifier = Modifier.padding(24.dp)) {
            // 主页设置
            Text("主页设置", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = homeUrl,
                    onValueChange = { homeUrl = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("主页URL") },
                    placeholder = { Text("例如: youtube.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { viewModel.setHomeUrl(homeUrl) },
                    enabled = homeUrl.isNotBlank()
                ) {
                    Text("保存")
                }
            }
            Text(
                "主页URL必须在白名单中才能正常加载",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 清除数据
            Text("清除数据", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.clearBrowsingHistory() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清除浏览历史")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.clearCache() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清除缓存")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.clearCookies() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清除Cookies")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showClearAllDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清除所有浏览数据")
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("确认清除") },
            text = { Text("确定要清除所有浏览数据(历史、缓存、Cookies)?此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllBrowsingData()
                    showClearAllDialog = false
                }) {
                    Text("清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowsingHistoryPage(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val browsingHistory by viewModel.browsingHistory.collectAsState(initial = emptyList())
    var showClearDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val filteredHistory = if (searchQuery.isEmpty()) {
        browsingHistory
    } else {
        browsingHistory.filter { it.url.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("浏览历史") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                if (browsingHistory.isNotEmpty()) {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text("清除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )

        // 搜索栏
        if (browsingHistory.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("搜索网站") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        if (filteredHistory.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(if (searchQuery.isEmpty()) "暂无浏览历史" else "没有匹配的记录", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredHistory, key = { it.id }) { history ->
                    ListItem(
                        headlineContent = { Text(history.url, maxLines = 1) },
                        supportingContent = {
                            Column {
                                history.title?.let { Text(it, maxLines = 1) }
                                Text(dateFormat.format(Date(history.timestamp)), style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除") },
            text = { Text("确定要清除所有浏览历史吗?此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearBrowsingHistory()
                    showClearDialog = false
                }) {
                    Text("清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun RuleTestDialog(
    rule: WhitelistRule,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    var testUrl by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("测试规则") },
        text = {
            Column {
                Text("规则: ${rule.pattern}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = testUrl,
                    onValueChange = { testUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入要测试的URL") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (testResult != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(
                                if (testResult == true) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (testResult == true) Icons.Default.Add else Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (testResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (testResult == true) "✓ 匹配此规则" else "✗ 不匹配此规则",
                            color = if (testResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        if (testUrl.isNotBlank()) {
                            isLoading = true
                            testResult = viewModel.testUrlAsync(testUrl)
                            isLoading = false
                        }
                    }
                },
                enabled = testUrl.isNotBlank() && !isLoading
            ) {
                Text("测试")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemporaryAuthorizationPage(
    accessControlManager: AccessControlManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var authorizations by remember { mutableStateOf(emptyList<TemporaryAuthorization>()) }
    var showRevokeDialog by remember { mutableStateOf<String?>(null) }

    // 每秒刷新授权列表和剩余时间
    LaunchedEffect(Unit) {
        while (true) {
            authorizations = accessControlManager.getActiveAuthorizations()
            delay(1000)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("临时授权") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        if (authorizations.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "当前没有临时授权",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "在浏览器中解锁网站时选择时限即可创建临时授权",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(authorizations, key = { it.domain }) { auth ->
                    ListItem(
                        headlineContent = { Text(auth.domain) },
                        supportingContent = {
                            Text(
                                text = formatRemainingTime(auth.remainingSeconds),
                                color = if (auth.remainingSeconds < 300)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            OutlinedButton(
                                onClick = { showRevokeDialog = auth.domain }
                            ) {
                                Text("撤销", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }
        }
    }

    // 撤销确认对话框
    showRevokeDialog?.let { domain ->
        AlertDialog(
            onDismissRequest = { showRevokeDialog = null },
            title = { Text("撤销授权") },
            text = { Text("确定要撤销 \"$domain\" 的临时访问授权吗？撤销后需要重新输入密码才能访问。") },
            confirmButton = {
                TextButton(onClick = {
                    accessControlManager.revokeAuthorization(domain)
                    authorizations = accessControlManager.getActiveAuthorizations()
                    showRevokeDialog = null
                }) {
                    Text("撤销", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 格式化剩余时间显示
 */
private fun formatRemainingTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "剩余 ${hours}小时${minutes}分钟"
        minutes > 0 -> "剩余 ${minutes}分${secs}秒"
        else -> "剩余 ${secs}秒"
    }
}
