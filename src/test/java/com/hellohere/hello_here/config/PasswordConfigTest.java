package com.hellohere.hello_here.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordConfigTest {

    @Test
    void passwordEncoderUsesOneWayHashThatCanBeVerified() {
        PasswordEncoder encoder = new PasswordConfig().passwordEncoder();

        String encoded = encoder.encode("password");

        assertFalse(encoded.contains("password"));
        assertTrue(encoded.startsWith("$argon2"));
        assertTrue(encoder.matches("password", encoded));
        assertFalse(encoder.matches("different", encoded));
    }
}
