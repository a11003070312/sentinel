package com.pineguard;

public class SensorRingBuffer {
    private final float[][] data; // [size][6]
    private int head = 0;
    private final int size;

    public SensorRingBuffer(int size) {
        this.size = size;
        this.data = new float[size][6];
    }

    public synchronized void add(float[] values) {
        System.arraycopy(values, 0, data[head], 0, 6);
        head = (head + 1) % size;
    }

    public synchronized float[][] getSortedData() {
        float[][] result = new float[size][6];
        for (int i = 0; i < size; i++) {
            int index = (head + i) % size;
            System.arraycopy(data[index], 0, result[i], 0, 6);
        }
        return result;
    }
}
