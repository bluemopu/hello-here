package com.hellohere.hello_here.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hellohere.hello_here.dto.LoginDto;
import com.hellohere.hello_here.dto.RegisterDto;
import com.hellohere.hello_here.entity.User;
import com.hellohere.hello_here.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto dto) {
        try {
            userService.register(dto.getEmail(), dto.getName(), dto.getPassword());
            return ResponseEntity.ok("登録しました");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginDto dto, HttpServletRequest request,
            HttpSession session) {

        User user = userService.login(dto.getEmail(), dto.getPassword());

        request.changeSessionId();
        session.setAttribute("LOGIN_USER_ID", user.getId());

        return "ログインしました";
    }

    @PostMapping("/logout")
    @ResponseBody
    public String logout(HttpSession session) {

        session.invalidate();

        return "ログアウトしました";
    }
}
