package com.grain.monitoring.mqtt;

import java.time.LocalDateTime;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grain.monitoring.entity.AlarmConfig;
import com.grain.monitoring.entity.AlarmLog;
import com.grain.monitoring.entity.SensorData;
import com.grain.monitoring.mapper.AlarmConfigMapper;
import com.grain.monitoring.mapper.AlarmLogMapper;
import com.grain.monitoring.mapper.SensorDataMapper;
import com.grain.monitoring.websocket.MonitoringWebSocketHandler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MqttSubscriber implements MqttCallback {

    @Value("${spring.mqtt.broker-url}")
    private String brokerUrl;

    @Value("${spring.mqtt.client-id}")
    private String clientId;

    @Value("${spring.mqtt.default-topic}")
    private String topic;

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private AlarmConfigMapper alarmConfigMapper;

    @Autowired
    private AlarmLogMapper alarmLogMapper;

    @Autowired
    private MonitoringWebSocketHandler webSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MqttClient mqttClient;

    @PostConstruct
    public void init() {
        try {
            mqttClient = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.setCallback(this);
            mqttClient.connect(options);
            mqttClient.subscribe(topic);
            log.info("MQTT Subscriber connected to {} and subscribed to {}", brokerUrl, topic);
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost, attempting to reconnect...");
        init();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        log.debug("Received MQTT message: {}", payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String deviceId = jsonNode.get("deviceId").asText();
            double temperature = jsonNode.get("temperature").asDouble();
            double humidity = jsonNode.get("humidity").asDouble();

            // 1. 保存传感器数据
            SensorData sensorData = new SensorData();
            sensorData.setDeviceId(deviceId);
            sensorData.setTemperature(temperature);
            sensorData.setHumidity(humidity);
            sensorData.setCollectTime(LocalDateTime.now());
            sensorDataMapper.insert(sensorData);

            // 2. 检查阈值并生成报警
            checkThresholds(deviceId, temperature, humidity);

            // 3. 通过 WebSocket 推送到前端
            webSocketHandler.broadcast(payload);

        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
        }
    }

    private void checkThresholds(String deviceId, double temperature, double humidity) {
        AlarmConfig config = alarmConfigMapper.selectOne(
            new LambdaQueryWrapper<AlarmConfig>().eq(AlarmConfig::getDeviceId, deviceId)
        );

        if (config == null) return;

        if (temperature > config.getMaxTemp()) {
            saveAlarmLog(deviceId, "TEMPERATURE_HIGH", temperature, config.getMaxTemp());
        } else if (temperature < config.getMinTemp()) {
            saveAlarmLog(deviceId, "TEMPERATURE_LOW", temperature, config.getMinTemp());
        }

        if (humidity > config.getMaxHum()) {
            saveAlarmLog(deviceId, "HUMIDITY_HIGH", humidity, config.getMaxHum());
        } else if (humidity < config.getMinHum()) {
            saveAlarmLog(deviceId, "HUMIDITY_LOW", humidity, config.getMinHum());
        }
    }

    private void saveAlarmLog(String deviceId, String type, double value, double threshold) {
        AlarmLog log = new AlarmLog();
        log.setDeviceId(deviceId);
        log.setAlarmType(type);
        log.setAlarmValue(value);
        log.setThresholdValue(threshold);
        log.setStatus(0);
        log.setCreateTime(LocalDateTime.now());
        alarmLogMapper.insert(log);
        
        // 推送报警信息到前端
        String alarmMsg = String.format("{\"type\": \"ALARM\", \"deviceId\": \"%s\", \"alarmType\": \"%s\", \"value\": %.2f}", 
                                       deviceId, type, value);
        webSocketHandler.broadcast(alarmMsg);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
