package com.grain.monitoring.simulator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.grain.monitoring.entity.DeviceInfo;
import com.grain.monitoring.mqtt.MqttPublisher;
import com.grain.monitoring.service.DeviceInfoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SensorSimulator {

    @Autowired
    private MqttPublisher mqttPublisher;

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Value("${spring.mqtt.default-topic}")
    private String topic;

    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 每 5 秒产生一次模拟数据
     */
    @Scheduled(fixedRate = 5000)
    public void simulateSensorData() {
        // 从数据库获取所有在线设备
        List<DeviceInfo> activeDevices = deviceInfoService.list();
        
        for (DeviceInfo device : activeDevices) {
            if (device.getStatus() != 1) continue; // 仅模拟在线设备

            String deviceId = device.getDeviceId();
            // 根据设备ID生成略有差异的模拟温湿度数据，使展示更真实
            double baseTemp = 20.0;
            double baseHum = 50.0;
            
            // 比如 S007 是低温仓
            if ("S007".equals(deviceId)) {
                baseTemp = 10.0;
                baseHum = 40.0;
            } else if (deviceId.hashCode() % 2 == 0) {
                baseTemp = 25.0;
                baseHum = 60.0;
            }

            double temperature = baseTemp + (random.nextDouble() - 0.5) * 10.0; // 波动
            double humidity = baseHum + (random.nextDouble() - 0.5) * 20.0;    // 波动
            
            // 限制范围
            temperature = Math.max(0, Math.min(50, temperature));
            humidity = Math.max(0, Math.min(100, humidity));
            
            // 构建 JSON 消息
            String payload = String.format(
                "{\"deviceId\": \"%s\", \"temperature\": %.2f, \"humidity\": %.2f, \"timestamp\": \"%s\"}",
                deviceId, temperature, humidity, LocalDateTime.now().format(formatter)
            );
            
            // 发布到 MQTT 主题
            mqttPublisher.publish(topic, payload);
        }
    }
}
