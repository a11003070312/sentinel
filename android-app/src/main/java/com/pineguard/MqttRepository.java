package com.pineguard;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttRepository {
    private MqttClient client;

    public void connect(String brokerUrl, String clientId) throws Exception {
        client = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(false);
        client.connect(opts);
    }

    public void publishAlarm(String topic, String payload) throws Exception {
        if (client != null && client.isConnected()) {
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(2);
            client.publish(topic, msg);
        }
    }
}
