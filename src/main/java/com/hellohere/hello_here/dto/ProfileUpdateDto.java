package com.hellohere.hello_here.dto;

import jakarta.validation.constraints.NotBlank;

import com.hellohere.hello_here.validation.CodePointSize;

public class ProfileUpdateDto {

    @NotBlank(message = "表示名を入力してください")
    @CodePointSize(max = 50, message = "表示名は50文字以内で入力してください")
    private String displayName;

    @CodePointSize(max = 160, message = "自己紹介は160文字以内で入力してください")
    private String bio;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
