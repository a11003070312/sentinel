项目代号建议：**Project Sentinel (哨兵计划)**

**商业名称建议：** **松柏守护 (PineGuard)**
*寓意：在中国文化中，“松柏”象征长寿与坚韧（寿比南山不老松），同时也代表着屹立不倒的守护。*

---

这份方案不再是简单的 Demo 级别，而是按照 **企业级、高可用、可扩展** 的标准制定的 **架构设计文档 (Software Architecture Document)**。鉴于您 8 年的 Java 经验，我们将引入 **DDD (领域驱动设计)**、**IoT 物联网协议**、**时序数据处理** 和 **边缘计算** 等概念。

---

# Project Sentinel：企业级智能跌倒检测系统架构书

## 1. 业务架构与领域模型 (Domain Logic)

我们将系统划分为三个核心领域 (Bounded Contexts)：

1.  **设备域 (Device Context):** 负责传感器数据采集、边缘计算推理、心跳保活。
2.  **关怀域 (Care Context):** 负责跌倒报警、误报反馈、多渠道通知（短信/电话/微信）。
3.  **洞察域 (Insight Context):** 负责传感器数据的存储、回放、模型训练与 OTA 升级。

---

## 2. 总体技术架构图 (Technical Architecture)

采用 **端 (Edge) - 管 (Pipe) - 云 (Cloud)** 的经典物联网架构。

```text
[ 边缘端: Android ]               [ 传输层: MQTT + HTTPS ]            [ 云端: Java Clusters ]
+---------------------+           +------------------------+          +--------------------------+
|  传感器采集 (50Hz)   |           |                        |          |   接入网关 (Netty/EMQX)  |
|  (Accel + Gyro)     |           |  1. 遥测数据 (MQTT)     |--------->|   (Topic: /sensor/up)    |
+---------------------+           |  (QoS 0, 高频压缩)       |          +--------------------------+
|  信号处理 (DSP)      |           |                        |                      |
|  (卡尔曼滤波)        |           |  2. 报警信令 (MQTT)     |---------> [ 消息队列 (RabbitMQ) ]
+---------------------+           |  (QoS 1/2, 必达)        |                      |
|  AI 推理 (TFLite)   |           |                        |          +--------------------------+
|  (CNN 模型)         |           |  3. 业务数据 (HTTPS)    |--------->|   业务微服务集群         |
+---------------------+           |  (配置/联系人/OTA)       |          | (Spring Boot 3 / Cloud)  |
|  本地策略引擎        |           +------------------------+          +--------------------------+
|  (断网亦可报警)      |                                                           |
+---------------------+                                                  +--------------------+
                                                                         | 数据存储层          |
                                                                         | MySQL (用户/配置)   |
                                                                         | InfluxDB (时序数据) |
                                                                         | Redis (设备状态)    |
                                                                         | MinIO (音频/模型)   |
                                                                         +--------------------+
```

---

## 3. 核心技术选型与重难点 (Deep Dive)

### 3.1 通信协议升级：MQTT over TCP

对于实时性要求极高的“保活”和“报警”，WebSocket 稍显臃肿且耗电。建议采用 **MQTT (Message Queuing Telemetry Transport)** 协议。

*   **优势:**
    *   **低带宽/低功耗:** 协议头极小（最小 2 字节），适合老人机流量和电量。
    *   **QoS (服务质量) 机制:**
        *   QoS 0: 传传感器波形数据（丢几个包没事）。
        *   QoS 1/2: 传“跌倒报警”（**必须**送达）。
    *   **遗嘱消息 (Last Will):** 这是一个 Killer Feature。如果老人手机意外断电或断网，Broker 会自动发布一条“设备异常离线”的消息给后端，后端立即触发“失联预警”。
*   **Java 选型:** 后端集成 **EMQX** 或 **HiveMQ** 作为 Broker，Spring Boot 使用 Paho MQTT Client 或 Spring Integration MQTT。

### 3.2 后端架构 (Java Specialist Level)

*   **时序数据库 (Time-Series DB):**
    *   不要把 50Hz 的传感器原始数据存 MySQL，会瞬间爆炸。
    *   **选型:** **InfluxDB** 或 **TDengine**。
    *   **用途:** 存储跌倒前后 10 秒的完整波形数据。这是后续 AI 模型迭代的“金矿”。
*   **异步削峰:**
    *   报警是突发流量。使用 **RabbitMQ** 或 **RocketMQ** 解耦报警接收与短信发送。
    *   `Topic: sentinel.alarm.triggered` -> `SmsHandler`, `VoiceCallHandler`, `WechatPushHandler` 并行消费。
