package com.studyapp.util;

import java.util.Map;

/**
 * 소셜 공급자별 응답 구조가 다르기 때문에
 * 각 공급자에 맞게 name/email/id를 추출하는 유틸 클래스
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao"  -> new KakaoOAuth2UserInfo(attributes);
            case "naver"  -> new NaverOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 공급자: " + provider);
        };
    }

    // ── Google ─────────────────────────────────
    public static class GoogleOAuth2UserInfo extends OAuth2UserInfo {
        public GoogleOAuth2UserInfo(Map<String, Object> attributes) { super(attributes); }

        @Override public String getId()    { return (String) attributes.get("sub"); }
        @Override public String getName()  { return (String) attributes.get("name"); }
        @Override public String getEmail() { return (String) attributes.get("email"); }
        @Override public String getImageUrl() { return (String) attributes.get("picture"); }
    }

    // ── Kakao ──────────────────────────────────
    @SuppressWarnings("unchecked")
    public static class KakaoOAuth2UserInfo extends OAuth2UserInfo {
        public KakaoOAuth2UserInfo(Map<String, Object> attributes) { super(attributes); }

        @Override public String getId() { return String.valueOf(attributes.get("id")); }

        @Override public String getName() {
            Map<String, Object> account  = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile  = account != null ? (Map<String, Object>) account.get("profile") : null;
            return profile != null ? (String) profile.get("nickname") : null;
        }

        @Override public String getEmail() {
            Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
            return account != null ? (String) account.get("email") : null;
        }

        @Override public String getImageUrl() {
            Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;
            return profile != null ? (String) profile.get("profile_image_url") : null;
        }
    }

    // ── Naver ──────────────────────────────────
    @SuppressWarnings("unchecked")
    public static class NaverOAuth2UserInfo extends OAuth2UserInfo {
        public NaverOAuth2UserInfo(Map<String, Object> attributes) { super(attributes); }

        private Map<String, Object> response() {
            return (Map<String, Object>) attributes.get("response");
        }

        @Override public String getId()    { Map<String, Object> r = response(); return r != null ? (String) r.get("id") : null; }
        @Override public String getName()  { Map<String, Object> r = response(); return r != null ? (String) r.get("name") : null; }
        @Override public String getEmail() { Map<String, Object> r = response(); return r != null ? (String) r.get("email") : null; }
        @Override public String getImageUrl() { Map<String, Object> r = response(); return r != null ? (String) r.get("profile_image") : null; }
    }
}
