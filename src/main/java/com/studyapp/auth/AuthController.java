package com.studyapp.auth;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error  != null) model.addAttribute("errorMsg",   "아이디 또는 비밀번호가 올바르지 않습니다.");
        if (logout != null) model.addAttribute("successMsg", "로그아웃 되었습니다.");
        model.addAttribute("loginRequest", new LoginRequestDto());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequestDto dto,
                        BindingResult br, HttpServletResponse res, Model model) {
        if (br.hasErrors()) return "auth/login";
        try {
            AuthService.TokenResponse tokens = authService.login(dto);
            Cookie ac = new Cookie("accessToken",  tokens.accessToken());
            ac.setHttpOnly(true); ac.setPath("/"); ac.setMaxAge(3600);
            res.addCookie(ac);
            Cookie rc = new Cookie("refreshToken", tokens.refreshToken());
            rc.setHttpOnly(true); rc.setPath("/auth/refresh"); rc.setMaxAge(1209600);
            res.addCookie(rc);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequestDto dto,
                           BindingResult br, RedirectAttributes ra, Model model) {
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            br.rejectValue("confirmPassword", "mismatch", "비밀번호가 일치하지 않습니다.");
        if (br.hasErrors()) return "auth/register";
        try {
            authService.register(dto);
            ra.addFlashAttribute("successMsg", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/refresh")
    @ResponseBody
    public ResponseEntity<?> refresh(
            @CookieValue(value = "refreshToken", required = false) String rt,
            HttpServletResponse res) {
        if (rt == null) return ResponseEntity.status(401).body(Map.of("error", "Refresh Token 없음"));
        try {
            String newAccess = authService.refreshAccessToken(rt);
            Cookie c = new Cookie("accessToken", newAccess);
            c.setHttpOnly(true); c.setPath("/"); c.setMaxAge(3600);
            res.addCookie(c);
            return ResponseEntity.ok(Map.of("message", "토큰 갱신 완료"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse res) {
        Cookie ac = new Cookie("accessToken",  null); ac.setMaxAge(0); ac.setPath("/"); res.addCookie(ac);
        Cookie rc = new Cookie("refreshToken", null); rc.setMaxAge(0); rc.setPath("/auth/refresh"); res.addCookie(rc);
        return "redirect:/auth/login?logout";
    }
}
