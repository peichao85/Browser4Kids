package com.browser4kids.ui.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.browser4kids.repository.SettingsRepository
import com.browser4kids.ui.theme.BlockedPageBackground
import com.browser4kids.util.UrlMatcher

/**
 * 授权时长选项
 */
enum class AuthorizationDuration(val label: String, val minutes: Int) {
    FIVE_MINUTES("5分钟", 5),
    FIFTEEN_MINUTES("15分钟", 15),
    THIRTY_MINUTES("30分钟", 30),
    ONE_HOUR("1小时", 60),
    PERMANENT("永久", 0),
    CUSTOM("自定义", -1);
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PasswordDialog(
    blockedUrl: String,
    settingsRepository: SettingsRepository,
    onPasswordVerified: (durationMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptCount by remember { mutableStateOf(0) }
    var selectedDuration by remember { mutableStateOf(AuthorizationDuration.FIFTEEN_MINUTES) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableIntStateOf(30) }

    // 计算实际要使用的分钟数
    val effectiveMinutes = when (selectedDuration) {
        AuthorizationDuration.CUSTOM -> customMinutes
        else -> selectedDuration.minutes
    }

    // 验证密码的通用逻辑
    fun verifyAndSubmit() {
        if (settingsRepository.verifyPassword(password)) {
            onPasswordVerified(effectiveMinutes)
        } else {
            attemptCount++
            errorMessage = "密码不正确，请重试"
            password = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockedPageBackground.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(480.dp)
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "需要家长许可",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "访问这个网站需要输入家长密码",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (blockedUrl.isNotBlank()) {
                    Text(
                        text = UrlMatcher.extractDomain(blockedUrl) ?: blockedUrl,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("家长密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { msg ->
                        { Text(msg, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { verifyAndSubmit() }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 授权时长选择
                Text(
                    text = "授权时长",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AuthorizationDuration.entries.forEach { duration ->
                        val chipLabel = if (duration == AuthorizationDuration.CUSTOM && selectedDuration == AuthorizationDuration.CUSTOM) {
                            "${customMinutes}分钟"
                        } else {
                            duration.label
                        }
                        FilterChip(
                            selected = selectedDuration == duration,
                            onClick = {
                                if (duration == AuthorizationDuration.CUSTOM) {
                                    showCustomDurationDialog = true
                                }
                                selectedDuration = duration
                            },
                            label = { Text(chipLabel, style = MaterialTheme.typography.bodyMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("取消")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { verifyAndSubmit() },
                        enabled = password.isNotEmpty()
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }

    // 自定义时长输入对话框
    if (showCustomDurationDialog) {
        CustomDurationDialog(
            initialMinutes = customMinutes,
            onConfirm = { minutes ->
                customMinutes = minutes
                showCustomDurationDialog = false
            },
            onDismiss = {
                showCustomDurationDialog = false
                // 如果取消自定义，回到默认的15分钟
                if (selectedDuration == AuthorizationDuration.CUSTOM) {
                    selectedDuration = AuthorizationDuration.FIFTEEN_MINUTES
                }
            }
        )
    }
}

@Composable
private fun CustomDurationDialog(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf(initialMinutes.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义授权时长") },
        text = {
            Column {
                Text(
                    text = "请输入授权时长（1-1440分钟）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it.filter { c -> c.isDigit() }
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分钟数") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { msg ->
                        { Text(msg, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = inputText.toIntOrNull()
                    when {
                        minutes == null || minutes < 1 -> {
                            errorMessage = "请输入至少1分钟"
                        }
                        minutes > 1440 -> {
                            errorMessage = "最多1440分钟（24小时）"
                        }
                        else -> onConfirm(minutes)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
