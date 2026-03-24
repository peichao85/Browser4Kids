## 1. AccessControlManager 改造

- [x] 1.1 将 unlockedDomains 从 MutableSet 改为 MutableMap (key=域名, value=到期时间戳)
- [x] 1.2 修改 unlockUrl() 方法，增加 durationMinutes 参数，0 表示永久
- [x] 1.3 修改 isUrlAllowedSync() 检查时间戳是否过期，过期则移除并返回 false
- [x] 1.4 新增 getActiveAuthorizations() 方法，返回当前有效的临时授权列表(域名+剩余秒数)
- [x] 1.5 新增 revokeAuthorization(domain) 方法，手动撤销某个域名的临时授权

## 2. PasswordDialog UI 改造

- [x] 2.1 新增授权时长数据类 AuthorizationDuration (枚举: 5min/15min/30min/1hour/permanent/custom)
- [x] 2.2 在密码输入框下方添加时长选择 Chip 组，默认选中 15 分钟
- [x] 2.3 选择"自定义"时弹出输入对话框，限制 1-1440 分钟
- [x] 2.4 修改 onPasswordVerified 回调签名，携带选中的分钟数参数

## 3. BrowserViewModel 适配

- [x] 3.1 修改 onPasswordVerified() 接收分钟数参数
- [x] 3.2 当分钟数为 0 (永久) 时，调用 whitelistRepository.addRule() 添加域名规则并刷新缓存
- [x] 3.3 当分钟数大于 0 时，调用 accessControlManager.unlockUrl(url, durationMinutes)

## 4. 设置页面 - 临时授权管理

- [x] 4.1 在设置主页面添加"临时授权"菜单入口
- [x] 4.2 创建临时授权列表页面，展示域名和剩余时间倒计时
- [x] 4.3 每条授权项添加"撤销"按钮
- [x] 4.4 使用 LaunchedEffect + delay 每秒刷新剩余时间显示
- [x] 4.5 无临时授权时显示空状态提示

## 5. 编译验证与测试

- [x] 5.1 编译项目确认无错误
- [x] 5.2 更新 AccessControlManagerTest 单元测试覆盖新逻辑
- [x] 5.3 在模拟器上验证完整流程
