# Browser4Kids - 儿童安全浏览器

## 项目概述

Browser4Kids是一个专为儿童设计的Android浏览器应用,通过白名单和密码保护机制,让家长能够精确控制儿童可访问的网站,为儿童提供安全的上网环境。

## 核心需求

### 功能需求
1. **URL访问控制**: 基于白名单的URL访问限制,未授权网站需要输入家长密码才能访问
2. **限时授权**: 解锁网站时可选择授权时长(5分钟/15分钟/30分钟/1小时/永久/自定义),到期自动收回
3. **浏览器基础功能**: 基于WebView的页面加载、导航(前进/后退/刷新)、地址栏、历史记录(不支持搜索,仅URL输入)
4. **白名单管理**: 支持添加、编辑、删除白名单规则,支持精确URL、域名、通配符三种匹配模式
5. **家长设置**: 密码保护的设置界面,包含密码管理、白名单管理、临时授权管理、访问日志查看
6. **儿童友好界面**: 简洁明亮的UI设计,大图标按钮,友好的错误提示

### 非功能需求
- **平台**: Android 8.0+ (API level 26+), **主要支持平板设备**(7-12英寸)
- **安全性**: 密码SHA-256哈希存储,WebView安全配置,防截屏
- **性能**: 白名单匹配延迟<100ms,支持最多1000条规则
- **易用性**: 儿童可独立操作浏览器,家长可快速配置白名单

## 技术架构

### 技术栈
- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **浏览器引擎**: Android WebView
- **数据存储**: Room数据库 + SharedPreferences
- **架构模式**: MVVM (Model-View-ViewModel)
- **构建**: Gradle 8.5, AGP 8.2.2, Kotlin 1.9.22, KSP

### 核心模块

```
app/
├── ui/                    # 用户界面层
│   ├── browser/          # 浏览器主界面 (BrowserScreen, CustomWebViewClient, CustomWebChromeClient)
│   ├── settings/         # 家长设置界面 (含临时授权管理页)
│   ├── password/         # 密码输入界面 (含授权时长选择)
│   ├── welcome/          # 首次启动引导界面
│   └── theme/            # Material Design 3 主题
├── viewmodel/            # 业务逻辑层
│   ├── BrowserViewModel  # 浏览器状态管理,密码验证后按时长处理
│   └── SettingsViewModel # 设置页数据管理
├── repository/           # 数据访问层
│   ├── WhitelistRepository
│   ├── AccessLogRepository
│   ├── BrowsingHistoryRepository
│   └── SettingsRepository
├── data/                 # 数据模型层
│   ├── database/         # Room数据库
│   │   ├── AppDatabase
│   │   ├── WhitelistDao
│   │   ├── AccessLogDao
│   │   └── BrowsingHistoryDao
│   └── model/            # 数据实体
│       ├── WhitelistRule
│       ├── AccessLog
│       └── BrowsingHistory
└── util/                 # 工具类
    ├── UrlMatcher        # URL匹配引擎
    ├── PasswordManager   # 密码哈希和验证
    └── AccessControlManager  # 访问控制逻辑 (含限时授权)
```

### 关键设计决策

