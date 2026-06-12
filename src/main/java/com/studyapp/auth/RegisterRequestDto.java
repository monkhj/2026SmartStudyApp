package com.studyapp.auth;

import jakarta.validation.constraints.*;

public class RegisterRequestDto {

    @NotBlank
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private String username;

    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;

    @NotBlank
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
    private String confirmPassword;

    private String major; // 선택 입력

    public RegisterRequestDto() {}

    public String getUsername()        { return username; }
    public String getName()            { return name; }
    public String getEmail()           { return email; }
    public String getPassword()        { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    public String getMajor()           { return major; }

    public void setUsername(String v)        { this.username = v; }
    public void setName(String v)            { this.name = v; }
    public void setEmail(String v)           { this.email = v; }
    public void setPassword(String v)        { this.password = v; }
    public void setConfirmPassword(String v) { this.confirmPassword = v; }
    public void setMajor(String v)           { this.major = v; }
}
