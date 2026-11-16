package com.hellohere.hello_here.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.hellohere.hello_here.repository.PostImageRepository;
import com.hellohere.hello_here.repository.PostRepository;
import com.hellohere.hello_here.repository.UserRepository;

class PostServiceValidationTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PostImageRepository postImageRepository = mock(PostImageRepository.class);
    private final PostService postService = new PostService(
            postRepository, userRepository, postImageRepository);

    @Test
    void rejectsBlankContentWhenOnlyImagePartIsEmpty() {
        MockMultipartFile emptyImage = new MockMultipartFile(
                "images", "empty.png", "image/png", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.savePostWithImages(" ", BigDecimal.ZERO, BigDecimal.ZERO,
                        1L, List.of(emptyImage)));

        assertEquals("文字または画像を入力してください", exception.getMessage());
        verifyNoInteractions(postRepository, userRepository, postImageRepository);
    }

    @Test
    void rejectsContentLongerThan140Characters() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.savePostWithImages("あ".repeat(141), BigDecimal.ZERO,
                        BigDecimal.ZERO, 1L, List.of()));

        assertEquals("投稿本文は140文字以内で入力してください", exception.getMessage());
        verifyNoInteractions(postRepository, userRepository, postImageRepository);
    }

    @Test
    void rejectsPostImageWiderThan4096BeforeSaving() throws Exception {
        MockMultipartFile image = pngImage(4097, 1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.savePostWithImages("content", BigDecimal.ZERO,
                        BigDecimal.ZERO, 1L, List.of(image)));

        assertEquals("投稿画像は縦横4096px以下にしてください", exception.getMessage());
        verifyNoInteractions(postRepository, userRepository, postImageRepository);
    }

    @Test
    void rejectsGifDisguisedAsPng() throws Exception {
        MockMultipartFile image = imageFile("gif", "image/png");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.savePostWithImages("content", BigDecimal.ZERO,
                        BigDecimal.ZERO, 1L, List.of(image)));

        assertEquals("画像ファイルの形式とContent-Typeが一致しません", exception.getMessage());
        verifyNoInteractions(postRepository, userRepository, postImageRepository);
    }

    @Test
    void rejectsJpegWhoseDeclaredContentTypeIsPng() throws Exception {
        MockMultipartFile image = imageFile("jpeg", "image/png");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.savePostWithImages("content", BigDecimal.ZERO,
                        BigDecimal.ZERO, 1L, List.of(image)));

        assertEquals("画像ファイルの形式とContent-Typeが一致しません", exception.getMessage());
        verifyNoInteractions(postRepository, userRepository, postImageRepository);
    }

    @Test
    void rejectsOutOfRangeCoordinatesBeforeSaving() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.savePostWithImages("content", new BigDecimal("90.1"),
                        BigDecimal.ZERO, 1L, List.of()));

        assertEquals("緯度は-90以上90以下で入力してください", exception.getMessage());
        verifyNoInteractions(postRepository, userRepository, postImageRepository);
    }

    private MockMultipartFile pngImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile(
                "images", "image.png", "image/png", output.toByteArray());
    }

    private MockMultipartFile imageFile(String format, String contentType) throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return new MockMultipartFile(
                "images", "image." + format, contentType, output.toByteArray());
    }
}
