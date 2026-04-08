# 粮库温湿度监控预警系统 

本项目是一套基于物联网技术的粮库环境监控解决方案，专为软件工程专业毕业设计打造。系统通过模拟传感器数据，实现了从数据采集、处理、存储到可视化展示的全流程监控。

## 1. 核心技术栈

- **后端**: Spring Boot 3, MyBatis-Plus, MySQL, MQTT (Eclipse Paho), WebSocket
- **前端**: Vue 3, Vite, Element Plus, ECharts, Axios, Pinia
- **硬件模拟**: 内置传感器模拟器，通过 MQTT 协议定时发布随机温湿度数据

## 2. 系统功能

1. **系统欢迎首页**:
   - 展示项目背景、核心功能概况及技术架构栈信息。
2. **实时监控大屏**:
   - 动态展示各监测点的实时温湿度数值及在线状态。
   - 使用 ECharts 展示温湿度实时变化曲线（最近20个采样点）。
3. **设备管理**:
   - 维护粮库内的传感器设备信息（ID、位置、状态等）。
4. **预警管理**:
   - 自定义温湿度上下限阈值。
   - 系统自动检测异常并记录预警日志。
   - 实时推送预警通知（基于 WebSocket）。
5. **历史数据查询**:
   - 支持按设备、时间范围查询历史监测数据，并以列表形式展示。

## 3. 运行指南

> **推荐方式**：使用项目根目录提供的 PowerShell 脚本一键操作，详见 [3.1 一键脚本](#31-一键脚本windows-powershell)。

### 3.1 一键脚本（Windows PowerShell）

项目根目录提供三个脚本，右键选择"使用 PowerShell 运行"即可，或在终端中执行：

| 脚本 | 用途 |
|------|------|
| `start.ps1` | 一键启动后端（8081）+ 前端（5173），自动打开浏览器 |
| `stop.ps1`  | 一键停止所有服务进程 |
| `pull.ps1`  | 从 GitHub 拉取最新代码，自动处理本地变更 |

```powershell
# 首次运行前解除执行策略限制（仅需一次）
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

# 启动
.\start.ps1

# 停止
.\stop.ps1

# 拉取更新
.\pull.ps1
```

> **注意**：使用脚本前请先完成数据库准备（见 3.2），并在 `backend/src/main/resources/application.yml` 中配置正确的数据库密码。

---

### 3.2 数据库准备

1. 安装 MySQL 8.0+。
2. 执行 `backend/src/main/resources/schema.sql` 脚本，创建数据库 `grain_monitoring` 及相关表结构，并初始化基础数据。
3. 按需修改 `backend/src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/grain_monitoring?...
    username: root
    password: 123456   # ← 改为你的 MySQL 密码
```

### 3.3 后端手动启动

1. 确保安装了 JDK 17 及 Maven 3.6+。
2. 进入 `backend` 目录：

```bash
cd backend
mvn spring-boot:run
```

3. 启动成功后服务地址为 `http://localhost:8081`。
   - 系统启动后将自动连接公共 MQTT Broker（`broker.emqx.io:1883`）并启动传感器模拟器产生数据。

### 3.4 前端手动启动

1. 确保安装了 Node.js v18+。
2. 进入 `frontend` 目录：

```bash
cd frontend
npm install      # 首次运行需安装依赖
npm run dev
```

3. 访问 `http://localhost:5173`。

## 4. 演示要点 (毕设答辩)

1. **数据闭环**: 演示后台 `SensorSimulator` 产生数据 -> MQTT 发送 -> 后台接收并保存数据库 -> WebSocket 推送到前端 -> ECharts 实时更新。
2. **预警触发**: 在“预警管理”中调低某个设备的最高温度阈值，观察系统是否能立即在大屏弹出预警通知。
3. **响应式设计**: 演示前端界面在不同屏幕尺寸下的适配情况（Element Plus 布局）。

