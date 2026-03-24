## ADDED Requirements

### Requirement: URL访问验证
系统SHALL在用户尝试访问URL时验证该URL是否在白名单中。

#### Scenario: 访问白名单内URL
- **WHEN** 用户访问的URL匹配白名单规则
- **THEN** 系统允许页面正常加载

#### Scenario: 访问白名单外URL需要密码
- **WHEN** 用户访问的URL不在白名单中
- **THEN** 系统SHALL显示密码输入界面,阻止页面加载

#### Scenario: 正确输入密码后允许访问当前URL
- **WHEN** 用户在密码界面输入正确的家长密码
- **THEN** 系统SHALL仅允许访问该URL(不影响其他非白名单URL的拦截),并记录访问日志

#### Scenario: 页面内嵌内容放行
- **WHEN** 白名单内或已解锁的页面加载内嵌资源(图片、CSS、JS、iframe等)
- **THEN** 系统SHALL允许这些资源正常加载,不进行拦截

#### Scenario: 页面内跳转链接仍需验证
- **WHEN** 用户在白名单内或已解锁的页面中点击链接跳转到其他URL
- **THEN** 系统SHALL对目标URL进行白名单验证,非白名单URL仍需密码

#### Scenario: 密码输入错误
- **WHEN** 用户输入的密码不正确
- **THEN** 系统SHALL显示错误提示,继续阻止页面加载

### Requirement: URL匹配规则
系统SHALL支持多种URL匹配模式来判断URL是否在白名单中。

#### Scenario: 精确URL匹配
- **WHEN** 白名单规则为完整URL(如https://example.com/page)
- **THEN** 系统SHALL仅允许该精确URL

#### Scenario: 域名匹配
- **WHEN** 白名单规则为域名(如example.com)
- **THEN** 系统SHALL允许该域名下所有路径和子域名

#### Scenario: 通配符匹配
- **WHEN** 白名单规则包含通配符(如*.example.com或example.com/*)
- **THEN** 系统SHALL根据通配符规则匹配URL

### Requirement: 密码保护机制
系统MUST提供家长密码设置和验证功能。

#### Scenario: 首次启动设置密码
- **WHEN** 应用首次启动且未设置密码
- **THEN** 系统SHALL强制用户设置家长密码

#### Scenario: 密码验证
- **WHEN** 需要验证家长身份时
- **THEN** 系统SHALL要求输入密码,验证成功后才允许继续操作

#### Scenario: 密码修改
- **WHEN** 用户在设置界面修改密码
- **THEN** 系统SHALL要求先输入旧密码,验证成功后才允许设置新密码

### Requirement: 访问日志记录
系统SHALL记录所有通过密码解锁的URL访问。

#### Scenario: 记录解锁访问
- **WHEN** 用户通过密码验证访问非白名单URL
- **THEN** 系统SHALL记录时间戳、URL和访问结果

#### Scenario: 查看访问日志
- **WHEN** 家长在设置界面查看访问日志
- **THEN** 系统SHALL显示所有解锁访问记录,按时间倒序排列
