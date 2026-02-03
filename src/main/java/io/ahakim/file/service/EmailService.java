package io.ahakim.file.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        // Existing method unchanged
        logger.debug("Preparing to send verification email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Verify Your Email");
            String link = "http://localhost:5000/verify?token=" + token;
            helper.setText("Click to verify: <a href='" + link + "'>Verify Email</a>", true);
            logger.debug("Sending verification email to: {}", to);
            mailSender.send(message);
            logger.debug("Verification email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void sendResetPasswordEmail(String to, String token) throws MessagingException {
        // Existing method unchanged
        logger.debug("Preparing to send reset password email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");
            String link = "http://localhost:5000/reset-password?token=" + token;
            helper.setText("Click to reset password: <a href='" + link + "'>Reset Password</a>", true);
            logger.debug("Sending reset password email to: {}", to);
            mailSender.send(message);
            logger.debug("Reset password email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send reset password email to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void sendFilePasswordEmail(String to, String fileName, String password) throws MessagingException {
        logger.debug("Preparing to send file password email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Your File Password");
            helper.setText("The password for your file '" + fileName + "' is: <strong>" + password + "</strong>", true);
            logger.debug("Sending file password email to: {}", to);
            mailSender.send(message);
            logger.debug("File password email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send file password email to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }
}