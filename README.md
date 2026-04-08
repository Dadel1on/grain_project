# 粮库温湿度监控预警系统

本项目是一套面向粮库场景的物联网温湿度监控与预警平台，适合作为软件工程专业毕业设计展示。系统通过 MQTT 模拟传感器数据采集，后端完成数据落库、阈值判断与预警生成，前端提供实时大屏、设备管理、历史查询和报警收件箱等功能，形成完整的数据闭环。

## 项目目标

粮食仓储对温度和湿度非常敏感，环境失控会直接影响粮食品质与存储安全。本系统的目标是：

- 实时采集粮库各监测点的温湿度数据。
- 对超阈值数据进行自动预警。
- 将监测数据持久化到 MySQL，支持回溯分析。
- 在前端以大屏和卡片化界面直观展示所有监测点状态。
- 为毕业设计答辩提供清晰的数据流与功能演示链路。

## 功能概览

### 1. 首页展示

- 展示系统名称、项目背景和技术定位。
- 说明系统用于粮库温湿度监控与预警。
- 提供进入控制台的快捷入口。

### 2. 实时监控大屏

- 显示所有监测点的在线状态与最新温湿度值。
- 汇总平均温度、平均湿度、在线设备数、实时告警数。
- 支持查看所有传感器指标变化趋势。
- 实时接收 WebSocket 推送的数据。

### 3. 设备管理

- 管理粮库内传感器设备信息。
- 展示设备编号、名称、在线状态等信息。
- 支持新增、修改、删除设备（后端接口已提供基础能力）。

### 4. 预警管理

- 按设备配置温度、湿度上下限阈值。
- 数据超限时自动生成报警记录。
- 提供报警收件箱，支持查看未处理预警。
- 支持一键标记全部已处理。

### 5. 历史数据查询

- 支持按设备查询历史温湿度记录。
- 支持按时间范围筛选。
- 页面展示历史列表与趋势图。

## 技术栈

### 后端

- Spring Boot 3.2.4
- MyBatis-Plus
- MySQL 8.0
- MQTT（Eclipse Paho）
- WebSocket
- Lombok

### 前端

- Vue 3
- Vite
- TypeScript
- Element Plus
- ECharts
- Axios
- Pinia

### 模拟与数据链路

- 内置 SensorSimulator 模拟器定时产生温湿度数据。
- 模拟数据通过 MQTT 发送到公共 Broker：broker.emqx.io:1883。
- 后端订阅 MQTT 主题后完成解析、入库和预警判断。
- WebSocket 将最新数据和报警信息实时推送到前端。

## 系统架构

```text
传感器模拟器 -> MQTT Broker -> 后端订阅服务 -> MySQL落库
                                   └-> WebSocket -> 前端实时刷新
```

数据流说明：

1. 模拟器从设备表中读取在线设备。
2. 每 5 秒生成一批温湿度数据并发送到 MQTT 主题。
3. 后端订阅到消息后写入 `sensor_data`。
4. 系统根据 `alarm_config` 检查是否超限。
5. 若超限则写入 `alarm_log` 并通过 WebSocket 推送。
6. 前端实时更新大屏、卡片和报警状态。

## 目录结构

```text
grain_project/
├─ README.md
├─ backend/
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/grain/monitoring/
│     │  ├─ common/
│     │  ├─ config/
│     │  ├─ controller/
│     │  ├─ entity/
│     │  ├─ mapper/
│     │  ├─ mqtt/
│     │  ├─ service/
│     │  ├─ simulator/
│     │  └─ websocket/
│     └─ resources/
│        ├─ application.yml
│        └─ schema.sql
└─ frontend/
   ├─ package.json
   ├─ vite.config.ts
   └─ src/
      ├─ components/
      ├─ router/
      ├─ views/
      └─ assets/
```

## 数据库设计

系统默认使用 `grain_monitoring` 数据库，初始化脚本位于：`backend/src/main/resources/schema.sql`。

### 1. device_info

设备信息表，用于保存传感器设备基础信息。

