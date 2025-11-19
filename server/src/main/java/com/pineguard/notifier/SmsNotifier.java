package main.java.com.pineguard.notifier;

import org.springframework.stereotype.Service;

@Service
public class SmsNotifier implements Notifier {
    @Override
    public void notify(String deviceId, String message) {
        System.out.println("[SMS] -> " + deviceId + ": " + message);
    }
}
