package com.komal.template_backend.Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class OtpUtil {
    public static String genSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    public static String hashOtp(String otp, String saltBase64) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        md.update(otp.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(md.digest());
    }
}
