package com.hellohere.hello_here.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.hellohere.hello_here.dto.LocationDto;
import com.hellohere.hello_here.dto.NearbyPostDto;
import com.hellohere.hello_here.dto.PostCreateResponseDto;
import com.hellohere.hello_here.entity.Post;
import com.hellohere.hello_here.service.PostService;
import com.hellohere.hello_here.validation.CodePointSize;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Controller
public class homeController {

    private static final Logger log = LoggerFactory.getLogger(homeController.class);

    private final PostService postService;

    public homeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/home")
    public String home(HttpSession session) {

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if (userId == null) {
            return "redirect:/login";
        }

        return "home";
    }

    @PostMapping(value = "/posts", consumes = "multipart/form-data")
    @ResponseBody
    public PostCreateResponseDto createPostWithImages(
            @RequestParam(value = "content", required = false)
            @CodePointSize(max = 140, message = "投稿本文は140文字以内で入力してください") String content,
            @RequestParam(value = "latitude", required = false)
            @NotNull(message = "緯度を入力してください")
            @DecimalMin(value = "-90.0", message = "緯度は-90以上で入力してください")
            @DecimalMax(value = "90.0", message = "緯度は90以下で入力してください")
            BigDecimal latitude,
            @RequestParam(value = "longitude", required = false)
            @NotNull(message = "経度を入力してください")
            @DecimalMin(value = "-180.0", message = "経度は-180以上で入力してください")
            @DecimalMax(value = "180.0", message = "経度は180以下で入力してください")
            BigDecimal longitude,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) throws IOException {

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if (userId == null) {
            throw new IllegalStateException("ログインしてください");
        }

        log.debug("投稿画像枚数: {}", images == null ? 0 : images.size());

        Post post = postService.savePostWithImages(content, latitude, longitude, userId, images);

        return new PostCreateResponseDto(post.getId());
    }


    @PostMapping("/posts/nearby")
    @ResponseBody
    public List<NearbyPostDto> getNearbyPosts(@Valid @RequestBody LocationDto location) {

        return postService.getNearbyPosts(location.getLatitude(), location.getLongitude());
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, HttpSession session) {

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        try {
            postService.deleteUserPost(postId, userId);
            return ResponseEntity.ok("投稿を削除しました");
        } catch (IllegalArgumentException e) {

            return ResponseEntity.notFound().build();
        }
    }
}
