package main.java.com.pineguard.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ModelController {

    @GetMapping("/model/latest")
    public ResponseEntity<Map<String, String>> latest() {
        Map<String, String> m = new HashMap<>();
        m.put("version", "v1.0");
        m.put("url", "https://oss.example.com/model_v1.tflite");
        m.put("md5", "deadbeefdeadbeefdeadbeefdeadbeef");
        return ResponseEntity.ok(m);
    }
}
