package com.medev.hrstream.auth;

import com.medev.hrstream.security.JwtService;
import com.medev.hrstream.user.User;
import com.medev.hrstream.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        userRepository.save(user);

        var springUser = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        var jwtToken = jwtService.generateTokenWithClaims(springUser, user.getFirstname(), user.getLastname());
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails principal = (UserDetails) authentication.getPrincipal();
            
            // fetch user from db to get firstname/lastname
            String token;
            var optionalUser = userRepository.findByEmail(principal.getUsername());
            if (optionalUser.isPresent()) {
                token = jwtService.generateTokenWithClaims(principal, optionalUser.get().getFirstname(), optionalUser.get().getLastname());
            } else {
                // Check candidate
                token = jwtService.generateToken(principal); // Can add Candidate name lookup later if needed
            }

            return AuthResponse.builder()
                    .token(token)
                    .build();
        } catch (BadCredentialsException ex) {
            // Swagger/UI should show a JSON 401 with a message instead of generic 403
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    /**
     * Forgot password:
     * - Always returns accepted=true (even if user not found) to prevent user enumeration.
     * - Generates a one-time token, stores hash + expiry.
     * - In production you should email the token/link.
     */
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        String email = request.getEmail().trim().toLowerCase();

        // Default: do not reveal if user exists
        ForgotPasswordResponse.ForgotPasswordResponseBuilder response = ForgotPasswordResponse.builder()
                .accepted(true)
                .resetToken(null);

        userRepository.findByEmail(email).ifPresent(user -> {
            String rawToken = generateResetToken();
            String tokenHash = sha256Base64(rawToken);

            user.setPasswordResetTokenHash(tokenHash);
            user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);

            // For now we return token for dev/testing; remove this in prod.
            response.resetToken(rawToken);
        });

        return response.build();
    }

    /**
     * Reset password:
     * - Validates token hash and expiration.
     * - Updates password and clears reset token fields.
     */
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }

        String tokenHash = sha256Base64(request.getToken().trim());

        User user = userRepository.findByPasswordResetTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);

        return ResetPasswordResponse.builder().success(true).build();
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Base64(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}
