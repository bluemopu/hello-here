package com.hellohere.hello_here.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hellohere.hello_here.dto.ProfileDto;
import com.hellohere.hello_here.dto.ProfileUpdateDto;
import com.hellohere.hello_here.dto.ProfilePostDto;
import com.hellohere.hello_here.service.PostService;
import com.hellohere.hello_here.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Controller
public class ProfileController {

    private final UserService userService;
    private final PostService postService;

    public ProfileController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        ProfileDto profile = userService.getProfile(userId);
        List<ProfilePostDto> posts = postService.getUserPosts(userId);

        model.addAttribute("profile", profile);
        model.addAttribute("posts", posts);

        return "profile";
    }

    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<String> updateProfile(@Valid @RequestBody ProfileUpdateDto dto,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        try {
            userService.updateProfile(userId, dto.getDisplayName(), dto.getBio());
            return ResponseEntity.ok("更新しました");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/profile/avatar")
    @ResponseBody
    public String updateAvatar(
            @RequestParam(value = "avatar", required = false)
            @NotNull(message = "画像を選択してください") MultipartFile avatar,
            HttpSession session)
            throws IOException {

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        return userService.updateAvatar(userId, avatar);
    }
}
