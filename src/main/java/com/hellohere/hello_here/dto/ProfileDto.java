package com.hellohere.hello_here.dto;

public class ProfileDto {

    private String email;
    private String name;
    private String displayName;
    private String bio;
    private String avatarUrl;

    public ProfileDto(String email, String name, String displayName, String bio, String avatarUrl) {
        this.email = email;
        this.name = name;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
