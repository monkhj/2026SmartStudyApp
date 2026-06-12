package com.studyapp.auth;

import com.studyapp.model.User;
import com.studyapp.repository.UserRepository;
import com.studyapp.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class.getName());
    private static final int MAX_FAIL = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public void register(RegisterRequestDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        if (userRepository.existsByUsername(dto.getUsername()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");

        User user = User.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .email(dto.getEmail())
                .major(dto.getMajor())
                .password(passwordEncoder.encode(dto.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        log.info("[Auth] 회원가입 완료: " + dto.getUsername());
    }

    public TokenResponse login(LoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (user.isAccountLocked())
            throw new IllegalStateException("로그인 5회 실패로 계정이 잠겼습니다.");

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int fails = user.getLoginFailCount() + 1;
            user.setLoginFailCount(fails);
            if (fails >= MAX_FAIL) user.setAccountLocked(true);
            userRepository.save(user);
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다. (" + fails + "/" + MAX_FAIL + ")");
        }

        user.setLoginFailCount(0);
        String access  = jwtProvider.generateAccessToken(user.getUsername(), user.getRoles());
        String refresh = jwtProvider.generateRefreshToken(user.getUsername());
        user.setRefreshToken(refresh);
        userRepository.save(user);
        return new TokenResponse(access, refresh);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!jwtProvider.validate(refreshToken))
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        String username = jwtProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!refreshToken.equals(user.getRefreshToken()))
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        return jwtProvider.generateAccessToken(username, user.getRoles());
    }

    public record TokenResponse(String accessToken, String refreshToken) {}
}
