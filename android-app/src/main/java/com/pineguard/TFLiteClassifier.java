package com.pineguard;

import org.tensorflow.lite.Interpreter;

public class TFLiteClassifier {
    private Interpreter tflite;
    private final Object lock = new Object();

    public float predict(float[][] data) {
        synchronized (lock) {
            if (tflite == null) {
                return 0f;
            }
            float[] output = new float[1];
            try {
                tflite.run(data, output);
                return output[0];
            } catch (Exception e) {
                return 0f;
            }
        }
    }

    public void setInterpreter(Interpreter interpreter) {
        synchronized (lock) {
            this.tflite = interpreter;
        }
    }
}
