# 课程签到助手 (Check-in Assistant) - Android移动应用

## 应用名称
**课程签到助手** (Check-in Assistant)

## 应用简介
课程签到助手是一款专为大学生设计的课程签到管理系统Android应用。本应用作为《移动应用开发》课程的毕业设计项目，集成了微信小程序跳转功能，为学生提供便捷的课程管理和签到服务。

## 应用功能详情

### 核心功能
- 📚 **课程管理**：添加、编辑、删除课程信息
- ✅ **在线签到**：一键快速签到，记录考勤信息  
- 📅 **课表查看**：直观的课程时间表展示
- 🔗 **微信集成**：无缝跳转微信小程序生态
- 💾 **数据存储**：使用Room数据库本地存储数据
- 🌐 **网络请求**：支持在线数据同步和更新

### 特色服务
- 简洁直观的用户界面设计
- 高效的签到流程优化
- 与微信生态深度集成
- 稳定的本地数据管理
- 完善的错误处理机制

## 技术架构

### 开发环境
- **开发工具**: Android Studio
- **编程语言**: Java
- **目标SDK**: Android API 34
- **最低SDK**: Android API 24

### 技术栈
- **UI框架**: Material Design + ViewBinding
- **数据存储**: Room Database
- **网络请求**: OkHttp + Jsoup
- **微信集成**: 微信开放平台SDK
- **架构模式**: MVC模式

## 项目结构
CheckApp/
├── app/src/main/java/com/example/check/
│ ├── MainActivity.java # 主界面
│ ├── LoginActivity.java # 登录界面
│ ├── CourseListActivity.java # 课程列表
│ ├── SignInActivity.java # 签到界面
│ ├── CourseTableActivity.java # 课表界面
│ ├── MyApplication.java # 应用初始化
│ └── utils/
│ └── WeChatManager.java # 微信管理工具类
│ └── wxapi/
│ └── WXEntryActivity.java # 微信回调处理
├── app/src/main/res/ # 资源文件
└── app/src/main/AndroidManifest.xml


## 微信集成说明

### 集成功能
- 微信SDK接入和初始化
- 小程序跳转功能实现
- 微信回调处理机制
- 错误状态码处理

### 配置信息
- **目标微信AppID**: wx13439bc546007458
- **目标小程序ID**: gh_d2d41b77389b
- **应用包名**: com.example.check

## 安装和使用

### 系统要求
- Android 7.0及以上版本
- 微信客户端（用于小程序跳转功能）
- 网络连接（部分功能需要）

### 安装步骤
1. 下载APK文件到Android设备
2. 安装应用并授予必要权限
3. 启动应用开始使用

## 项目信息

### 学术声明
**项目性质**: 大学生课程设计作业  
**开发目的**: 《移动应用开发》课程毕业设计  
**开发者**: Bahamut513  
**邮箱**: yc050103@qq.com  

### 开发说明
- 本项目仅用于学术研究和课程作业
- 所有代码均为原创课程设计作品
- 不用于任何商业用途

## 联系方式

**开发者**: Bahamut513  
**邮箱**: yc050103@qq.com  

---

*项目最后更新: 2025年11月27日
