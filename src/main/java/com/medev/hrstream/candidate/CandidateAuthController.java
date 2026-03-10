package com.medev.hrstream.candidate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidate/auth")
public class CandidateAuthController {

    private final CandidateAuthService candidateAuthService;

    public CandidateAuthController(CandidateAuthService candidateAuthService) {
        this.candidateAuthService = candidateAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<CandidateAuthResponse> register(@RequestBody CandidateRegisterRequest request) {
        return ResponseEntity.ok(candidateAuthService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<CandidateAuthResponse> login(@RequestBody CandidateLoginRequest request) {
        return ResponseEntity.ok(candidateAuthService.login(request));
    }
}
