package com.browser4kids.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.browser4kids.repository.SettingsRepository
import com.browser4kids.util.PasswordManager
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    settingsRepository: SettingsRepository,
    onSetupComplete: () -> Unit,
    whitelistRepository: com.browser4kids.repository.WhitelistRepository? = null
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var recoveryQuestion by remember { mutableStateOf("你的出生城市?") }
    var recoveryAnswer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            0 -> WelcomeStep(onNext = { currentStep = 1 })
            1 -> PasswordSetupStep(
                password = password,
                confirmPassword = confirmPassword,
                onPasswordChange = { password = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onNext = {
                    settingsRepository.setPassword(password)
                    currentStep = 2
                }
            )
            2 -> RecoveryQuestionStep(
                question = recoveryQuestion,
                answer = recoveryAnswer,
                onQuestionChange = { recoveryQuestion = it },
                onAnswerChange = { recoveryAnswer = it },
                onNext = {
                    settingsRepository.setRecoveryQuestion(recoveryQuestion, recoveryAnswer)
                    currentStep = 3
                },
                onSkip = {
                    settingsRepository.setSetupComplete(true)
                    onSetupComplete()
                }
            )
            3 -> PresetSitesWelcomeStep(
                whitelistRepository = whitelistRepository,
                onNext = {
                    settingsRepository.setSetupComplete(true)
                    onSetupComplete()
                }
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Browser4Kids",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "为孩子打造的安全浏览器",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "接下来需要设置家长密码\n用于管理允许访问的网站",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.width(200.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("开始设置", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PasswordSetupStep(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val validation = PasswordManager.validatePasswordStrength(password)
    val passwordsMatch = password == confirmPassword && confirmPassword.isNotEmpty()
    val canProceed = validation.isValid && passwordsMatch

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(400.dp)
    ) {
        Text(
            text = "设置家长密码 (1/2)",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "请设置一个6位以上的密码，需包含数字和字母",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = password.isNotEmpty() && !validation.isValid,
            supportingText = if (password.isNotEmpty() && !validation.isValid) {
                { Text(validation.errors.first(), color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("确认密码") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                { Text("两次密码不一致", color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(16.dp)
        )

        if (validation.isValid && password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "密码强度: 符合要求",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            enabled = canProceed,
            modifier = Modifier.width(200.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryQuestionStep(
    question: String,
    answer: String,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val questions = listOf(
        "你的出生城市?",
        "你最喜欢的颜色?",
        "你的小学名称?",
        "你宠物的名字?"
    )
    var expanded by remember { mutableStateOf(false) }
    val canProceed = answer.isNotBlank()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(400.dp)
    ) {
        Text(
            text = "设置找回问题 (2/3)",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "如果忘记密码，可以通过回答此问题来重置",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = {},
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                label = { Text("选择问题") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                questions.forEach { q ->
                    DropdownMenuItem(
                        text = { Text(q) },
                        onClick = {
                            onQuestionChange(q)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("答案") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            enabled = canProceed,
            modifier = Modifier.width(200.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSkip) {
            Text("跳过此步骤")
        }
    }
}

@Composable
private fun PresetSitesWelcomeStep(
    whitelistRepository: com.browser4kids.repository.WhitelistRepository?,
    onNext: () -> Unit
) {
    var selectedSites by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()
    val presetSites = listOf(
        Pair("youtube.com", "YouTube视频"),
        Pair("bilibili.com", "哔哩哔哩"),
        Pair("wikipedia.org", "维基百科"),
        Pair("khanacademy.org", "Khan Academy"),
        Pair("scratch.mit.edu", "Scratch编程"),
        Pair("code.org", "Code.org编程学习"),
        Pair("coolmath-games.com", "数学游戏"),
        Pair("abcya.com", "ABCya教育游戏"),
        Pair("beastacademy.com", "Beast Academy数学"),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(400.dp)
    ) {
        Text(
            text = "添加推荐网站 (3/3)",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "选择儿童友好网站添加到白名单(可选)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            presetSites.forEach { (pattern, description) ->
                val isSelected = selectedSites.contains(pattern)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSites = if (isSelected) {
                                selectedSites - pattern
                            } else {
                                selectedSites + pattern
                            }
                        }
                        .padding(8.dp)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pattern, style = MaterialTheme.typography.labelLarge)
                        Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    if (selectedSites.isNotEmpty() && whitelistRepository != null) {
                        presetSites
                            .filter { selectedSites.contains(it.first) }
                            .forEach { (pattern, description) ->
                                val rule = com.browser4kids.data.model.WhitelistRule(
                                    pattern = pattern,
                                    type = com.browser4kids.data.model.RuleType.DOMAIN,
                                    description = description
                                )
                                whitelistRepository.addRule(rule)
                            }
                    }
                    onNext()
                }
            },
            modifier = Modifier.width(200.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (selectedSites.isEmpty()) "跳过" else "添加 ${selectedSites.size} 个网站", style = MaterialTheme.typography.titleMedium)
        }
    }
}
