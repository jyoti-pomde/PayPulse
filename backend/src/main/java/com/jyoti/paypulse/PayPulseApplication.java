package com.jyoti.paypulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PayPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayPulseApplication.class, args);
    }
}
