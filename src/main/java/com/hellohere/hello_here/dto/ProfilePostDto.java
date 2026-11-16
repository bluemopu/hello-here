package com.hellohere.hello_here.dto;

import java.util.List;

import java.time.LocalDateTime;


public class ProfilePostDto {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private List<String> imageUrls;

    public ProfilePostDto(Long id, String content, LocalDateTime createdAt,
            List<String> imageUrls) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrls = imageUrls;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
