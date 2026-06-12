package com.studyapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studyapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final UserService userService;

    @GetMapping
    public String page(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("user", userService.findByUsername(ud.getUsername()));
        return "auth/account";
    }

    @PostMapping("/update")
    public String update(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam String name, @RequestParam String email, RedirectAttributes ra) {
        try { userService.updateProfile(ud.getUsername(), name, email); ra.addFlashAttribute("message", "프로필이 업데이트되었습니다."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/account";
    }

    @PostMapping("/password")
    public String password(@AuthenticationPrincipal UserDetails ud,
                           @RequestParam String currentPassword, @RequestParam String newPassword,
                           @RequestParam String confirmPassword, RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) { ra.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다."); return "redirect:/account"; }
        try { userService.changePassword(ud.getUsername(), currentPassword, newPassword); ra.addFlashAttribute("message", "비밀번호가 변경되었습니다."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/account";
    }

    @PostMapping("/delete")
    public String delete(@AuthenticationPrincipal UserDetails ud) {
        userService.deleteAccount(ud.getUsername());
        return "redirect:/auth/login?deleted=true";
    }
}
