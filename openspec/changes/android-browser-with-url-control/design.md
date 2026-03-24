## Context

这是一个全新的Android浏览器应用项目,目标是为儿童提供安全的上网环境。当前市场上的浏览器(Chrome、Firefox等)缺乏细粒度的URL访问控制功能,家长无法有效限制儿童访问的网站范围。

本项目需要从零开始构建一个基于Android平台的浏览器应用,核心技术栈包括:
- **平台**: Android 8.0+ (API level 26+)
- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose (现代声明式UI)
- **浏览器引擎**: Android WebView
- **数据存储**: Room数据库 + SharedPreferences
- **架构模式**: MVVM (Model-View-ViewModel)

约束条件:
- 必须在Android系统的WebView能力范围内实现浏览器功能
- 数据完全本地存储,不依赖云端服务
- **主要面向Android平板设备**(7-12英寸屏幕),手机适配为次要优先级

## Goals / Non-Goals

**Goals:**
- 构建完整的Android浏览器应用,支持基本浏览功能(导航、历史、加载状态等)
- 实现灵活的URL访问控制系统,支持多种匹配规则(精确、域名、通配符)
- 提供双界面设计:儿童友好的浏览界面 + 功能完整的家长设置界面
- 确保密码保护机制安全可靠,防止儿童绕过限制
- 所有数据本地持久化存储,应用重启后配置不丢失

**Non-Goals:**
- 不实现书签、阅读列表等高级浏览器功能
- 不支持多标签页浏览(简化儿童使用场景)
- 不实现广告拦截、隐私追踪保护等功能
- 不支持扩展插件系统
- 不提供云同步功能,数据仅保存在本地设备

## Decisions

### 1. 使用WebView而非自建浏览器引擎

**决策**: 使用Android系统提供的WebView组件作为渲染引擎。

**理由**:
- WebView提供成熟的HTML/CSS/JavaScript渲染能力,无需从零实现
- 自动获得安全更新和性能优化
- 开发成本低,可快速实现MVP

**替代方案**: 
- Chromium嵌入式框架: 包体积巨大(100MB+),复杂度高
- GeckoView: Mozilla的方案,集成复杂,文档较少

**权衡**: WebView的定制能力受限,但对本项目需求足够。

### 2. URL拦截时机: shouldOverrideUrlLoading

**决策**: 在WebViewClient的`shouldOverrideUrlLoading`回调中拦截URL加载请求。

**理由**:
- 这是WebView提供的标准拦截点,在页面加载前触发
- 可以完全阻止未授权URL的加载,无需担心内容泄露
- 支持拦截所有导航行为(点击链接、表单提交、重定向等)

**替代方案**:
- `onPageStarted`回调: 页面已开始加载,无法完全阻止
- WebViewClient的其他拦截点: 粒度不够或触发时机不对

### 3. 白名单匹配策略: 三级优先级

**决策**: 白名单规则按以下优先级匹配:
1. 精确URL匹配 (如 `https://example.com/page`)
2. 域名匹配 (如 `example.com` 匹配所有该域名路径)
3. 通配符匹配 (如 `*.example.com` 或 `example.com/*`)

**理由**:
- 精确匹配提供最细粒度控制
- 域名匹配满足常见需求(允许整个网站)
- 通配符提供灵活性(如允许所有子域名)

**实现**:
```kotlin
fun isUrlAllowed(url: String, rules: List<WhitelistRule>): Boolean {
    // 1. 精确匹配
    if (rules.any { it.type == EXACT && it.pattern == url }) return true
    
    // 2. 域名匹配
    val domain = extractDomain(url)
    if (rules.any { it.type == DOMAIN && it.pattern == domain }) return true
    
    // 3. 通配符匹配
    return rules.any { it.type == WILDCARD && matchesPattern(url, it.pattern) }
}
```

### 4. 数据存储: Room数据库 + SharedPreferences

**决策**:
- **Room数据库**: 存储白名单规则、访问日志、历史记录
- **SharedPreferences**: 存储密码哈希、应用设置(搜索引擎、主页等)

**理由**:
- Room提供类型安全的数据库操作,适合结构化数据
- SharedPreferences适合简单键值对,读写速度快
- 密码使用SHA-256哈希存储,不保存明文

**数据模型**:
```kotlin
@Entity(tableName = "whitelist_rules")
data class WhitelistRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val type: RuleType, // EXACT, DOMAIN, WILDCARD
    val addedTime: Long
)

@Entity(tableName = "access_logs")
data class AccessLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String?,
    val timestamp: Long,
    val unlockMethod: String // PASSWORD or WHITELIST
)
```

### 5. 架构模式: MVVM + Repository

**决策**: 采用标准Android MVVM架构:
- **View (Activity/Composable)**: UI渲染和用户交互
- **ViewModel**: 业务逻辑和状态管理
- **Repository**: 数据层抽象(Room DAO + SharedPreferences)

**理由**:
- 符合Android官方推荐的架构模式
- ViewModel自动处理配置变更(屏幕旋转)
- Repository层便于单元测试和数据源切换

