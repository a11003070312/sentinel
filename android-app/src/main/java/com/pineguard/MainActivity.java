package com.pineguard;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.MotionEvent;
import androidx.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;
import com.pineguard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CountDownTimer longPressTimer;
    private Vibrator vibrator;
    private MediaPlayer pressSound;
    private MqttRepository mqtt;
    private String deviceId;

    private static final long HOLD_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        pressSound = MediaPlayer.create(this, R.raw.sound_click);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mqtt = new MqttRepository();
        try {
            mqtt.connect("tcp://broker.emqx.io:1883", "pg-" + deviceId);
        } catch (Exception e) {
            // ignore
        }

        startService(new Intent(this, FallDetectionService.class));

        setupSosButton();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSosButton() {
        binding.btnSos.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAccumulating();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancelAccumulating();
                    return true;
            }
            return false;
        });
    }

    private void startAccumulating() {
        binding.btnSos.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        longPressTimer = new CountDownTimer(HOLD_DURATION, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((HOLD_DURATION - millisUntilFinished) * 100 / HOLD_DURATION);
                binding.progressRing.setProgress(progress);
            }

            @Override
            public void onFinish() {
                binding.progressRing.setProgress(100);
                triggerAlarm();
            }
        }.start();
    }

    private void cancelAccumulating() {
        if (longPressTimer != null) {
            longPressTimer.cancel();
        }
        binding.btnSos.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
        binding.progressRing.setProgress(0);
    }

    private void triggerAlarm() {
        if (pressSound != null) pressSound.start();
        Intent intent = new Intent(MainActivity.this, AlertActivity.class);
        startActivity(intent);
    }
}
