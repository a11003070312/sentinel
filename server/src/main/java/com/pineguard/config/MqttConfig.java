package com.pineguard.config;

import com.pineguard.handler.MqttInboundHandler;
import com.pineguard.service.DeviceKeepAliveService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory f = new DefaultMqttPahoClientFactory();
        f.setServerURIs("tcp://localhost:1883");
        f.setCleanSession(false);
        return f;
    }

    @Bean
    public MessageChannel mqttInputChannel() { return new DirectChannel(); }

    @Bean
    public IntegrationFlow mqttInboundFlow(MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "server-consumer", factory, "sentinel/device/+/alarm", "sentinel/device/+/telemetry");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return IntegrationFlows.from(adapter).channel(mqttInputChannel()).get();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public org.springframework.messaging.MessageHandler inboundHandler(MqttInboundHandler handler, DeviceKeepAliveService keepAliveService) {
        return message -> {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = (String) message.getPayload();
            if (topic == null) return;
            // topic: sentinel/device/{id}/...
            String[] parts = topic.split("/");
            String deviceId = parts.length >= 3 ? parts[2] : "unknown";
            if (topic.endsWith("/alarm")) {
                handler.onAlarmMessage(deviceId, payload);
            } else if (topic.endsWith("/telemetry")) {
                keepAliveService.updateOnline(deviceId);
            }
        };
    }
}
