## Why

当前访问白名单之外的网站时，家长只能选择"允许"或"拒绝"，没有时间限制选项。家长有时希望临时允许孩子访问某个网站一段时间（如完成作业），而不是永久添加到白名单。需要提供限时授权机制，让家长更灵活地控制孩子的上网行为。

## What Changes

- 密码验证对话框增加"授权时长"选择：5分钟、15分钟、30分钟、1小时、永久、自定义
- 选择"永久"时，自动将该域名添加到白名单规则列表
- 选择"自定义"时，弹出输入框让家长输入分钟数
- 非永久授权的域名在授权到期后自动失效，再次访问需重新输入密码
- 设置页面新增"临时授权"区域，展示当前有效的临时授权域名及剩余时间
- 设置页面可手动撤销临时授权

## Capabilities

### New Capabilities
- `timed-authorization`: 限时域名授权机制，包括授权时长选择、到期自动失效、临时授权管理界面

### Modified Capabilities

## Impact

- `AccessControlManager` — 需要从简单的 Set 改为带过期时间的 Map 结构
- `PasswordDialog` — 需要增加时长选择 UI
- `BrowserViewModel` — 需要传递授权时长参数
- `SettingsScreen` — 需要新增临时授权管理区域
- `CustomWebViewClient` — `isUrlAllowedSync` 需要检查授权是否过期
