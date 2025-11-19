package com.pineguard;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pineguard.databinding.ActivityAlertBinding;

public class AlertActivity extends AppCompatActivity {

    private ActivityAlertBinding binding;
    private CountDownTimer alertTimer;
    private ToneGenerator toneGenerator;
    private boolean isAlarmSent = false;
    private MqttRepository mqtt;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mqtt = new MqttRepository();
        try {
            mqtt.connect("tcp://broker.emqx.io:1883", "pg-" + deviceId);
        } catch (Exception e) {
            // ignore
        }

        startCountdown();

        binding.btnCancel.setOnClickListener(v -> cancelAlarm());
        binding.btnHelpNow.setOnClickListener(v -> sendAlarmNow());
    }

    private void startCountdown() {
        alertTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvCountDown.setText(String.valueOf(millisUntilFinished / 1000));
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                flashScreen();
            }

            @Override
            public void onFinish() {
                binding.tvCountDown.setText("0");
                sendAlarmNow();
            }
        }.start();
    }

    private void cancelAlarm() {
        if (alertTimer != null) alertTimer.cancel();
        Toast.makeText(this, "已取消报警，恢复安全状态", Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendAlarmNow() {
        if (isAlarmSent) return;
        if (alertTimer != null) alertTimer.cancel();
        isAlarmSent = true;

        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);

        try {
            mqtt.publishAlarm("sentinel/device/" + deviceId + "/alarm", "{\"prob\":1,\"ts\":" + System.currentTimeMillis() + "}");
        } catch (Exception e) {
            // ignore
        }

        binding.tvCountDown.setTextSize(30);
        binding.tvCountDown.setText("已呼叫");
        binding.btnCancel.setEnabled(false);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:13800000000"));
        startActivity(callIntent);
    }

    private void flashScreen() {
        ObjectAnimator colorFade = ObjectAnimator.ofObject(
                binding.getRoot(),
                "backgroundColor",
                new ArgbEvaluator(),
                Color.WHITE, 0xFFFFE0E0, Color.WHITE);
        colorFade.setDuration(500);
        colorFade.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertTimer != null) alertTimer.cancel();
        if (toneGenerator != null) toneGenerator.release();
    }
}
