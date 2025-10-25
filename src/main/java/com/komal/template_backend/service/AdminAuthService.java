
package com.komal.template_backend.service;

import com.komal.template_backend.model.Admin;
import com.komal.template_backend.repo.AdminRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminAuthService {

    @Value("${Front_end}")  // Read from .env or environment
    private String Front_end;

    private final AdminRepo adminRepo;
    private final BCryptPasswordEncoder encoder;
    private final MailService emailService;

    public AdminAuthService(AdminRepo adminRepo, MailService emailService) {
        this.adminRepo = adminRepo;
        this.emailService = emailService;
        this.encoder = new BCryptPasswordEncoder();
    }

    // ----------------- Registration -----------------
    public boolean register(String email, String password) {
        if (adminRepo.findByEmail(email).isPresent()) return false;
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPasswordHash(encoder.encode(password));
        adminRepo.save(admin);
        return true;
    }

    // ----------------- Login -----------------
    public boolean login(String email, String password) {
        Optional<Admin> admin = adminRepo.findByEmail(email);
        return admin.isPresent() && encoder.matches(password, admin.get().getPasswordHash());
    }

    // ----------------- Initiate Forgot Password -----------------
    public boolean initiateForgotPassword(String email) throws UnsupportedEncodingException {
        Optional<Admin> adminOpt = adminRepo.findByEmail(email);
        if (adminOpt.isEmpty()) return false;

        Admin admin = adminOpt.get();

        // Generate token and expiry
        String token = UUID.randomUUID().toString();
        admin.setResetToken(token);
        admin.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        adminRepo.save(admin);

        // Encode token for URL
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8.toString());
        String resetLink = Front_end + "/reset-password?token=" + encodedToken;

        // Send HTML email
        String htmlContent = "<p>Click the link below to reset your password (valid for 1 hour):</p>" +
                "<a href='" + resetLink + "'>Reset Password</a>";
        emailService.sendMail(email, "Reset your password", htmlContent);

        return true;
    }

    // ----------------- Reset Password -----------------
    public boolean resetPassword(String token, String newPassword) {
        Optional<Admin> adminOpt = adminRepo.findByResetToken(token);
        if (adminOpt.isEmpty()) return false;

        Admin admin = adminOpt.get();

        // Check if token is expired
        if (admin.getResetTokenExpiry() == null || admin.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false; // token expired
        }

        // Update password and clear token
        admin.setPasswordHash(encoder.encode(newPassword));
        admin.setResetToken(null);
        admin.setResetTokenExpiry(null); // clear expiry after use
        adminRepo.save(admin);

        return true;
    }
}
