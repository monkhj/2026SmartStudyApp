package com.studyapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 통합 User 도메인
 * - LYS: JWT 인증 필드 (refreshToken, loginFailCount, accountLocked, roles)
 * - PHJ: 소셜 로그인 필드 (provider, providerId, profileImage, passwordResetToken)
 * - PHM: 소셜 랭킹용 필드 (major)
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String username;

    @Indexed(unique = true, sparse = true)
    private String email;

    private String password;       // BCrypt 해시 (소셜 로그인은 null)
    private String name;
    private String major;          // 전공 (소셜 랭킹 필터용) — PHM

    // ── JWT 인증 필드 (LYS) ─────────────────────────
    private String refreshToken;
    private int    loginFailCount = 0;
    private boolean accountLocked = false;
    private boolean emailVerified = false;
    private List<String> roles = new ArrayList<>(List.of("ROLE_USER"));

    // ── 소셜 로그인 필드 (PHJ) ──────────────────────
    private String provider;       // "local", "google", "kakao", "naver"
    private String providerId;
    private String profileImage;

    // ── 비밀번호 찾기 (PHJ) ─────────────────────────
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public User() {}

    private User(Builder b) {
        this.username      = b.username;
        this.password      = b.password;
        this.email         = b.email;
        this.name          = b.name;
        this.major         = b.major;
        this.roles         = b.roles;
        this.provider      = b.provider;
        this.providerId    = b.providerId;
        this.profileImage  = b.profileImage;
        this.createdAt     = b.createdAt;
        this.updatedAt     = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username, password, email, name, major;
        private String provider, providerId, profileImage;
        private List<String> roles = new ArrayList<>(List.of("ROLE_USER"));
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder username(String v)     { this.username = v;     return this; }
        public Builder password(String v)     { this.password = v;     return this; }
        public Builder email(String v)        { this.email = v;        return this; }
        public Builder name(String v)         { this.name = v;         return this; }
        public Builder major(String v)        { this.major = v;        return this; }
        public Builder roles(List<String> v)  { this.roles = v;        return this; }
        public Builder provider(String v)     { this.provider = v;     return this; }
        public Builder providerId(String v)   { this.providerId = v;   return this; }
        public Builder profileImage(String v) { this.profileImage = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public User build() { return new User(this); }
    }

    // ── Getters ──────────────────────────────────────
    public String getId()                       { return id; }
    public String getUsername()                 { return username; }
    public String getPassword()                 { return password; }
    public String getEmail()                    { return email; }
    public String getName()                     { return name; }
    public String getMajor()                    { return major; }
    public String getRefreshToken()             { return refreshToken; }
    public int    getLoginFailCount()           { return loginFailCount; }
    public boolean isAccountLocked()            { return accountLocked; }
    public boolean isEmailVerified()            { return emailVerified; }
    public List<String> getRoles()              { return roles; }
    public String getProvider()                 { return provider; }
    public String getProviderId()               { return providerId; }
    public String getProfileImage()             { return profileImage; }
    public String getPasswordResetToken()       { return passwordResetToken; }
    public LocalDateTime getPasswordResetTokenExpiry() { return passwordResetTokenExpiry; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }

    // ── Setters ──────────────────────────────────────
    public void setId(String v)                        { this.id = v; }
    public void setUsername(String v)                  { this.username = v; }
    public void setPassword(String v)                  { this.password = v; }
    public void setEmail(String v)                     { this.email = v; }
    public void setName(String v)                      { this.name = v; }
    public void setMajor(String v)                     { this.major = v; }
    public void setRefreshToken(String v)              { this.refreshToken = v; }
    public void setLoginFailCount(int v)               { this.loginFailCount = v; }
    public void setAccountLocked(boolean v)            { this.accountLocked = v; }
    public void setEmailVerified(boolean v)            { this.emailVerified = v; }
    public void setRoles(List<String> v)               { this.roles = v; }
    public void setProvider(String v)                  { this.provider = v; }
    public void setProviderId(String v)                { this.providerId = v; }
    public void setProfileImage(String v)              { this.profileImage = v; }
    public void setPasswordResetToken(String v)        { this.passwordResetToken = v; }
    public void setPasswordResetTokenExpiry(LocalDateTime v) { this.passwordResetTokenExpiry = v; }
    public void setCreatedAt(LocalDateTime v)          { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v)          { this.updatedAt = v; }
}