- `device_id`：设备唯一标识
- `device_name`：设备名称或位置
- `status`：在线状态，0 离线，1 在线
- `create_time` / `update_time`：时间信息

### 2. sensor_data

传感器历史数据表，用于保存温湿度采样记录。

- `device_id`：设备标识
- `temperature`：温度
- `humidity`：湿度
- `collect_time`：采集时间

### 3. alarm_config

预警阈值配置表，用于保存每个设备的上下限阈值。

- `max_temp` / `min_temp`
- `max_hum` / `min_hum`

### 4. alarm_log

预警日志表，用于保存超限记录。

- `alarm_type`：报警类型，如温度过高、湿度过低等
- `alarm_value`：触发时的实际值
- `threshold_value`：阈值
- `status`：处理状态

### 5. sys_user

管理员账户表，用于系统登录或扩展管理功能。

## 后端接口说明

后端统一前缀为 `/api`。

### 设备接口

- `GET /api/devices`：获取设备列表
- `POST /api/devices`：新增设备
- `PUT /api/devices`：更新设备
- `DELETE /api/devices/{id}`：删除设备

### 传感器数据接口

- `GET /api/sensor-data`
- 支持参数：
  - `deviceId`
  - `startTime`
  - `endTime`

### 预警配置接口

- `GET /api/alarm-configs`：获取阈值配置
- `PUT /api/alarm-configs`：更新阈值配置

### 预警日志接口

- `GET /api/alarm-logs?status=0`：获取未处理预警
- `PUT /api/alarm-logs/{id}/handle`：处理单条报警
- `PUT /api/alarm-logs/handle-all`：批量处理全部报警

## 实时通信说明

### MQTT

- Broker：`tcp://broker.emqx.io:1883`
- 默认主题：`/grain/sensor/data`
- 模拟器发布设备温湿度数据到该主题。

### WebSocket

- 地址：`/ws/monitoring`
- 功能：
  - 推送实时监控数据
  - 推送报警消息
  - 让前端大屏自动刷新

## 运行环境要求

- JDK 17
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

## 快速启动

### 方式一：手动启动后端

1. 进入 backend 目录。
2. 安装并确认本地 MySQL 可用。
3. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

4. 服务默认监听：`http://localhost:8081`

### 方式二：手动启动前端

1. 进入 frontend 目录。
2. 安装依赖并启动：

```bash
cd frontend
npm install
npm run dev
```

3. 前端默认访问：`http://localhost:5173`

## 数据库初始化

1. 执行 `backend/src/main/resources/schema.sql`。
2. 确认已创建数据库 `grain_monitoring`。
3. 按需修改 `backend/src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/grain_monitoring?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

## 答辩演示建议

如果用于毕业设计答辩，可以重点演示以下流程：

1. 从首页进入系统，介绍项目背景和技术栈。
2. 打开实时监控大屏，展示设备在线状态和指标变化。
3. 演示模拟器持续推送数据，说明数据闭环。
4. 调低某个设备的阈值，触发报警并展示报警收件箱。
5. 切换到历史记录页，展示历史查询和趋势图。

## 项目亮点

- 前后端分离，结构清晰，便于扩展。
- MQTT + WebSocket 组合，兼顾设备通信与前端实时刷新。
- 数据落库完整，支持历史追溯和报警记录。
- 适合作为粮库物联网监控系统的毕业设计案例。

## 常见问题

### 1. 页面没有实时数据怎么办？

- 检查后端是否正常启动。
- 检查 MySQL 是否已初始化。
- 检查 MQTT Broker 网络是否可访问。
- 确认设备表中有在线设备，且 `status = 1`。

### 2. 为什么看不到预警？

- 可能当前数据未超过阈值。
- 可以在预警配置中降低某个设备的上下限再观察。

### 3. 为什么历史图表为空？

- 可能当前数据库中还没有 sensor_data 记录。
- 先等待模拟器运行一段时间，再刷新页面。

## 备注

- 默认 MQTT Broker 使用公共地址，演示时无需额外部署设备端。
- 当前系统已具备落库、实时推送和基础管理能力，适合进一步扩展为完整的仓储环境监控平台。

