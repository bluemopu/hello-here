package com.hellohere.hello_here.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hellohere.hello_here.entity.Post;
import com.hellohere.hello_here.service.PostService;
import com.hellohere.hello_here.service.UserService;

class ApiValidationTest {

    private MockMvc mockMvc;
    private PostService postService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        userService = mock(UserService.class);
        postService = mock(PostService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(userService),
                new HomeController(postService),
                new ProfileController(userService, postService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerReturnsReasonForInvalidEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"invalid","name":"user","password":"password"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("メールアドレスの形式が正しくありません"));
    }

    @Test
    void loginRejectsInvalidEmailBeforeAuthentication() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"invalid","password":"password"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("メールアドレスの形式が正しくありません"));

        verifyNoInteractions(userService);
    }

    @Test
    void registerAccepts50SupplementaryCharactersInName() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","name":"%s","password":"password"}
                                """.formatted("😀".repeat(50))))
                .andExpect(status().isOk());
    }

    @Test
    void profileAcceptsSupplementaryCharactersAtLimits() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"%s","bio":"%s"}
                                """.formatted("😀".repeat(50), "😀".repeat(160)))
                        .sessionAttr("LOGIN_USER_ID", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void multipartPostRejectsContentLongerThan140Characters() throws Exception {
        mockMvc.perform(multipart("/posts")
                        .param("content", "あ".repeat(141))
                        .param("latitude", "35.0")
                        .param("longitude", "139.0")
                        .sessionAttr("LOGIN_USER_ID", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("投稿本文は140文字以内で入力してください"));
    }

    @Test
    void multipartPostAccepts140SupplementaryCharacters() throws Exception {
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(1L);
        when(postService.savePostWithImages(
                "😀".repeat(140),
                new java.math.BigDecimal("35.0"),
                new java.math.BigDecimal("139.0"),
                1L,
                null)).thenReturn(post);

        mockMvc.perform(multipart("/posts")
                        .param("content", "😀".repeat(140))
                        .param("latitude", "35.0")
                        .param("longitude", "139.0")
                        .sessionAttr("LOGIN_USER_ID", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void nearbyPostsRejectsMissingLatitude() throws Exception {
        mockMvc.perform(post("/posts/nearby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longitude\":139.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("緯度を入力してください"));
    }
}
