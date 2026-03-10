package com.medev.hrstream.candidate;

import com.medev.hrstream.user.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CandidateUserDetailsService implements UserDetailsService {

    private final CandidateRepository candidateRepository;

    public CandidateUserDetailsService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Candidate not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(candidate.getEmail())
                .password(candidate.getPassword())
                .roles(Role.CANDIDATE.name())
                .build();
    }
}
