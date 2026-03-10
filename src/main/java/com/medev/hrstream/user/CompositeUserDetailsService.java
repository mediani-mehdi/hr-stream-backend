package com.medev.hrstream.user;

import com.medev.hrstream.candidate.CandidateUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CompositeUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final CandidateUserDetailsService candidateUserDetailsService;

    public CompositeUserDetailsService(UserService userService, CandidateUserDetailsService candidateUserDetailsService) {
        this.userService = userService;
        this.candidateUserDetailsService = candidateUserDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return candidateUserDetailsService.loadUserByUsername(username);
        }
    }
}
