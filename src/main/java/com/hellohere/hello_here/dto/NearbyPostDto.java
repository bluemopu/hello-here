package com.hellohere.hello_here.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NearbyPostDto {

    private Long id;
    private String content;
    private long distance;
    private LocalDateTime createdAt;

    private String displayName;
    private String avatarUrl;

    private List<String> imageUrls;

    public NearbyPostDto(Long id, String content, long distance, LocalDateTime createdAt,
            String displayName, String avatarUrl, List<String> imageUrls) {

        this.id = id;
        this.content = content;
        this.distance = distance;
        this.createdAt = createdAt;

        this.displayName = displayName;
        this.avatarUrl = avatarUrl;

        this.imageUrls = imageUrls;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public long getDistance() {
        return distance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