**组件结构**:
```
app/
├── ui/
│   ├── browser/          # 浏览器主界面
│   ├── settings/         # 家长设置界面
│   └── password/         # 密码输入/设置界面
├── viewmodel/
│   ├── BrowserViewModel  # 浏览器逻辑
│   └── SettingsViewModel # 设置逻辑
├── repository/
│   ├── WhitelistRepository
│   ├── AccessLogRepository
│   └── SettingsRepository
├── data/
│   ├── database/         # Room数据库
│   └── model/            # 数据模型
└── util/
    ├── UrlMatcher        # URL匹配工具
    └── PasswordManager   # 密码哈希验证
```

### 6. UI框架: Jetpack Compose

**决策**: 使用Jetpack Compose构建UI,而非传统XML布局。

**理由**:
- 声明式UI开发效率更高
- 更容易实现儿童友好的动画和交互效果
- 是Android官方推荐的现代UI框架

**替代方案**:
- XML + View系统: 传统方案,开发效率较低
- Flutter: 跨平台但本项目只需Android

### 7. 密码存储: SHA-256哈希

**决策**: 密码使用SHA-256哈希后存储在SharedPreferences。

**理由**:
- 不保存明文密码,即使数据泄露也无法反推
- SHA-256是安全且广泛使用的哈希算法
- 家长场景下无需复杂的加盐和密钥派生(不涉及网络传输)

**实现**:
```kotlin
fun hashPassword(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(password.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}
```

## Risks / Trade-offs

### 风险1: WebView安全漏洞
**风险**: WebView可能存在未修复的安全漏洞,允许恶意网站绕过限制。

**缓解措施**:
- 禁用不必要的WebView功能(文件访问、内容访问)
- 设置`WebSettings.setMixedContentMode(MIXED_CONTENT_NEVER_ALLOW)`防止混合内容
- 定期更新Android System WebView组件

### 风险2: 儿童可能绕过限制
**风险**: 儿童可能通过卸载应用、清除数据等方式绕过限制。

**缓解措施**:
- 建议家长使用Android家长控制功能锁定应用卸载
- 在应用描述中说明需配合系统家长控制使用
- 未来版本可考虑实现设备管理器(Device Admin)功能

### 风险3: URL匹配规则复杂性
**风险**: 通配符规则可能导致非预期的匹配结果,或性能问题。

**缓解措施**:
- 提供规则测试功能,家长可测试URL是否匹配规则
- 限制通配符规则数量(最多50条)
- 优先匹配精确规则,减少通配符匹配次数

### 风险4: 密码忘记无法找回
**风险**: 家长忘记密码后无法进入设置界面。

**缓解措施**:
- 首次设置时要求配置找回问题(如"你的出生城市?")
- 提供密码重置功能,需回答找回问题
- 最终兜底方案:清除应用数据(会丢失所有配置)

### 权衡1: 单标签页 vs 多标签页
**决策**: 仅支持单标签页浏览。

**权衡**:
- **优势**: 简化UI,降低儿童使用难度;减少实现复杂度
- **劣势**: 无法同时打开多个页面,可能影响部分用户体验

### 权衡2: 本地存储 vs 云同步
**决策**: 数据仅本地存储,不提供云同步。

**权衡**:
- **优势**: 无隐私泄露风险,无需账号系统,实现简单
- **劣势**: 更换设备后需重新配置,无法跨设备共享白名单

## Migration Plan

本项目是全新应用,无需迁移现有数据。部署步骤:

1. **开发阶段**:
   - 搭建Android项目结构
   - 实现核心功能模块(按tasks.md中的任务顺序)
   - 每个模块完成后进行单元测试

2. **测试阶段**:
   - 在不同Android版本设备上测试(8.0, 10, 12, 14)
   - 测试各种URL格式和边缘情况
   - 邀请目标用户进行可用性测试

3. **发布流程**:
   - 打包APK,签名发布
   - 首先发布到小范围测试渠道(Google Play Alpha)
   - 收集反馈后正式发布到Google Play Store

4. **回滚策略**:
   - 保留每个版本的APK安装包
   - 如果新版本出现严重bug,可快速回滚到旧版本
   - 数据库使用版本号管理,支持向下兼容

## Open Questions (已解决)

1. ~~**搜索引擎选择**~~: **已决定 - 禁用搜索功能**。地址栏仅接受URL输入,不支持关键词搜索。

2. **预置白名单内容**: 应该预置哪些儿童友好网站?如何分类?
   - 建议: 按年龄段和类型分类(3-6岁教育类、7-12岁教育类、娱乐类等)

3. **访问日志保留时长**: 访问日志应该保留多久?自动清理策略?
   - 建议: 默认保留30天,提供设置选项(7天/30天/90天/永久)

4. ~~**离线功能**~~: **已决定 - MVP不实现**

5. ~~**多用户支持**~~: **已决定 - MVP不实现**

6. ~~**密码解锁范围**~~: **已决定 - 仅解锁当前URL**,不影响其他非白名单URL的拦截

7. ~~**页面内嵌内容处理**~~: **已决定 - 内嵌资源(图片/JS/CSS/iframe)放行,用户点击链接跳转仍需白名单验证**。利用WebView的shouldOverrideUrlLoading仅拦截导航行为的特性实现。

8. ~~**屏幕方向**~~: **已决定 - 跟随系统设置**,不强制横屏或竖屏
