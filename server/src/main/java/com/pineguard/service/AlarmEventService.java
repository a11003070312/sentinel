package com.pineguard.service;

import com.pineguard.notifier.NotificationStrategyFactory;
import com.pineguard.notifier.Notifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class AlarmEventService {
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationStrategyFactory notifierFactory;
    private final AlarmEventPublisher publisher;

    public AlarmEventService(RedisTemplate<String, String> redisTemplate, NotificationStrategyFactory notifierFactory, AlarmEventPublisher publisher) {
        this.redisTemplate = redisTemplate;
        this.notifierFactory = notifierFactory;
        this.publisher = publisher;
    }

    public void triggerAlarm(String deviceId, String payload) {
        String lockKey = "lock:alarm:" + deviceId;

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(locked)) {
            System.out.println("防抖: 设备 " + deviceId + " 报警已在处理");
            return;
        }

        try {
            System.out.println("触发报警: " + deviceId + " - " + payload);

            // 异步发布到优先级队列
            publisher.publish(deviceId, payload);
        } finally {
            // 可选：提前释放锁 (自动过期也可)
        }
    }
}
