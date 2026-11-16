package com.hellohere.hello_here.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

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

    @Test
    void rejectsPngWhoseDeclaredContentTypeIsJpeg() throws Exception {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        MockMultipartFile disguisedImage = new MockMultipartFile(
                "avatar", "avatar.jpg", "image/jpeg", output.toByteArray());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateAvatar(1L, disguisedImage));

        assertEquals("画像ファイルの形式とContent-Typeが一致しません", exception.getMessage());
        verifyNoInteractions(userRepository, userProfileRepository, passwordEncoder);
    }

    @Test
    void rejectsAvatarWiderThan4096BeforeSaving() throws Exception {
        BufferedImage image = new BufferedImage(4097, 200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        MockMultipartFile oversizedImage = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", output.toByteArray());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateAvatar(1L, oversizedImage));

        assertEquals("画像は縦横4096px以下にしてください", exception.getMessage());
        verifyNoInteractions(userRepository, userProfileRepository, passwordEncoder);
    }
}
