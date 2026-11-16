package com.hellohere.hello_here.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.hellohere.hello_here.dto.LoginDto;
import com.hellohere.hello_here.entity.User;
import com.hellohere.hello_here.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

class AuthControllerTest {

    @Test
    void successfulLoginRotatesSessionIdBeforeStoringUser() {
        UserService userService = mock(UserService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        User user = mock(User.class);
        LoginDto dto = new LoginDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        when(userService.login("test@example.com", "password")).thenReturn(user);
        when(user.getId()).thenReturn(1L);

        String response = new AuthController(userService).login(dto, request, session);

        assertEquals("ログインしました", response);
        InOrder order = inOrder(userService, request, session);
        order.verify(userService).login("test@example.com", "password");
        order.verify(request).changeSessionId();
        order.verify(session).setAttribute("LOGIN_USER_ID", 1L);
    }
}
