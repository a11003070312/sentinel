package com.pineguard.notifier;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationStrategyFactory {
    private final SmsNotifier sms;
    private final VoiceCallNotifier voice;
    private final WeChatNotifier wechat;

    public NotificationStrategyFactory(SmsNotifier sms, VoiceCallNotifier voice, WeChatNotifier wechat) {
        this.sms = sms;
        this.voice = voice;
        this.wechat = wechat;
    }

    public List<Notifier> createNotifiers(String deviceId) {
        // 伪代码：根据设备或用户配置构造策略链
        List<Notifier> list = new ArrayList<>();
        list.add(sms);
        list.add(voice);
        list.add(wechat);
        return list;
    }
}
