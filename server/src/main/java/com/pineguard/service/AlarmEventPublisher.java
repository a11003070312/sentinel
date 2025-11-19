package com.pineguard.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AlarmEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public AlarmEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String deviceId, String payload) {
        String msg = "{\"deviceId\":\"" + deviceId + "\",\"payload\":" + payload + ",\"ts\":" + System.currentTimeMillis() + "}";
        rabbitTemplate.convertAndSend("sentinel.alarm", "alarm.triggered", msg, m -> {
            m.getMessageProperties().setPriority(10);
            return m;
        });
    }
}
