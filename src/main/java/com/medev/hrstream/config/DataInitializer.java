package com.medev.hrstream.config;

import com.medev.hrstream.user.Role;
import com.medev.hrstream.user.User;
import com.medev.hrstream.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@hrstream.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@1234}")
    private String adminPassword;

    @Value("${app.seed.admin.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.debug("Seed admin already exists, skipping.");
            return;
        }

        User admin = User.builder()
                .firstname("Admin")
                .lastname("HrStream")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Seeded default admin user: {}", adminEmail);
    }
}
