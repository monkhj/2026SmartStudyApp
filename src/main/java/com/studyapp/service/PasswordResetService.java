package com.studyapp.service;

import com.studyapp.model.User;
import com.studyapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.from:noreply@studyapp.com}")
    private String fromEmail;

    /**
     * 비밀번호 재설정 메일 발송
     * 이메일이 없어도 보안상 같은 메시지 반환 (계정 존재 여부 노출 방지)
     */
    public void sendResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // 1시간 유효한 토큰 생성
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            // 메일 발송
            String resetLink = baseUrl + "/auth/reset-password?token=" + token;
            sendMail(email, user.getName(), resetLink);
        });
    }

    private void sendMail(String to, String name, String resetLink) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject("[StudyApp] 비밀번호 재설정 안내");
            msg.setText(
                name + "님 안녕하세요.\n\n" +
                "아래 링크를 클릭하면 비밀번호를 재설정할 수 있습니다.\n" +
                "링크는 1시간 동안 유효합니다.\n\n" +
                resetLink + "\n\n" +
                "본인이 요청하지 않은 경우 이 메일을 무시하세요.\n\n" +
                "- StudyApp 팀"
            );
            mailSender.send(msg);
            log.info("비밀번호 재설정 메일 발송 완료: {}", to);
        } catch (Exception e) {
            log.error("메일 발송 실패: {}", e.getMessage());
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public User validateToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .filter(u -> u.getPasswordResetTokenExpiry() != null
                          && u.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    /**
     * 새 비밀번호 저장 + 토큰 무효화
     */
    public void resetPassword(String token, String newPassword) {
        User user = validateToken(token);
        if (user == null) throw new IllegalArgumentException("유효하지 않거나 만료된 링크입니다.");

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
}
