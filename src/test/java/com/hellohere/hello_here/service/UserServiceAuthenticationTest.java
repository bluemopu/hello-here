package com.hellohere.hello_here.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hellohere.hello_here.entity.User;
import com.hellohere.hello_here.repository.UserProfileRepository;
import com.hellohere.hello_here.repository.UserRepository;

class UserServiceAuthenticationTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(
            userRepository, userProfileRepository, passwordEncoder);

    @Test
    void loginReturnsActiveUserWhenPasswordMatches() {
        User user = new User();
        user.setPasswordHash("encoded-password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        User result = userService.login("test@example.com", "password");

        assertSame(user, result);
        verify(passwordEncoder).matches("password", "encoded-password");
    }

    @Test
    void loginRejectsDeletedUserWithGenericMessage() {
        User user = new User();
        user.setDeletedAt(LocalDateTime.now());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.login("test@example.com", "password"));

        assertEquals("メールアドレスまたはパスワードが違います", exception.getMessage());
    }
}
