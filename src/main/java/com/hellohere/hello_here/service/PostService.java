package com.hellohere.hello_here.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.hellohere.hello_here.dto.NearbyPostDto;
import com.hellohere.hello_here.dto.ProfilePostDto;
import com.hellohere.hello_here.entity.Post;
import com.hellohere.hello_here.entity.PostImage;
import com.hellohere.hello_here.entity.User;
import com.hellohere.hello_here.repository.NearbyPostProjection;
import com.hellohere.hello_here.repository.PostImageRepository;
import com.hellohere.hello_here.repository.PostRepository;
import com.hellohere.hello_here.repository.UserRepository;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private static final int MAX_POST_CONTENT_LENGTH = 140;
    private static final int MAX_POST_IMAGES = 4;
    private static final long MAX_POST_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_POST_IMAGE_DIMENSION = 4096;
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostImageRepository postImageRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
            PostImageRepository postImageRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postImageRepository = postImageRepository;
    }

    public List<NearbyPostDto> getNearbyPosts(BigDecimal currentLatitude,
            BigDecimal currentLongitude) {
        validateCoordinates(currentLatitude, currentLongitude);

        List<NearbyPostProjection> results =
                postRepository.findNearbyPosts(currentLatitude, currentLongitude);
        List<NearbyPostDto> nearbyPosts = new ArrayList<>();

        for (NearbyPostProjection result : results) {
            List<String> imageUrls;

            if (result.getImageUrls() == null || result.getImageUrls().isBlank()) {
                imageUrls = new ArrayList<>();
            } else {
                imageUrls = Arrays.asList(result.getImageUrls().split("\\|\\|"));
            }
            NearbyPostDto nearbyPost = new NearbyPostDto(result.getId(), result.getContent(),
                    Math.round(result.getDistance()), result.getCreatedAt(),
                    result.getDisplayName(), result.getAvatarUrl(), imageUrls);

            nearbyPosts.add(nearbyPost);
        }

        return nearbyPosts;
    }

    public List<ProfilePostDto> getUserPosts(Long userId) {
        List<Post> posts =
                postRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);

        List<ProfilePostDto> profilePosts = new ArrayList<>();

        for (Post post : posts) {
            List<String> imageUrls = postImageRepository.findByPost_IdOrderByIdAsc(post.getId())
                    .stream().map(PostImage::getImageUrl).toList();

            ProfilePostDto dto = new ProfilePostDto(post.getId(), post.getContent(),
                    post.getCreatedAt(), imageUrls);

            profilePosts.add(dto);
        }

        return profilePosts;
    }

    @Transactional
    public void deleteUserPost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndUser_IdAndDeletedAtIsNull(postId, userId)
                .orElseThrow(() -> new IllegalArgumentException("投稿が見つかりません"));

        LocalDateTime now = LocalDateTime.now();
        post.setDeletedAt(now);
        post.setUpdatedAt(now);
        postRepository.saveAndFlush(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public Post savePostWithImages(String content, BigDecimal latitude, BigDecimal longitude,
            Long userId,
            List<MultipartFile> images) throws IOException {

        String normalizedContent = content == null ? "" : content.trim();
        List<MultipartFile> nonEmptyImages = images == null ? List.of()
                : images.stream()
                        .filter(image -> image != null && !image.isEmpty())
                        .toList();

        boolean hasContent = !normalizedContent.isEmpty();
        boolean hasImages = !nonEmptyImages.isEmpty();

        if (!hasContent && !hasImages) {
            throw new IllegalArgumentException("文字または画像を入力してください");
        }

        if (normalizedContent.codePointCount(0, normalizedContent.length())
                > MAX_POST_CONTENT_LENGTH) {
            throw new IllegalArgumentException("投稿本文は140文字以内で入力してください");
        }

        if (nonEmptyImages.size() > MAX_POST_IMAGES) {
            throw new IllegalArgumentException("画像は4枚まで投稿できます");
        }

        validateCoordinates(latitude, longitude);

        List<ValidatedPostImage> validatedImages = new ArrayList<>();

        for (MultipartFile imageFile : nonEmptyImages) {
            validatedImages.add(validatePostImage(imageFile));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        Post post = new Post();
        post.setContent(normalizedContent);
        post.setLatitude(latitude);
        post.setLongitude(longitude);
        post.setUser(user);

        Post savedPost = postRepository.save(post);

        if (validatedImages.isEmpty()) {
            return savedPost;
        }

        Path postImageDirectory = Paths.get("uploads", "posts").toAbsolutePath().normalize();

        Files.createDirectories(postImageDirectory);

        List<Path> savedFiles = new ArrayList<>();
        try {
            for (ValidatedPostImage validatedImage : validatedImages) {
                MultipartFile imageFile = validatedImage.file();
                String fileName = UUID.randomUUID() + validatedImage.extension();
                Path savePath = postImageDirectory.resolve(fileName).normalize();

                if (!savePath.startsWith(postImageDirectory)) {
                    throw new IllegalArgumentException("不正なファイルパスです");
                }

                imageFile.transferTo(savePath);

                savedFiles.add(savePath);

                PostImage postImage = new PostImage();
                postImage.setPost(savedPost);
                postImage.setImageUrl("/uploads/posts/" + fileName);
                postImage.setCreatedAt(LocalDateTime.now());

                postImageRepository.save(postImage);
            }

            registerRollbackCleanup(savedFiles);
            return savedPost;

        } catch (Exception exception) {
            deleteSavedFiles(savedFiles);

            throw exception;
        }
    }

    private ValidatedPostImage validatePostImage(MultipartFile imageFile) {
        if (imageFile.getSize() > MAX_POST_IMAGE_SIZE) {
            throw new IllegalArgumentException("画像1枚のサイズは5MB以下にしてください");
        }

        String contentType = imageFile.getContentType();
        String extension;
        String expectedFormat;

        if ("image/png".equals(contentType)) {
            extension = ".png";
            expectedFormat = "png";
        } else if ("image/jpeg".equals(contentType)) {
            extension = ".jpg";
            expectedFormat = "jpeg";
        } else {
            throw new IllegalArgumentException("PNGまたはJPEG画像を選択してください");
        }

        ImageFileValidator.ImageMetadata metadata =
                ImageFileValidator.inspectHeader(imageFile);

        if (!expectedFormat.equals(metadata.formatName())) {
            throw new IllegalArgumentException(
                    "画像ファイルの形式とContent-Typeが一致しません");
        }

        if (metadata.width() > MAX_POST_IMAGE_DIMENSION
                || metadata.height() > MAX_POST_IMAGE_DIMENSION) {
            throw new IllegalArgumentException("投稿画像は縦横4096px以下にしてください");
        }

        ImageFileValidator.verifyDecodable(imageFile);

        return new ValidatedPostImage(imageFile, extension);
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null) {
            throw new IllegalArgumentException("緯度を入力してください");
        }

        if (longitude == null) {
            throw new IllegalArgumentException("経度を入力してください");
        }

        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("緯度は-90以上90以下で入力してください");
        }

        if (longitude.compareTo(MIN_LONGITUDE) < 0
                || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("経度は-180以上180以下で入力してください");
        }
    }

    private void registerRollbackCleanup(List<Path> savedFiles) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        List<Path> filesToDelete = List.copyOf(savedFiles);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_COMMITTED) {
                            deleteSavedFiles(filesToDelete);
                        }
                    }
                });
    }

    private void deleteSavedFiles(List<Path> savedFiles) {
        for (Path savedFile : savedFiles) {
            try {
                Files.deleteIfExists(savedFile);
            } catch (IOException deleteError) {
                log.warn("保存途中の投稿画像を削除できませんでした: {}", savedFile,
                        deleteError);
            }
        }
    }

    private record ValidatedPostImage(MultipartFile file, String extension) {
    }
}
