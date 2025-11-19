package com.pineguard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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

    public void loadModelFromAsset(Context context, String assetName) {
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(assetName);
            FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
            FileChannel channel = fis.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, afd.getStartOffset(), afd.getLength());
            synchronized (lock) {
                this.tflite = new Interpreter(buffer);
            }
        } catch (Exception e) {
            // ignore: 模型缺失时保持空解释器
        }
    }
}
