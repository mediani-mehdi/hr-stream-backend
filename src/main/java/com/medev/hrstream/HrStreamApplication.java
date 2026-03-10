package com.medev.hrstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication()
public class HrStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrStreamApplication.class, args);
    }

}
