package com.komal.template_backend.service;

import com.komal.template_backend.model.Otpentity;
import com.komal.template_backend.repo.OtpRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class OtpCleanupScheduler {
    private final OtpRepo otpRepo;

    public OtpCleanupScheduler(OtpRepo otpRepo) {
        this.otpRepo = otpRepo;
    }

    // Run every 5 minutes
    @Scheduled(fixedRate = 300_000)
    public void cleanupOtps() {
        Date now = new Date();

        // Delete expired OTPs
        List<Otpentity> expiredOtps = otpRepo.findAllByExpiresAtBefore(now);
        if (!expiredOtps.isEmpty()) {
            otpRepo.deleteAll(expiredOtps);
        }

        // Unblock OTPs after cooldown (e.g., 15 min)
        long unblockCooldownMs = 15 * 60 * 1000; // 15 minutes
        List<Otpentity> blockedOtps = otpRepo.findAllByBlockedTrue();
        for (Otpentity otp : blockedOtps) {
            if (otp.getBlockedAt() != null &&
                    now.getTime() - otp.getBlockedAt().getTime() >= unblockCooldownMs) {
                otp.setBlocked(false);
                otp.setAttempts(0);
                otp.setBlockedAt(null);
                otpRepo.save(otp);
            }
        }
    }
}
