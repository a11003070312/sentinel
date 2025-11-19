package main.java.com.pineguard.repo;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class TimeSeriesRepo {
    private final InfluxDBClient client;
    private final String bucket;
    private final String org;

    public TimeSeriesRepo(
            @Value("${influx.url:http://localhost:8086}") String url,
            @Value("${influx.token:token}") String token,
            @Value("${influx.bucket:sensor}") String bucket,
            @Value("${influx.org:default}") String org) {
        this.client = InfluxDBClientFactory.create(url, token.toCharArray());
        this.bucket = bucket;
        this.org = org;
    }

    public void writeSensors(String deviceId, float accX, float accY, float accZ, float gyroX, float gyroY, float gyroZ) {
        try (WriteApi writeApi = client.getWriteApi()) {
            Point p = Point.measurement("sensor_data")
                    .addTag("deviceId", deviceId)
                    .addField("acc_x", accX)
                    .addField("acc_y", accY)
                    .addField("acc_z", accZ)
                    .addField("gyro_x", gyroX)
                    .addField("gyro_y", gyroY)
                    .addField("gyro_z", gyroZ)
                    .time(System.currentTimeMillis(), WritePrecision.MS);
            writeApi.writePoint(bucket, org, p);
        }
    }
}
