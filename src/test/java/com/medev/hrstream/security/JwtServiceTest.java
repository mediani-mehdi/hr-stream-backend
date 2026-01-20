package com.medev.hrstream.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generatesAndValidatesToken() throws Exception {
        JwtService jwtService = new JwtService();

        // Set fields without Spring context
        setField(jwtService, "secret", "MyVeryLongJwtSecretKey_AtLeast32Chars");
        setField(jwtService, "expiration", 60_000L);

        User user = new User("user@example.com", "pw", AuthorityUtils.NO_AUTHORITIES);

        String token = jwtService.generateToken(user);
        assertThat(token).isNotBlank();

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getEmailFromToken(token)).isEqualTo("user@example.com");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

