package com.studyapp.service;

import com.studyapp.model.User;
import com.studyapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(String username, String email, String name, String password) {
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("local");
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public User updateProfile(String username, String name, String email) {
        User user = findByUsername(username);
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void changePassword(String username, String current, String newPw) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(current, user.getPassword()))
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);
    }

    public void deleteAccount(String username) {
        userRepository.delete(findByUsername(username));
    }
}
