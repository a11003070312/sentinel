package com.pineguard.service;

import com.pineguard.notifier.NotificationStrategyFactory;
import com.pineguard.notifier.Notifier;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlarmDispatchConsumer {
    private final NotificationStrategyFactory factory;

    public AlarmDispatchConsumer(NotificationStrategyFactory factory) {
        this.factory = factory;
    }

    @RabbitListener(queues = "sentinel.alarm.triggered")
    public void onMessage(String json) {
        // 简化解析：只需拿到 deviceId
        String deviceId = json.contains("\"deviceId\":\"") ? json.split("\"deviceId\":\"")[1].split("\"")[0] : "unknown";
        List<Notifier> notifiers = factory.createNotifiers(deviceId);
        for (Notifier n : notifiers) {
            n.notify(deviceId, "跌倒报警");
        }
    }
}
