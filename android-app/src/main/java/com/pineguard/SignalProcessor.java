package com.pineguard;

public class SignalProcessor {
    public static float[] applyLowPassFilter(float[] current, float[] gravity) {
        float alpha = 0.8f;
        float[] filtered = new float[3];
        for (int i = 0; i < 3; i++) {
            gravity[i] = alpha * gravity[i] + (1 - alpha) * current[i];
            filtered[i] = current[i] - gravity[i];
        }
        return filtered;
    }

    public static float calculateSVM(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
