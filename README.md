# 校园智能饭搭子系统 (Campus Intelligent MealBuddy System)

一个基于 Spring Boot + Vue 3 的校园约饭匹配平台，帮助在校学生找到志同道合的饭搭子，支持智能推荐、信用评价和 AI 美食助手。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7+ |
| 安全认证 | Spring Security + JWT |
| 数据库 | MySQL 8.0 + Spring Data JPA |
| 前端框架 | Vue 3 (Composition API) |
| 状态管理 | Pinia |
| UI 组件库 | Element Plus |
| 构建工具 | Vite / Maven |
| AI 集成 | DeepSeek Chat API |

## 功能模块

- **用户系统** — 注册/登录、JWT 认证、个人资料管理、头像上传
- **约饭广场** — 浏览/搜索约饭活动、按食堂/口味/时间筛选
- **组局管理** — 发起约饭、申请加入、成员管理、状态流转
- **信用评价** — 信用分机制、互评系统、匿名评价
- **智能推荐** — 基于口味偏好和信用分的匹配算法
- **AI 助手** — 接入 DeepSeek，推荐菜品、搭配建议
- **消息通知** — 申请通知、信用分变动、活动提醒
- **管理后台** — 用户管理、活动管理、数据统计（管理员专属）

## 项目结构

```
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/mealbuddy/
│   │   ├── config/              # 安全、跨域、Web 配置
│   │   ├── controller/          # REST 控制器
│   │   ├── dto/                 # 数据传输对象
│   │   ├── entity/              # JPA 实体
│   │   ├── exception/           # 全局异常处理
│   │   ├── repository/          # 数据访问层
│   │   ├── security/            # JWT 过滤器与令牌工具
│   │   ├── service/             # 业务逻辑层
│   │   └── util/                # 工具类
│   └── src/main/resources/
│       └── db/init.sql          # 数据库初始化脚本
├── frontend/                    # Vue 3 前端
│   └── src/
│       ├── api/                 # API 请求封装
│       ├── data/                # 食堂数据
│       ├── router/              # 路由配置
│       ├── stores/              # Pinia 状态管理
│       └── views/               # 页面组件
└── .gitignore
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+

### 1. 配置数据库

创建 MySQL 数据库（应用启动时会自动建表）：

```sql
CREATE DATABASE IF NOT EXISTS mealbuddy DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置应用

复制配置模板并填入实际值：

```bash
cp backend/src/main/resources/application-template.yml backend/src/main/resources/application.yml
```

编辑 `application.yml`，修改数据库密码、JWT 密钥和 AI API Key。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:3000`

### 5. 初始化数据

应用首次启动后会自动初始化食堂数据。管理员账号需通过数据库手动设置 `role='ADMIN'`。

## API 概览

| 前缀 | 说明 |
|------|------|
| `/api/auth/**` | 注册/登录（免认证） |
| `/api/user/**` | 用户资料、头像、偏好 |
| `/api/group/**` | 约饭活动 CRUD |
| `/api/evaluation/**` | 信用评价 |
| `/api/notification/**` | 消息通知 |
| `/api/ai/**` | AI 美食助手 |
| `/api/admin/**` | 管理后台（管理员） |
| `/uploads/**` | 静态资源（头像等） |

## 信用分机制

- 初始信用分：80
- 完成约饭后互评，评分影响信用分
- 低于 60 分为信用受限，无法发起/加入约饭
- 信用分变动会推送通知
