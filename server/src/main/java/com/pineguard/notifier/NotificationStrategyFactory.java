package com.pineguard.notifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationStrategyFactory {
    private final SmsNotifier sms;
    private final VoiceCallNotifier voice;
    private final WeChatNotifier wechat;

    @Value("${notify.sms.enabled:false}")
    private boolean smsEnabled;
    @Value("${notify.voice.enabled:false}")
    private boolean voiceEnabled;
    @Value("${notify.wechat.enabled:false}")
    private boolean wechatEnabled;

    public NotificationStrategyFactory(SmsNotifier sms, VoiceCallNotifier voice, WeChatNotifier wechat) {
        this.sms = sms;
        this.voice = voice;
        this.wechat = wechat;
    }

    public List<Notifier> createNotifiers(String deviceId) {
        List<Notifier> list = new ArrayList<>();
        if (smsEnabled) list.add(sms);
        if (voiceEnabled) list.add(voice);
        if (wechatEnabled) list.add(wechat);
        return list;
    }
}
