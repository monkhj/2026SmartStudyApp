package com.studyapp.controller;

import com.studyapp.model.NotificationLog;
import com.studyapp.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    @GetMapping
    public String notificationList(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        // 현재 로그인한 사용자의 전체 알림 내역 조회 (필요 시 정렬 조건 추가 가능)
        List<NotificationLog> logs = notificationLogRepository.findAll(); // 실제로는 userId로 필터링해야 합니다.
        model.addAttribute("notifications", logs);
        
        return "notification/index";
    }
    
    @GetMapping("/settings")
    public String notificationSettings() {
        return "notification/settings";
    }
}