*   **分布式锁:**
    *   防止老人手机摔落时产生的多次弹跳导致并发触发 3-4 次报警 API。使用 Redis `setIfAbsent` (Lock) 锁定该 `deviceId` 30秒。

### 3.3 边缘计算端 (Android Native Professional)

为了达到专业级别，Android 端不能只写应用层代码。

*   **1. 信号预处理 (Signal Processing):**
    *   传感器数据有噪声。在送入 AI 之前，先过一层 **低通滤波器 (Low-Pass Filter)** 或 **卡尔曼滤波 (Kalman Filter)**，去除高频抖动噪声，保留重力分量。
    *   *技术点:* 简单的滤波算法可以直接用 Java 写，如果追求极致性能，可以用 **NDK (C++)** 实现这部分 DSP 逻辑。
*   **2. 前台服务与进程守护:**
    *   实现 `NotificationListenerService` 或 `AccessibilityService`（无障碍服务）。这两种服务在 Android 系统中优先级极高，极难被杀。
    *   *话术:* 申请无障碍权限时，提示用户“为了保证在紧急情况下能自动拨打电话”。
*   **3. 环形缓冲区的内存管理:**
    *   使用 `FloatBuffer` (NIO) 或基本数组实现 RingBuffer，避免频繁 GC 造成的卡顿（STW）。

### 3.4 AI 算法进阶：MLOps 闭环

不仅仅是训练一个模型，而是建立一套机制。

1.  **冷启动:** 使用 SisFall 数据集训练 v1.0 模型 (`sentinel_v1.tflite`)。
2.  **影子模式 (Shadow Mode):**
    *   下发新模型 v2.0 到手机，但**不**根据 v2.0 触发报警，只在后台运行。
    *   如果 v1.0 报警了但 v2.0 没报（或反之），上传日志到后端。
    *   在云端对比分析 v2.0 的表现，确认优于 v1.0 后，再通过配置开启 v2.0 的报警权限。
3.  **误报闭环:**
    *   用户点击“我没事” -> 手机打包过去 10秒 数据 -> 上传 InfluxDB -> 标记为“Hard Negative”样本 -> 重新训练。

---

## 4. 数据库设计精要 (Schema)

**MySQL (关系型):**

```sql
CREATE TABLE device_config (
    device_id VARCHAR(64) PRIMARY KEY,
    sensitivity_level INT DEFAULT 5, -- 灵敏度 1-10
    active_model_version VARCHAR(32), -- 当前使用的模型版本
    sos_contacts JSON, -- 冗余存储，减少联表
    last_heartbeat TIMESTAMP
);
```

**InfluxDB (时序型 - 伪代码):**

```text
Measurement: sensor_data
Tags: device_id=1001, event_type=FALL_TRIGGER
Fields: acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z
Timestamp: 1711111111050
```

---

## 5. 接口定义 (API Specs)

### 5.1 MQTT Topics

*   `sentinel/device/{id}/telemetry`: 上报电量、GPS、信号强度 (QoS 0, Retain=True)。
*   `sentinel/device/{id}/alarm`: 上报跌倒事件 (QoS 2)。
*   `sentinel/device/{id}/cmd`: 下发指令（如：远程响铃查找老人、立即更新配置）。

### 5.2 HTTP API

*   `POST /api/v1/model/check`: 检查模型更新。
    *   *Response:* `{ "version": "2.1", "url": "https://oss.../model_v2.1.tflite", "md5": "..." }`

---

## 6. 开发与部署计划 (Project Plan)

**Sprint 1: 基础设施 (The Foundation)**

*   搭建 EMQX (MQTT Broker) 和 Spring Boot 后端。
*   Android 端实现 MQTT 连接，断线重连，心跳保活。
*   完成“遗嘱消息”处理逻辑，实现“老人失联”报警。

**Sprint 2: 数据流与采集 (The Eyes)**

*   Android 端实现传感器采集 + 卡尔曼滤波。
*   实现 RingBuffer 和数据上传接口（用于采集训练数据）。
*   自行采集 100 组“假摔”和“日常活动”数据。

**Sprint 3: 智能大脑 (The Brain)**

*   Python 训练 TFLite 模型。
*   Android 端集成 TensorFlow Lite 库。
*   实现“阈值 + AI”双重检测逻辑。

**Sprint 4: 业务闭环 (The Shield)**

*   开发 UI（参考之前的设计）。
*   接入阿里云短信/语音。
*   实现误报反馈机制。
