package com.medev.hrstream.config;

import com.medev.hrstream.security.JwtService;
import com.medev.hrstream.user.CompositeUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CompositeUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CompositeUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String jwt = authHeader.substring(7);
            System.out.println("DEBUG: Found JWT in header: " + jwt);

            if (jwtService.validateToken(jwt)) {
                String email = jwtService.getEmailFromToken(jwt);
                System.out.println("DEBUG: JWT is valid for email: " + email);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("DEBUG: Authentication set in context for: " + email);
            } else {
                System.out.println("DEBUG: JWT validation failed");
            }
        }

        filterChain.doFilter(request, response);
    }
}
