package com.pineguard.notifier;

import org.springframework.stereotype.Service;

@Service
public class WeChatNotifier implements Notifier {
    @Override
    public void notify(String deviceId, String message) {
        System.out.println("[WeChat] 推送模板消息 -> " + deviceId + ": " + message);
    }
}
