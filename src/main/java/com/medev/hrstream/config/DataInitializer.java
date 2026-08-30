package com.medev.hrstream.config;

import com.medev.hrstream.candidate.Candidate;
import com.medev.hrstream.candidate.CandidateRepository;
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
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@hrstream.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.seed.admin.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;

        seedStaffUser(adminEmail, adminPassword, "Admin", "HrStream", Role.ADMIN);
        seedStaffUser("hr@hrstream.com", "hr123456", "HR", "Manager", Role.HR);
        seedCandidate();
    }

    private void seedStaffUser(String email, String password, String firstName, String lastName, Role role) {
        if (userRepository.findByEmail(email).isPresent()) return;

        User user = User.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("Seeded demo {} user: {}", role, email);
    }

    private void seedCandidate() {
        String email = "candidate@hrstream.com";
        if (candidateRepository.findByEmail(email).isPresent()) return;

        Candidate candidate = Candidate.builder()
                .firstName("Demo")
                .lastName("Candidate")
                .email(email)
                .password(passwordEncoder.encode("candidate123"))
                .build();

        candidateRepository.save(candidate);
        log.info("Seeded demo candidate: {}", email);
    }
}
