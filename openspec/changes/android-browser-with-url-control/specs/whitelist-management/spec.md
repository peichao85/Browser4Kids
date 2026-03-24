## ADDED Requirements

### Requirement: 添加白名单规则
系统SHALL允许家长添加新的白名单URL规则。

#### Scenario: 添加域名规则
- **WHEN** 家长输入域名(如example.com)并保存
- **THEN** 系统SHALL将该规则添加到白名单中

#### Scenario: 添加精确URL规则
- **WHEN** 家长输入完整URL(如https://example.com/page)并保存
- **THEN** 系统SHALL将该精确URL规则添加到白名单中

#### Scenario: 添加通配符规则
- **WHEN** 家长输入包含通配符的规则(如*.example.com)并保存
- **THEN** 系统SHALL验证规则格式,有效则添加到白名单

#### Scenario: 重复规则检测
- **WHEN** 家长添加已存在的规则
- **THEN** 系统SHALL提示规则已存在,不重复添加

### Requirement: 删除白名单规则
系统SHALL允许家长删除已有的白名单规则。

#### Scenario: 删除单条规则
- **WHEN** 家长选择某条规则并点击删除
- **THEN** 系统SHALL从白名单中移除该规则

#### Scenario: 确认删除
- **WHEN** 家长执行删除操作
- **THEN** 系统SHALL显示确认对话框,确认后才删除

### Requirement: 查看白名单规则
系统SHALL以列表形式展示所有白名单规则。

#### Scenario: 显示规则列表
- **WHEN** 家长打开白名单管理界面
- **THEN** 系统SHALL显示所有已配置的白名单规则

#### Scenario: 规则分类显示
- **WHEN** 显示规则列表时
- **THEN** 系统SHALL标识规则类型(域名、精确URL、通配符)

### Requirement: 编辑白名单规则
系统SHALL允许家长修改已有的白名单规则。

#### Scenario: 编辑规则内容
- **WHEN** 家长选择某条规则并编辑
- **THEN** 系统SHALL允许修改规则内容并保存

#### Scenario: 编辑验证
- **WHEN** 家长保存编辑后的规则
- **THEN** 系统SHALL验证新规则格式,有效才保存

### Requirement: 白名单数据持久化
系统MUST持久化保存白名单规则数据。

#### Scenario: 保存规则到本地存储
- **WHEN** 家长添加、修改或删除规则
- **THEN** 系统SHALL立即将变更保存到本地数据库

#### Scenario: 应用启动加载规则
- **WHEN** 应用启动时
- **THEN** 系统SHALL从本地存储加载所有白名单规则

### Requirement: 预置白名单
系统SHALL提供儿童友好网站的预置白名单选项。

#### Scenario: 首次启动推荐网站
- **WHEN** 应用首次启动时
- **THEN** 系统SHALL显示推荐的儿童友好网站列表,供家长选择添加

#### Scenario: 批量导入预置规则
- **WHEN** 家长选择预置网站列表
- **THEN** 系统SHALL批量添加选中的网站到白名单
