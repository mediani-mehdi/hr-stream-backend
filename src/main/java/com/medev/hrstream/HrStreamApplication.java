package com.medev.hrstream;

import com.medev.hrstream.config.AiProviderProperties;
import com.medev.hrstream.config.ScoringProperties;
import com.medev.hrstream.user.Role;
import com.medev.hrstream.user.User;
import com.medev.hrstream.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties({ScoringProperties.class, AiProviderProperties.class})
@SpringBootApplication
public class HrStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrStreamApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@hrstream.local").isEmpty()) {
                User admin = User.builder()
                        .firstname("Admin")
                        .lastname("User")
                        .email("admin@hrstream.local")
                        .password(passwordEncoder.encode("Admin1234"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
