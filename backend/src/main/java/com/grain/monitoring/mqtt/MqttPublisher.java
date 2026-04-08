package com.grain.monitoring.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MqttPublisher {

    @Value("${spring.mqtt.broker-url}")
    private String brokerUrl;

    @Value("${spring.mqtt.client-id}")
    private String clientId;

    private MqttClient mqttClient;

    @PostConstruct
    public void init() {
        try {
            // 模拟器使用独立的 client id
            mqttClient = new MqttClient(brokerUrl, clientId + "_simulator");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);
            log.info("MQTT Simulator Publisher connected to {}", brokerUrl);
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker for simulation", e);
        }
    }

    public void publish(String topic, String payload) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                mqttClient.publish(topic, message);
                log.debug("Published simulation data to {}: {}", topic, payload);
            } catch (MqttException e) {
                log.error("Failed to publish MQTT message", e);
            }
        }
    }
}
