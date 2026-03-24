## Context

Browser4Kids 是一个 Android 儿童浏览器应用，使用 Kotlin + Jetpack Compose + MVVM 架构。当前访问白名单之外的网站时，家长输入密码后只能"允许"或"取消"，允许后该域名在当前会话内有效（内存中的 `unlockedDomains` Set）。需要增加限时授权机制。

当前关键文件：
- `AccessControlManager.kt` — 维护 `unlockedDomains: MutableSet<String>`，提供 `isUrlAllowedSync()` 和 `unlockUrl()`
- `PasswordDialog.kt` — 密码输入对话框，验证后回调 `onPasswordVerified`
- `BrowserViewModel.kt` — 调用 `accessControlManager.unlockUrl()` 并加载 URL
- `SettingsScreen.kt` — 设置页面，已有白名单管理、访问日志、浏览历史等区域
- `WhitelistRepository.kt` — Room 数据库操作白名单规则

## Goals / Non-Goals

**Goals:**
- 家长输入密码后可选择授权时长（5分钟/15分钟/30分钟/1小时/永久/自定义）
- 选择"永久"时直接添加到白名单数据库
- 选择"自定义"时弹出输入框输入分钟数
- 临时授权到期后自动失效
- 设置页面可查看和撤销临时授权

**Non-Goals:**
- 不需要持久化临时授权（应用重启后临时授权清零是可接受的）
- 不需要通知提醒授权即将到期
- 不修改白名单规则的数据库结构

## Decisions

### 1. 临时授权存储：内存 Map 替代 Set

将 `AccessControlManager` 中的 `unlockedDomains: MutableSet<String>` 改为 `unlockedDomains: MutableMap<String, Long>`，value 为到期时间戳（`System.currentTimeMillis()` + 分钟数 * 60000）。

**理由**：简单直接，不需要数据库变更。临时授权本身是会话级别的，不需要持久化。

**替代方案**：持久化到 Room 数据库 — 增加复杂度但应用重启后可保留授权。暂不需要。

### 2. 永久授权：直接调用 WhitelistRepository

选择"永久"时，在 `BrowserViewModel` 中直接调用 `whitelistRepository.addRule()` 添加域名规则，同时刷新 `accessControlManager` 的规则缓存。

### 3. UI 方案：密码对话框内嵌时长选择

在 `PasswordDialog` 中密码输入框下方增加一排时长选择按钮（Chip 样式），默认选中"15分钟"。密码验证成功后，携带选中的时长回调。

**理由**：一步完成授权，不需要二次弹窗（除了自定义时长）。

### 4. 设置页面展示：新增"临时授权"卡片区域

在设置主页面现有菜单项中添加"临时授权"入口，点击进入独立页面展示列表，每条显示域名和倒计时。使用 `LaunchedEffect` + `delay` 每秒刷新剩余时间显示。

## Risks / Trade-offs

- **[应用重启丢失授权]** → 可接受行为，临时授权本身就是短期的。如果后续需要可扩展为持久化。
- **[授权到期时机不精确]** → `shouldOverrideUrlLoading` 只在导航时检查，不会主动中断正在浏览的页面。这是合理的行为 — 到期后下次导航才拦截。
- **[自定义时长输入校验]** → 需要限制最小1分钟、最大1440分钟（24小时），防止无效输入。
