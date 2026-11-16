package com.hellohere.hello_here.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hellohere.hello_here.repository.UserProfileRepository;
import com.hellohere.hello_here.repository.UserRepository;

class UserServiceNameValidationTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(
            userRepository, userProfileRepository, passwordEncoder);

    @Test
    void registerRejectsNameLongerThan50CharactersBeforeSaving() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.register("test@example.com", "あ".repeat(51), "password"));

        assertEquals("ユーザー名は50文字以内で入力してください", exception.getMessage());
        verifyNoInteractions(userRepository, userProfileRepository, passwordEncoder);
    }

    @Test
    void updateProfileRejectsDisplayNameLongerThan50CharactersBeforeSaving() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(1L, "あ".repeat(51), "bio"));

        assertEquals("表示名は50文字以内で入力してください", exception.getMessage());
        verifyNoInteractions(userRepository, userProfileRepository, passwordEncoder);
    }
}
