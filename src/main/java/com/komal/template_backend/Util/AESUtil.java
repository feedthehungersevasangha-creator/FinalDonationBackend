////package com.example.DonourSecu;
////
////import javax.crypto.Cipher;
////import javax.crypto.spec.SecretKeySpec;
////import java.util.Base64;
////
////public class AESUtil {
////    private static final String ALGORITHM = "AES";
////
////    private static SecretKeySpec getKeySpec() {
////        String key = System.getenv("AESKey");
////        if (key == null || key.isEmpty()) {
////            throw new IllegalStateException("AES_SECRET_KEY not found in environment!");
////        }
////        else {
////            System.out.println("✅ AES Key loaded successfully.");
////        }
////        byte[] decodedKey = Base64.getDecoder().decode(key);
////        return new SecretKeySpec(decodedKey, ALGORITHM);
////    }
////
////    public static String encrypt(String data) throws Exception {
////        Cipher cipher = Cipher.getInstance("AES");
////        cipher.init(Cipher.ENCRYPT_MODE, getKeySpec());
////        byte[] encrypted = cipher.doFinal(data.getBytes());
////        return Base64.getEncoder().encodeToString(encrypted);
////    }
////
////    public static String decrypt(String encryptedData) throws Exception {
////        Cipher cipher = Cipher.getInstance("AES");
////        cipher.init(Cipher.DECRYPT_MODE, getKeySpec());
////        byte[] decoded = Base64.getDecoder().decode(encryptedData);
////        return new String(cipher.doFinal(decoded));
////    }
////}
//package com.komal.template_backend.Util;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.security.SecureRandom;
//import java.util.Arrays;
//import java.util.Base64;
//
//public class AESUtil {
//
//    private static final String ALGORITHM = "AES";
//    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
//
//    // Load key from environment variable
//    private static SecretKeySpec getKeySpec() {
//        String key = System.getenv("AESKey"); // must be 32 bytes for AES-256
//        if (key == null || key.isEmpty()) {
//            throw new IllegalStateException("AESKey not found in environment!");
//        } else {
//            System.out.println(" AES Key loaded successfully.");
//        }
//        byte[] decodedKey = Base64.getDecoder().decode(key);
//        if (decodedKey.length != 32) {
//            throw new IllegalStateException("AESKey must be 32 bytes for AES-256!");
//        }
//        return new SecretKeySpec(decodedKey, ALGORITHM);
//    }
//
//    // Encrypt with random IV per record
//    public static String encrypt(String data) throws Exception {
//        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        byte[] iv = new byte[16]; // 16 bytes for AES block
//        SecureRandom random = new SecureRandom();
//        random.nextBytes(iv);
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//        cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), ivSpec);
//        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//
//        // Prepend IV to encrypted data
//        byte[] combined = new byte[iv.length + encrypted.length];
//        System.arraycopy(iv, 0, combined, 0, iv.length);
//        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
//
//        return Base64.getEncoder().encodeToString(combined);
//    }
//
//    // Decrypt with IV extracted from data
//    public static String decrypt(String encryptedData) throws Exception {
//        byte[] combined = Base64.getDecoder().decode(encryptedData);
//        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
//        byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);
//
//        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), new IvParameterSpec(iv));
//        byte[] decrypted = cipher.doFinal(encrypted);
//
//        return new String(decrypted, StandardCharsets.UTF_8);
//    }
//}
// AESUtil.java
//package com.komal.template_backend.Util;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.UUID;
//
//public class AESUtil {
//
//    private static final String ALGORITHM = "AES";
//    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
//    private static final String SPECIAL_CHARS = "!@#$%^&*()_+";
//
//    // Generate dynamic AES key per record
//    public static SecretKeySpec generateKey(String mobile, String uniqueId, String dob, LocalDateTime donationDate, String salt) throws Exception {
//
//        int month = donationDate.getMonthValue();
//        String monthSymbol = String.valueOf(SPECIAL_CHARS.charAt((month - 1) % SPECIAL_CHARS.length()));
//        String combined = mobile + uniqueId + dob + donationDate.toString() + salt + monthSymbol;
//        MessageDigest sha = MessageDigest.getInstance("SHA-256");
//        byte[] keyBytes = sha.digest(combined.getBytes(StandardCharsets.UTF_8));
//        return new SecretKeySpec(keyBytes, ALGORITHM);
//    }
//    // Encrypt with random IV
//    public static String encrypt(String data, SecretKeySpec key) throws Exception {
//        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        byte[] iv = new byte[16];
//        new SecureRandom().nextBytes(iv);
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
//        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//        byte[] combined = new byte[iv.length + encrypted.length];
//        System.arraycopy(iv, 0, combined, 0, iv.length);
//        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
//        return Base64.getEncoder().encodeToString(combined);
//    }
//    public static String decrypt(String encryptedData, SecretKeySpec key) throws Exception {
//        if (encryptedData == null || encryptedData.isEmpty()) return null; // <--- safe check
//        byte[] combined = Base64.getDecoder().decode(encryptedData);
//        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
//        byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);
//
//        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
//        byte[] decrypted = cipher.doFinal(encrypted);
//
//        return new String(decrypted, StandardCharsets.UTF_8);
//    }
//    // Decrypt with IV
////    public static String decrypt(String encryptedData, SecretKeySpec key) throws Exception {
////        byte[] combined = Base64.getDecoder().decode(encryptedData);
////        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
////        byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);
////        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
////        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
////        byte[] decrypted = cipher.doFinal(encrypted);
////        return new String(decrypted, StandardCharsets.UTF_8);
////    }
//
//    // Generate random salt
//    public static String generateSalt() {
//        return UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    // Simple reversible obfuscation for storing key
//    public static String obfuscateKey(SecretKeySpec key) {
//        byte[] bytes = key.getEncoded();
//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] = (byte) (bytes[i] + 3); // simple addition
//        }
//        return Base64.getEncoder().encodeToString(bytes);
//    }
//
//    public static SecretKeySpec deobfuscateKey(String obfuscatedKey) {
//        byte[] bytes = Base64.getDecoder().decode(obfuscatedKey);
//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] = (byte) (bytes[i] - 3); // reverse addition
//        }
//        return new SecretKeySpec(bytes, ALGORITHM);
//    }
//
//    // Optional: masking
//    public static String maskMobile(String mobile) {
//        if (mobile == null || mobile.length() < 8) return mobile;
//        return mobile.replaceAll("(\\d{2})\\d{4,6}(\\d{2})", "$1******$2");
//    }
//}
package com.komal.template_backend.Util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+";

    // 🔍 Generate dynamic AES key per record (debug-enabled)
    public static SecretKeySpec generateKey(String mobile, String uniqueId, String dob, LocalDateTime donationDate, String salt) throws Exception {
        System.out.println("🔐 [AESUtil] Generating AES key...");
        System.out.println("   mobile: " + safe(mobile));
        System.out.println("   uniqueId: " + safe(uniqueId));
        System.out.println("   dob: " + safe(dob));
        System.out.println("   donationDate: " + (donationDate != null ? donationDate.toString() : "null"));
        System.out.println("   salt: " + salt);

        int month = (donationDate != null ? donationDate.getMonthValue() : 1);
        String monthSymbol = String.valueOf(SPECIAL_CHARS.charAt((month - 1) % SPECIAL_CHARS.length()));
        String combined = (mobile == null ? "" : mobile)
                + (uniqueId == null ? "" : uniqueId)
                + (dob == null ? "" : dob)
                + (donationDate != null ? donationDate.toString() : "")
                + salt
                + monthSymbol;

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(combined.getBytes(StandardCharsets.UTF_8));

        System.out.println("   ✅ AES key generated successfully. Key length: " + keyBytes.length);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // 🔍 Encrypt with random IV
    public static String encrypt(String data, SecretKeySpec key) throws Exception {
        if (data == null || data.isEmpty()) {
            System.out.println("⚠️ [AESUtil] Skipping encryption: data is null or empty");
            return null;
        }
        if (key == null) {
            System.out.println("❌ [AESUtil] Encryption failed: key is null");
            throw new IllegalArgumentException("Encryption key cannot be null");
        }

        System.out.println("🔒 [AESUtil] Encrypting data (len=" + data.length() + ")...");

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        String encoded = Base64.getEncoder().encodeToString(combined);
        System.out.println("   ✅ Encryption successful. Output length: " + encoded.length());
        return encoded;
    }

    // 🔍 Decrypt with IV
    public static String decrypt(String encryptedData, SecretKeySpec key) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            System.out.println("⚠️ [AESUtil] Skipping decryption: input is null or empty");
            return null;
        }
        if (key == null) {
            System.out.println("❌ [AESUtil] Decryption failed: key is null");
            throw new IllegalArgumentException("Decryption key cannot be null");
        }

        System.out.println("🔓 [AESUtil] Attempting decryption...");

        byte[] combined;
        try {
            combined = Base64.getDecoder().decode(encryptedData);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [AESUtil] Invalid Base64 input. Data: " + snippet(encryptedData));
            throw e;
        }

        if (combined.length < 17) {
            System.out.println("❌ [AESUtil] Invalid encrypted data length: " + combined.length);
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
        byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        try {
            byte[] decrypted = cipher.doFinal(encrypted);
            String result = new String(decrypted, StandardCharsets.UTF_8);
            System.out.println("   ✅ Decryption successful. Output length: " + result.length());
            return result;
        } catch (Exception e) {
            System.out.println("❌ [AESUtil] Decryption failed: " + e.getMessage());
            throw e;
        }
    }

    // Generate random salt
    public static String generateSalt() {
        String salt = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("🧂 [AESUtil] Generated salt: " + salt);
        return salt;
    }

    // Simple reversible obfuscation for storing key
    public static String obfuscateKey(SecretKeySpec key) {
        if (key == null) {
            System.out.println("⚠️ [AESUtil] Cannot obfuscate null key");
            return null;
        }
        byte[] bytes = key.getEncoded();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] + 3);
        }
        String encoded = Base64.getEncoder().encodeToString(bytes);
        System.out.println("🔑 [AESUtil] Key obfuscated successfully (len=" + encoded.length() + ")");
        return encoded;
    }

    public static SecretKeySpec deobfuscateKey(String obfuscatedKey) {
        if (obfuscatedKey == null || obfuscatedKey.isEmpty()) {
            System.out.println("⚠️ [AESUtil] No key to deobfuscate (null/empty)");
            return null;
        }

        System.out.println("🔑 [AESUtil] Deobfuscating key...");
        try {
            byte[] bytes = Base64.getDecoder().decode(obfuscatedKey);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (bytes[i] - 3);
            }
            System.out.println("   ✅ Deobfuscation complete. Key length: " + bytes.length);
            return new SecretKeySpec(bytes, ALGORITHM);
        } catch (Exception e) {
            System.out.println("❌ [AESUtil] Failed to deobfuscate key: " + e.getMessage());
            throw e;
        }
    }

    // Optional: masking
    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 8) return mobile;
        return mobile.replaceAll("(\\d{2})\\d{4,6}(\\d{2})", "$1******$2");
    }

    // Utility: shorten sensitive prints
    private static String snippet(String text) {
        if (text == null) return "null";
        return text.length() > 20 ? text.substring(0, 20) + "..." : text;
    }

    private static String safe(String val) {
        if (val == null || val.isEmpty()) return "null/empty";
        return val.length() > 6 ? val.substring(0, 3) + "..." + val.substring(val.length() - 3) : val;
    }
    public static String encryptIfNotNull(String value, SecretKeySpec key) throws Exception {
        if (value == null || value.isEmpty()) return null;
        return encrypt(value, key);
    }
}
