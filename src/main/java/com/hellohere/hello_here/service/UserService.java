package com.hellohere.hello_here.service;

import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.hellohere.hello_here.entity.User;
import com.hellohere.hello_here.entity.UserProfile;
import com.hellohere.hello_here.repository.UserProfileRepository;
import com.hellohere.hello_here.repository.UserRepository;
import com.hellohere.hello_here.dto.ProfileDto;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_BIO_LENGTH = 160;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final long MAX_AVATAR_FILE_SIZE = 5L * 1024 * 1024;

    private static final int MIN_AVATAR_SIZE = 200;

    private static final int MAX_AVATAR_SIZE = 8000;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public User register(String email, String name, String password) {
        validateName(name, "ユーザー名");

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }

        User user = new User();

        user.setEmail(email);
        user.setName(name);

        user.setPasswordHash(passwordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();

        profile.setUserId(savedUser.getId());
        profile.setDisplayName(name);

        LocalDateTime now = LocalDateTime.now();

        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);

        userProfileRepository.save(profile);


        return savedUser;

    }

    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが違います"));

        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが違います");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが違います");
        }


        return user;

    }

    public ProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("プロフィールが見つかりません"));

        return new ProfileDto(user.getEmail(), user.getName(), profile.getDisplayName(),
                profile.getBio(), profile.getAvatarUrl());
    }

    @Transactional
    public void updateProfile(Long userId, String displayName, String bio) {
        validateName(displayName, "表示名");

        if (bio != null && bio.codePointCount(0, bio.length()) > MAX_BIO_LENGTH) {
            throw new IllegalArgumentException("自己紹介は160文字以内で入力してください");
        }

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("プロフィールが見つかりません"));

        profile.setDisplayName(displayName);
        profile.setBio(bio);

        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);
    }

    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("画像を選択してください");
        }

        String contentsType = file.getContentType();
        String extension;

        if ("image/png".equals(contentsType)) {
            extension = ".png";
        } else if ("image/jpeg".equals((contentsType))) {
            extension = ".jpg";
        } else if ("image/gif".equals((contentsType))) {
            extension = ".gif";
        } else {
            throw new IllegalArgumentException("PNG、JPEGまたはGIF画像を選択してください");
        }

        if (file.getSize() > MAX_AVATAR_FILE_SIZE) {

            throw new IllegalArgumentException("画像サイズは5MB以下にしてください");
        }

        ImageFileValidator.ImageDimensions dimensions = ImageFileValidator.inspectHeader(file);

        int imageWidth = dimensions.width();

        int imageHeight = dimensions.height();


        if (imageWidth < MIN_AVATAR_SIZE || imageHeight < MIN_AVATAR_SIZE) {

            throw new IllegalArgumentException("画像は縦横200px以上にしてください");
        }


        if (imageWidth > MAX_AVATAR_SIZE || imageHeight > MAX_AVATAR_SIZE) {

            throw new IllegalArgumentException("画像は縦横8000px以下にしてください");
        }

        ImageFileValidator.verifyDecodable(file);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("プロフィールが見つかりません"));

        String oldAvatarUrl = profile.getAvatarUrl();
        Path avatarDirectory = Paths.get("uploads", "avatars").toAbsolutePath().normalize();
        Files.createDirectories(avatarDirectory);
        String fileName = UUID.randomUUID() + extension;
        Path savePath = avatarDirectory.resolve(fileName).normalize();
        file.transferTo(savePath);
        String avatarUrl = "/uploads/avatars/" + fileName;

        try {
            profile.setAvatarUrl(avatarUrl);
            profile.setUpdatedAt(LocalDateTime.now());
            userProfileRepository.saveAndFlush(profile);
        } catch (Exception e) {
            Files.deleteIfExists(savePath);
            throw e;
        }

        if (oldAvatarUrl != null && oldAvatarUrl.startsWith("/uploads/avatars/")) {
            String oldFileName = oldAvatarUrl.substring("/uploads/avatars/".length());
            Path oldFilePath = avatarDirectory.resolve(oldFileName).normalize();

            if (oldFilePath.startsWith(avatarDirectory)) {
                TransactionSynchronizationManager
                        .registerSynchronization(new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                try {
                                    Files.deleteIfExists(oldFilePath);
                                } catch (IOException e) {
                                    log.warn("古いプロフィール画像を削除できません: {}", oldFilePath, e);
                                }
                            }
                        });
            }

        }


        return avatarUrl;
    }

    private void validateName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "を入力してください");
        }

        if (value.codePointCount(0, value.length()) > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(fieldName + "は50文字以内で入力してください");
        }
    }
}
