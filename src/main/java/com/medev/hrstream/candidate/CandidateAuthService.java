package com.medev.hrstream.candidate;

import com.medev.hrstream.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CandidateAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public CandidateAuthService(AuthenticationManager authenticationManager, JwtService jwtService, CandidateRepository candidateRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CandidateAuthResponse register(CandidateRegisterRequest request) {
        if (candidateRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        var candidate = Candidate.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .niveauEtude(request.getNiveauEtude())
                .domaineExpertise(request.getDomaineExpertise())
                .experienceProfessionnelle(request.getExperienceProfessionnelle())
                .build();
        candidateRepository.save(candidate);

        var springUser = org.springframework.security.core.userdetails.User
                .withUsername(candidate.getEmail())
                .password(candidate.getPassword())
                .roles("CANDIDATE")
                .build();

        var jwtToken = jwtService.generateToken(springUser);
        return CandidateAuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public CandidateAuthResponse login(CandidateLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);

            return CandidateAuthResponse.builder()
                    .token(token)
                    .build();
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }
}
