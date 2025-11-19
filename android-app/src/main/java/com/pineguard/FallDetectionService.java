package com.pineguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FallDetectionService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "pineguard_monitor";
    private SensorManager sensorManager;
    private Sensor accel, gyro;
    private SensorRingBuffer ringBuffer;
    private HandlerThread inferenceThread;
    private Handler inferenceHandler;
    private TFLiteClassifier classifier = new TFLiteClassifier();

    private static final float THRESHOLD_IMPACT = 24.5f; // ~2.5g

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("松柏守护正在保护您")
                .setContentText("跌倒检测与保活运行中")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .build();
        startForeground(1, notif);

        ringBuffer = new SensorRingBuffer(128);

        inferenceThread = new HandlerThread("InferenceThread");
        inferenceThread.start();
        inferenceHandler = new Handler(inferenceThread.getLooper());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "PineGuard Monitoring", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) sensorManager.unregisterListener(this);
        if (inferenceThread != null) inferenceThread.quitSafely();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = new float[]{event.values[0], event.values[1], event.values[2], 0, 0, 0};
            ringBuffer.add(values);

            float svm = SignalProcessor.calculateSVM(event.values[0], event.values[1], event.values[2]);
            if (svm > THRESHOLD_IMPACT) {
                inferenceHandler.post(() -> {
                    float[][] window = ringBuffer.getSortedData();
                    float prob = classifier.predict(window);
                    if (prob > 0.85f) {
                        Intent i = new Intent(FallDetectionService.this, AlertActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                });
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float[] last = new float[]{0,0,0, event.values[0], event.values[1], event.values[2]};
            ringBuffer.add(last);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
