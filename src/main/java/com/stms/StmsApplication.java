package com.stms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(StmsApplication.class, args);
    }
}
