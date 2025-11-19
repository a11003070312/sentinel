package main.java.com.pineguard.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DeviceKeepAliveService {
    private final StringRedisTemplate redisTemplate;

    public DeviceKeepAliveService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateOnline(String deviceId) {
        redisTemplate.opsForValue().set("device:online:" + deviceId, String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(60));
    }

    @Scheduled(fixedDelay = 60000)
    public void scanOffline() {
        // 简化：真实场景可用 Redis Keyspace 通知或维护设备列表
        // 这里仅示意定时任务存在
    }
}
