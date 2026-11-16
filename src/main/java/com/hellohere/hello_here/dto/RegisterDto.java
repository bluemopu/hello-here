package com.hellohere.hello_here.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.hellohere.hello_here.validation.CodePointSize;

public class RegisterDto {

    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String email;

    @NotBlank(message = "ユーザー名を入力してください")
    @CodePointSize(max = 50, message = "ユーザー名は50文字以内で入力してください")
    private String name;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, max = 128, message = "パスワードは8文字以上128文字以内で入力してください")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
