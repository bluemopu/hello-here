package com.hellohere.hello_here.interceptor;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws IOException {
        HttpSession session = request.getSession(false);

        Long userId = null;

        if (session != null) {
            userId = (Long) session.getAttribute("LOGIN_USER_ID");
        }

        if (userId != null) {
            return true;
        }

        String uri = request.getRequestURI();

        if (uri.startsWith("/posts")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"ログインが必要です\"}");

            return false;
        }

        response.sendRedirect("/login");

        return false;
    }
}
