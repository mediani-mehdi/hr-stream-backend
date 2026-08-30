package com.medev.hrstream;

import com.medev.hrstream.config.AiProviderProperties;
import com.medev.hrstream.config.ScoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties({ScoringProperties.class, AiProviderProperties.class})
@SpringBootApplication
public class HrStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrStreamApplication.class, args);
    }

}
