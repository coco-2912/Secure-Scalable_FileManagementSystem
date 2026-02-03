package io.ahakim.file.controller;

import io.ahakim.file.domain.User;
import io.ahakim.file.domain.UserRepository;
import io.ahakim.file.dto.response.FileInfoResponse;
import io.ahakim.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FileViewController {

    private final FileStorageService fileService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String showFileUploadPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("email", user.getEmail());
        List<FileInfoResponse> files = fileService.list(authentication);
        model.addAttribute("files", files);
        return "index";
    }

    @PostMapping("/update-profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String newEmail,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        String currentEmail = authentication.getName();
        User user = userRepository.findByEmail(currentEmail);

        if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(currentEmail)) {
            if (userRepository.findByEmail(newEmail) != null) {
                redirectAttributes.addFlashAttribute("error", "New email is already registered");
                return "redirect:/";
            }
            user.setEmail(newEmail);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.contains(" ")) {
                redirectAttributes.addFlashAttribute("error", "Password cannot contain spaces");
                return "redirect:/";
            }
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long");
                return "redirect:/";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        return "redirect:/";
    }
}