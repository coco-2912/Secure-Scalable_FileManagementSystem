package io.ahakim.file.web;

import io.ahakim.file.service.AuthService;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password,
                           RedirectAttributes redirectAttributes, Model model) {
        try {
            authService.register(email, password);
            // Add the flash attribute to be displayed after redirect
            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! A verification email has been sent to " + email);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (MailException e) {
            model.addAttribute("error", "Failed to send verification email. Please check your network or try again later.");
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token, Model model) {
        boolean verified = authService.verify(token);
        if (verified) {
            model.addAttribute("message", "Email verified! You can now log in.");
            return "login";
        } else {
            model.addAttribute("error", "Invalid or expired verification token.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String logout,
                                Model model) {
        if (logout != null) {
            model.addAttribute("message", "Logged out successfully");
        }
        // The flash attributes are automatically added to the model by Spring
        return "login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            authService.requestPasswordReset(email);
            redirectAttributes.addFlashAttribute("message", "If the email exists, a reset link has been sent");
            return "redirect:/login";
        } catch (MailException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send reset email. Please check your network or try again later.");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        if (authService.resetPassword(token, password)) {
            redirectAttributes.addFlashAttribute("message", "Password reset successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired reset token");
        }
        return "redirect:/login";
    }
}