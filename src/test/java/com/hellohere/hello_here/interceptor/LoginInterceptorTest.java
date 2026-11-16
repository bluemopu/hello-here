package com.hellohere.hello_here.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginInterceptorTest {

    private final LoginInterceptor interceptor = new LoginInterceptor();

    @Test
    void allowsRequestWithLoggedInUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
        request.getSession().setAttribute("LOGIN_USER_ID", 1L);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void unauthenticatedPostApiReturnsJsonUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/posts/nearby");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"message\":\"ログインが必要です\"}", response.getContentAsString());
    }

    @Test
    void unauthenticatedPageRequestRedirectsToLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals("/login", response.getRedirectedUrl());
    }
}