1. **URL拦截时机**: 在`WebViewClient.shouldOverrideUrlLoading`中同步拦截,确保在页面加载前进行验证
2. **匹配优先级**: 精确URL > 域名 > 通配符,三级优先级匹配
3. **域名级解锁**: 密码验证后按域名解锁(非URL级),同域名所有页面均可访问
4. **限时授权**: AccessControlManager使用`MutableMap<String, Long>`存储域名→到期时间戳,0表示永久(会话内)
5. **非HTTP scheme过滤**: CustomWebViewClient静默拦截非http/https协议(如baiduboxapp://)
6. **数据存储分离**: 
   - Room数据库存储结构化数据(白名单规则、访问日志、浏览历史)
   - SharedPreferences存储简单配置(密码哈希、应用设置)
7. **单标签页设计**: 简化儿童使用场景,降低实现复杂度

## 核心功能说明

### 1. URL访问控制与限时授权
- 用户访问URL时,系统按优先级匹配白名单规则
- 白名单内URL直接加载
- 白名单外URL弹出密码验证界面,同时显示授权时长选择(默认15分钟)
- 家长输入密码并选择时长后:
  - **永久**: 将域名添加到白名单数据库
  - **限时**: 在内存中记录域名和到期时间,到期后自动失效
- 页面内嵌资源(图片/JS/CSS/iframe)自动放行,但点击链接跳转仍需白名单验证

### 2. 白名单规则类型
- **精确匹配**: `https://example.com/page` - 仅允许该完整URL
- **域名匹配**: `example.com` - 允许该域名下所有页面和子域名
- **通配符匹配**: `*.example.com` 或 `example.com/[*]` - 支持灵活的通配符规则

### 3. 家长控制
- 密码保护的设置入口
- 白名单规则增删改查
- **临时授权管理**: 查看当前有效的临时授权列表(实时倒计时),支持手动撤销
- 查看访问日志(解锁记录)和浏览历史
- 修改家长密码
- 推荐网站快速添加(9个预置儿童友好网站)
- 清除浏览数据

### 4. 儿童友好设计
- 简化的导航控件(后退/前进/刷新/主页)
- 大尺寸触摸按钮
- 明亮的配色方案(Material Design 3,绿色主色)
- 友好的错误提示(无技术术语)
- 不提供搜索功能,地址栏仅支持URL输入
- 主页仅显示白名单网站快捷方式

## 安全设计

1. **密码保护**: SHA-256哈希存储,不保存明文
2. **WebView加固**: 
   - 禁用文件访问(`setAllowFileAccess(false)`)
   - 禁用内容访问(`setAllowContentAccess(false)`)
   - 禁止混合内容(`setMixedContentMode(NEVER_ALLOW)`)
3. **密码复杂度要求**: 至少6位,必须包含数字和字母
4. **密码找回机制**: 设置找回问题和答案
5. **防截屏**: FLAG_SECURE保护家长密码不被截图

## 项目状态

**当前状态**: 核心功能和增强功能开发完成，包含限时授权功能
- ✅ 核心功能实现完成
- ✅ 增强功能实现完成
  - 预置网站推荐界面、日志搜索、规则测试、浏览历史
  - 加载动画、页面切换动画、防截屏
- ✅ **限时URL授权** (OpenSpec change: timed-url-authorization, 20/20任务完成)
  - PasswordDialog 时长选择 Chip 组
  - AccessControlManager 限时域名授权
  - 设置页临时授权管理(倒计时+撤销)
- ✅ 130个单元测试全部通过
- ✅ **修复平板上界面空白不可见的问题**（全面修复）：
  - WebView (AndroidView) 原生 View Z-order 覆盖 Compose UI：通过 `isVisible` 参数控制原生 View 的 `visibility`，在任何 Compose 覆盖层（HomePage/ErrorPage/PasswordDialog/设置密码框）显示时隐藏 WebView
  - WebView 添加 `clipToOutline = true` 防止溢出容器边界
  - 移除 `enableEdgeToEdge()` 避免平板上内容被系统栏遮挡、与透明系统栏的配色冲突
  - 移除对应的 `windowInsetsPadding(WindowInsets.systemBars)`
  - 禁用动态颜色 (`dynamicColor = false`)，确保使用固定的儿童友好明亮配色方案，避免壁纸导致 UI 元素不可见
  - `BrowserScreen` 新增 `hasExternalOverlay` 参数，支持外部覆盖层也能控制 WebView 可见性
- ✅ **修改 WebView User-Agent 伪装成标准 Chrome 浏览器**：移除 WebView 特有的 `Version/x.x` 标识，解决部分网站检测到非标准浏览器而拒绝访问的问题
- ⏳ 剩余任务: 应用锁定、更多单元测试、打包发布、文档编写

## 参考文档

- 初始变更提案: `openspec/changes/android-browser-with-url-control/`
- 限时授权变更: `openspec/changes/timed-url-authorization/`
