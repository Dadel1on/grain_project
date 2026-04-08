-- 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS grain_monitoring DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE grain_monitoring;

-- 1. 设备信息表
CREATE TABLE IF NOT EXISTS device_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(50) NOT NULL UNIQUE COMMENT '设备唯一标识',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称/位置',
    status TINYINT DEFAULT 1 COMMENT '设备状态: 0-离线, 1-在线',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='设备信息表';

-- 2. 温湿度监测数据表
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备标识',
    temperature DECIMAL(5, 2) NOT NULL COMMENT '温度(℃)',
    humidity DECIMAL(5, 2) NOT NULL COMMENT '湿度(%)',
    collect_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    INDEX idx_device_time (device_id, collect_time)
) ENGINE=InnoDB COMMENT='温湿度监测数据表';

-- 3. 预警阈值配置表
CREATE TABLE IF NOT EXISTS alarm_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(50) NOT NULL UNIQUE COMMENT '设备标识',
    max_temp DECIMAL(5, 2) DEFAULT 35.00 COMMENT '最高温度阈值',
    min_temp DECIMAL(5, 2) DEFAULT 0.00 COMMENT '最低温度阈值',
    max_hum DECIMAL(5, 2) DEFAULT 70.00 COMMENT '最高湿度阈值',
    min_hum DECIMAL(5, 2) DEFAULT 20.00 COMMENT '最低湿度阈值',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='预警阈值配置表';

-- 4. 预警记录表
CREATE TABLE IF NOT EXISTS alarm_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备标识',
    alarm_type VARCHAR(20) NOT NULL COMMENT '报警类型: TEMPERATURE_HIGH, TEMPERATURE_LOW, HUMIDITY_HIGH, HUMIDITY_LOW',
    alarm_value DECIMAL(5, 2) NOT NULL COMMENT '触发时的数值',
    threshold_value DECIMAL(5, 2) NOT NULL COMMENT '触发时的阈值',
    status TINYINT DEFAULT 0 COMMENT '处理状态: 0-未处理, 1-已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报警时间'
) ENGINE=InnoDB COMMENT='预警记录表';

-- 5. 管理员表 (简单实现)
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='管理员信息表';

-- 初始数据
INSERT INTO device_info (device_id, device_name) VALUES ('S001', '1号粮仓东侧');
INSERT INTO device_info (device_id, device_name) VALUES ('S002', '1号粮仓西侧');
INSERT INTO device_info (device_id, device_name) VALUES ('S003', '2号粮仓中心');
INSERT INTO device_info (device_id, device_name) VALUES ('S004', '2号粮仓入口');
INSERT INTO device_info (device_id, device_name) VALUES ('S005', '3号粮仓北侧');
INSERT INTO device_info (device_id, device_name) VALUES ('S006', '3号粮仓南侧');
INSERT INTO device_info (device_id, device_name) VALUES ('S007', '4号粮仓(低温仓)');
INSERT INTO device_info (device_id, device_name) VALUES ('S008', '5号粮仓(备用仓)');

INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S001', 30.0, 5.0, 65.0, 30.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S002', 30.0, 5.0, 65.0, 30.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S003', 28.0, 8.0, 60.0, 35.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S004', 28.0, 8.0, 60.0, 35.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S005', 25.0, 5.0, 55.0, 20.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S006', 25.0, 5.0, 55.0, 20.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S007', 20.0, 0.0, 50.0, 15.0);
INSERT INTO alarm_config (device_id, max_temp, min_temp, max_hum, min_hum) VALUES ('S008', 35.0, 0.0, 70.0, 10.0);

INSERT INTO sys_user (username, password, nickname) VALUES ('admin', 'admin123', '系统管理员');
