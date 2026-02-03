package io.ahakim.file.controller;

import io.ahakim.file.domain.AttachFile;
import io.ahakim.file.domain.UserRepository;
import io.ahakim.file.dto.DownloadFile;
import io.ahakim.file.dto.response.FileInfoResponse;
import io.ahakim.file.service.EmailService;
import io.ahakim.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<FileInfoResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(fileService.list(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Integer id) {
        DownloadFile downloadedFile = fileService.download(id, null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, createContentDisposition(downloadedFile.getName()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(downloadedFile.getSize())
                .body(downloadedFile.getFileBytes());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam List<MultipartFile> files,
            Authentication authentication,
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String password) throws IOException {
        fileService.upload(files, authentication, folder, password);
        return ResponseEntity.ok("Files uploaded successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        fileService.delete(id);
        return ResponseEntity.ok("File deleted successfully");
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Integer id,
                                                 @RequestBody PasswordUpdateRequest request,
                                                 Authentication authentication) {
        String email = authentication.getName();
        AttachFile file = userRepository.findFileById(id);

        if (file == null || !file.getUser().getEmail().equals(email)) {
            return ResponseEntity.badRequest().body("File not found or unauthorized");
        }

        if (file.getPassword() != null && !file.getPassword().equals(request.getCurrentPassword())) {
            return ResponseEntity.badRequest().body("Incorrect current password");
        }

        file.setPassword(request.getNewPassword() != null && !request.getNewPassword().isEmpty() ?
                request.getNewPassword() : null);
        userRepository.save(file.getUser());
        return ResponseEntity.ok("Password updated successfully");
    }

    @PostMapping("/{id}/forgot-password")
    public ResponseEntity<String> forgotPassword(@PathVariable Integer id,
                                                 Authentication authentication) {
        String email = authentication.getName();
        AttachFile file = userRepository.findFileById(id);

        if (file == null || !file.getUser().getEmail().equals(email)) {
            return ResponseEntity.badRequest().body("File not found or unauthorized");
        }

        if (file.getPassword() == null) {
            return ResponseEntity.badRequest().body("This file has no password");
        }

        try {
            emailService.sendFilePasswordEmail(email, file.getName(), file.getPassword());
            return ResponseEntity.ok("Password has been sent to your registered email");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send password email");
        }
    }

    @PostMapping("/{id}/verify-password")
    public ResponseEntity<String> verifyPassword(@PathVariable Integer id,
                                                 @RequestBody VerifyPasswordRequest request,
                                                 Authentication authentication) {
        String email = authentication.getName();
        AttachFile file = userRepository.findFileById(id);

        if (file == null || !file.getUser().getEmail().equals(email)) {
            return ResponseEntity.badRequest().body("File not found or unauthorized");
        }

        if (file.getPassword() == null || file.getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok("Password verified");
        } else {
            return ResponseEntity.badRequest().body("Incorrect password");
        }
    }

    private String createContentDisposition(String name) {
        String filename = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return "attachment; filename=\"" + filename + "\"";
    }

    // Inner class for password update request body
    private static class PasswordUpdateRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    // Inner class for password verification request body
    private static class VerifyPasswordRequest {
        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}