package com.pineguard.handler;

import com.pineguard.service.AlarmEventService;
import org.springframework.stereotype.Service;

@Service
public class MqttInboundHandler {
    private final AlarmEventService alarmEventService;

    public MqttInboundHandler(AlarmEventService alarmEventService) {
        this.alarmEventService = alarmEventService;
    }

    // 伪代码：供 MQTT 集成调用
    public void onAlarmMessage(String deviceId, String payload) {
        alarmEventService.triggerAlarm(deviceId, payload);
    }
}
