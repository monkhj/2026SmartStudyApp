package com.studyapp.auth;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {

    @NotBlank(message = "아이디를 입력해 주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;

    public LoginRequestDto() {}

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public void setUsername(String v) { this.username = v; }
    public void setPassword(String v) { this.password = v; }
}
