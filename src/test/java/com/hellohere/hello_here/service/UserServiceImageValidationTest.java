package com.hellohere.hello_here.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hellohere.hello_here.repository.UserProfileRepository;
import com.hellohere.hello_here.repository.UserRepository;

class UserServiceImageValidationTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(
            userRepository, userProfileRepository, passwordEncoder);

    @Test
    void rejectsUnreadableAvatarWithoutNullPointerException() {
        MockMultipartFile invalidImage = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", "not an image".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateAvatar(1L, invalidImage));

        assertEquals("正しい画像ファイルではありません", exception.getMessage());
        verifyNoInteractions(userRepository, userProfileRepository, passwordEncoder);
    }
}
