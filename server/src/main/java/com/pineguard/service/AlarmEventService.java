package main.java.com.pineguard.service;

import main.java.com.pineguard.notifier.NotificationStrategyFactory;
import main.java.com.pineguard.notifier.Notifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class AlarmEventService {
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationStrategyFactory notifierFactory;

    public AlarmEventService(RedisTemplate<String, String> redisTemplate, NotificationStrategyFactory notifierFactory) {
        this.redisTemplate = redisTemplate;
        this.notifierFactory = notifierFactory;
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

            List<Notifier> notifiers = notifierFactory.createNotifiers(deviceId);
            for (Notifier notifier : notifiers) {
                notifier.notify(deviceId, "跌倒报警");
            }
        } finally {
            // 可选：提前释放锁 (自动过期也可)
        }
    }
}
