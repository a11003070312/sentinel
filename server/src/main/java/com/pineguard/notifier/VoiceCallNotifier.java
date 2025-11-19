package com.pineguard.notifier;

import org.springframework.stereotype.Service;

@Service
public class VoiceCallNotifier implements Notifier {
    @Override
    public void notify(String deviceId, String message) {
        System.out.println("[VOICE] 拨打监护人电话 -> " + deviceId + ": " + message);
    }
}
