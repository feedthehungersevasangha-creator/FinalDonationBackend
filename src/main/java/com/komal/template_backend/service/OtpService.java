package com.komal.template_backend.service;

import com.komal.template_backend.Util.OtpUtil;
import com.komal.template_backend.model.OtpResponse;
import com.komal.template_backend.model.Otpentity;
import com.komal.template_backend.repo.OtpRepo;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;

@Service
public class OtpService {
    private final OtpRepo otprepo;
    private final MailService mailservice;


    public OtpService(OtpRepo otprepo, MailService mailservice) {
        this.otprepo = otprepo;
        this.mailservice = mailservice;
    }
    //    public void requestOtp(String email) throws Exception{
//        String otp=generateOtp();
//        String salt = OtpUtil.genSalt();
//        String hash = OtpUtil.hashOtp(otp, salt);
//        otprepo.deleteByEmail(email);
//        Otpentity otpdoc=new Otpentity(email,hash,salt);
//        otprepo.save(otpdoc);
//        mailservice.sendOtpMail(email, "Your OTP for Donation", "Your OTP is: " + otp);
//
//    }
//public void requestOtp(String email) throws Exception {
//    String otp = generateOtp();
//    String salt = OtpUtil.genSalt();
//    String hash = OtpUtil.hashOtp(otp, salt);
//
//    // Remove old OTPs
//    otprepo.deleteByEmail(email);
//
//    // Create new OTP with 5-min expiry
//    Otpentity otpdoc = new Otpentity(email, hash, salt);
//    otpdoc.setExpiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5 minutes from now
//
//    otprepo.save(otpdoc);
//
//    // Send OTP via email
//    mailservice.sendOtpMail(email, "Your OTP for Donation", "Your OTP is: " + otp);
//}
//    ------------------------------------------------------------------------------------
    public void requestOtp(String email) throws Exception {
        Optional<Otpentity> existingOtp = otprepo.findByEmail(email);
        Date now = new Date();

        if (existingOtp.isPresent()) {
            Otpentity otpDoc = existingOtp.get();
            if (otpDoc.isBlocked()) {
                long blockedMsLeft = otpDoc.getBlockedAt().getTime() + 15 * 60 * 1000 - now.getTime();
                throw new RuntimeException("Your account is blocked due to multiple failed attempts. Try again in " +
                        (blockedMsLeft / 1000 / 60) + " minutes.");
            }
            // Delete old OTP if not blocked
            otprepo.delete(otpDoc);
        }

        // Generate new OTP
        String otp = generateOtp();
        String salt = OtpUtil.genSalt();
        String hash = OtpUtil.hashOtp(otp, salt);

        Otpentity otpdoc = new Otpentity(email, hash, salt);
        otpdoc.setExpiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

        otprepo.save(otpdoc);
        mailservice.sendMail(email, "Your OTP for Donation", "Your OTP is: " + otp);
    }

//    --------------------------------------------------------------------------------
//    public boolean verifyOtp(String email, String inputOtp) throws Exception {
//        Optional<Otpentity> otpOpt = otprepo.findByEmail(email);
//        if (otpOpt.isEmpty()) return false;
//
//        Otpentity otpDoc = otpOpt.get();
//
//        // ✅ Check expiry
//        if (otpDoc.getExpiresAt().before(new Date())) {
//            otprepo.delete(otpDoc); // auto-delete expired OTP
//            return false;
//        }
//
//        // ✅ Check attempt
    ////        if (otpDoc.getAttempts() >= 5) return false;
//        if (otpDoc.getAttempts() >= 5) {
//            otpDoc.setBlocked(true);
//            otpDoc.setBlockedAt(new Date()); // mark the block time
//            otprepo.save(otpDoc); // save the block info
//            return false; // OTP is now blocked
//        }
//
//        // Hash comparison
//        String hash = OtpUtil.hashOtp(inputOtp, otpDoc.getSalt());
//        if (hash.equals(otpDoc.getOtpHash())) {
//            otprepo.delete(otpDoc); // success → delete immediately
//            return true;
//        } else {
//            otpDoc.setAttempts(otpDoc.getAttempts() + 1); // increment attempts
//            otprepo.save(otpDoc);
//            return false;
//        }
//    }
//---------------------------------------------------------------------
//with response
    public OtpResponse verifyOtp(String email, String inputOtp) throws Exception {
        Optional<Otpentity> otpOpt = otprepo.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return new OtpResponse(false, "OTP not found or expired", 0, 0);
        }

        Otpentity otpDoc = otpOpt.get();
        Date now = new Date();

        // ✅ Check expiry
        if (otpDoc.getExpiresAt().before(now)) {
            otprepo.delete(otpDoc);
            return new OtpResponse(false, "OTP expired", 0, 0);
        }

        // ✅ Check if blocked
        if (otpDoc.isBlocked()) {
            long blockedMsLeft = otpDoc.getBlockedAt().getTime() + 15 * 60 * 1000 - now.getTime();
            return new OtpResponse(false,
                    "Too many failed attempts. Try again in " + (blockedMsLeft / 1000 / 60) + " minutes",
                    0,
                    otpDoc.getBlockedAt().getTime() + 15 * 60 * 1000);
        }

        // Hash check
        String hash = OtpUtil.hashOtp(inputOtp, otpDoc.getSalt());
        if (hash.equals(otpDoc.getOtpHash())) {
            otprepo.delete(otpDoc);
            return new OtpResponse(true, "OTP verified successfully", 0, 0);
        } else {
            otpDoc.setAttempts(otpDoc.getAttempts() + 1);

            int attemptsLeft = Math.max(5 - otpDoc.getAttempts(), 0);

            // Block if max attempts reached
            if (otpDoc.getAttempts() >= 5) {
                otpDoc.setBlocked(true);
                otpDoc.setBlockedAt(now);
                attemptsLeft = 0;
            }

            otprepo.save(otpDoc);

            return new OtpResponse(false,
                    "Incorrect OTP. Attempts left: " + attemptsLeft,
                    attemptsLeft,
                    otpDoc.isBlocked() ? otpDoc.getBlockedAt().getTime() + 15 * 60 * 1000 : 0);
        }
    }

//-------------------------------------------------------------------

    //    public boolean verifyOtp(String email, String inputOtp) throws Exception {
//        Optional<Otpentity> otpOpt = otprepo.findByEmail(email);
//        if(otpOpt.isEmpty()) return  false;
//        Otpentity otpDoc = otpOpt.get();
//        if (otpDoc.getAttempts() >= 5) return false;
//        String hash=OtpUtil.hashOtp(inputOtp,otpDoc.getSalt());
//        if(hash.equals(otpDoc.getOtpHash())){
//            otprepo.delete(otpDoc);
//            return true;
//        }
//        else {
//            otpDoc.setAttemps((otpDoc.getAttempts()+1));
//            otprepo.save(otpDoc);
//            return false;
//        }
//    }
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000;
        return String.valueOf(num);
    }
}
