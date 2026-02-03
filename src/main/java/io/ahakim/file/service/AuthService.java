package io.ahakim.file.service;

import io.ahakim.file.domain.User;
import io.ahakim.file.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException; // For Spring's mail exceptions
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException; // For JavaMail exceptions
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void register(String email, String password) throws MailException {
        logger.debug("Starting registration for email: {}", email);
        if (userRepository.findByEmail(email) != null) {
            logger.warn("Email already registered: {}", email);
            throw new IllegalArgumentException("Email is already registered");
        }
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Password is empty or null for email: {}", email);
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.contains(" ")) {
            logger.warn("Password contains spaces for email: {}", email);
            throw new IllegalArgumentException("Password cannot contain spaces");
        }
        if (password.length() < 8) {
            logger.warn("Password too short for email: {}", email);
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerified(false);
        user.setTokenExpiration(LocalDateTime.now().plusHours(24));
        logger.debug("Saving user: {}", user.getEmail());
        userRepository.save(user);
        logger.debug("User saved, sending verification email");
        try {
            emailService.sendVerificationEmail(email, token);
            logger.debug("Verification email sent for: {}", email);
        } catch (MessagingException e) { // Catch MessagingException from EmailService
            logger.error("Registration failed due to email error for {}: {}", email, e.getMessage());
            throw new MailException("Failed to send verification email. Check your network or try again later.") {
                @Override
                public synchronized Throwable fillInStackTrace() {
                    return this; // Avoid stack trace overhead
                }
            };
        }
    }

    public boolean verify(String token) {
        logger.debug("Verifying token: {}", token);
        User user = userRepository.findByVerificationToken(token);
        if (user == null || user.isVerified() || user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            logger.warn("Verification failed for token: {}", token);
            return false;
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);
        logger.debug("User verified: {}", user.getEmail());
        return true;
    }

    public void requestPasswordReset(String email) throws MailException {
        logger.debug("Requesting password reset for: {}", email);
        User user = userRepository.findByEmail(email);
        if (user != null && user.isVerified()) {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiration(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            try {
                emailService.sendResetPasswordEmail(email, token); // Line 91
                logger.debug("Password reset email sent for: {}", email);
            } catch (MessagingException e) { // Catch MessagingException and rethrow as MailException
                logger.error("Password reset email failed for {}: {}", email, e.getMessage());
                throw new MailException("Failed to send reset email. Check your network or try again later.") {
                    @Override
                    public synchronized Throwable fillInStackTrace() {
                        return this;
                    }
                };
            }
        } else {
            logger.warn("No verified user found for email: {}", email);
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        logger.debug("Resetting password with token: {}", token);
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            logger.warn("Password reset failed for token: {}", token);
            return false;
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            logger.warn("New password is empty or null for reset token: {}", token);
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (newPassword.contains(" ")) {
            logger.warn("New password contains spaces for reset token: {}", token);
            throw new IllegalArgumentException("Password cannot contain spaces");
        }
        if (newPassword.length() < 8) {
            logger.warn("New password too short for reset token: {}", token);
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiration(null);
        userRepository.save(user);
        logger.debug("Password reset successful for: {}", user.getEmail());
        return true;
    }
}