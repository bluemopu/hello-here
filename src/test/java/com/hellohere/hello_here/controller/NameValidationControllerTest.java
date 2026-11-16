package com.hellohere.hello_here.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hellohere.hello_here.dto.ProfileUpdateDto;
import com.hellohere.hello_here.dto.RegisterDto;
import com.hellohere.hello_here.service.PostService;
import com.hellohere.hello_here.service.UserService;

import jakarta.servlet.http.HttpSession;

class NameValidationControllerTest {

    @Test
    void registerReturnsFriendlyBadRequestMessage() {
        UserService userService = mock(UserService.class);
        AuthController controller = new AuthController(userService);
        RegisterDto dto = new RegisterDto();
        dto.setEmail("test@example.com");
        dto.setName("あ".repeat(51));
        dto.setPassword("password");
        doThrow(new IllegalArgumentException("ユーザー名は50文字以内で入力してください"))
                .when(userService).register(dto.getEmail(), dto.getName(), dto.getPassword());

        ResponseEntity<String> response = controller.register(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ユーザー名は50文字以内で入力してください", response.getBody());
    }

    @Test
    void updateProfileReturnsFriendlyBadRequestMessage() {
        UserService userService = mock(UserService.class);
        ProfileController controller = new ProfileController(userService, mock(PostService.class));
        HttpSession session = mock(HttpSession.class);
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setDisplayName("あ".repeat(51));
        dto.setBio("bio");
        when(session.getAttribute("LOGIN_USER_ID")).thenReturn(1L);
        doThrow(new IllegalArgumentException("表示名は50文字以内で入力してください"))
                .when(userService).updateProfile(1L, dto.getDisplayName(), dto.getBio());

        ResponseEntity<String> response = controller.updateProfile(dto, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("表示名は50文字以内で入力してください", response.getBody());
    }
}
