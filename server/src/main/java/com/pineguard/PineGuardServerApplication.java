package main.java.com.pineguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PineGuardServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PineGuardServerApplication.class, args);
    }
}
