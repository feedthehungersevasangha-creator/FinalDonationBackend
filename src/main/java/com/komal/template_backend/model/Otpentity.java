package com.komal.template_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection="Otps")
public class Otpentity {
    @Id
    private String id;

    private String email;
    private String otpHash;
    private String salt;
    private int attempts = 0;

    private boolean blocked = false; // for brute-force protection
    private Date blockedAt;
    private Date createdAt = new Date();                  // when OTP was created
    private Date expiresAt = new Date(System.currentTimeMillis() + 5 * 60 * 1000); // expires in 5 mins

    // Constructor
    public Otpentity(String email, String otpHash, String salt) {
        this.email = email;
        this.otpHash = otpHash;
        this.salt = salt;
    }

    // Getters & Setters
    public Date getBlockedAt() { return blockedAt; }
    public void setBlockedAt(Date blockedAt) { this.blockedAt = blockedAt; }
    public String getId() { return id; }

    public String getEmail() { return email; }

    public String getOtpHash() { return otpHash; }

    public String getSalt() { return salt; }

    public int getAttempts() { return attempts; }

    public void setAttempts(int attempts) { this.attempts = attempts; }

    public boolean isBlocked() { return blocked; }

    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public Date getCreatedAt() { return createdAt; }

    public Date getExpiresAt() { return expiresAt; }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void setEmail(String email) {
        this.email = email;

    }
//    @Id
//    private String id;
//    private String email;
//    private String otpHash;
//    private String salt;
//    private int attempts;
//     private Date createdAt;
//    public Otpentity(String email, String otpHash, String salt) {
//        this.email = email;
//        this.salt = salt;
//        this.otpHash = otpHash;
//        attempts=0;
//        @TimeToLive  // ✅ Let Spring manage TTL
//        private Long ttl = 300L; // 300 seconds = 5 minutes
//
//        this.createdAt= new Date();
//    }
//    public String getEmail() {
//        return email;
//    }
//
//    public String getOtpHash() {
//        return otpHash;
//    }
//
//    public String getSalt() {
//        return salt;
//    }
//
//    public int getAttempts() {
//        return attempts;
//    }
//
//    public void setAttemps(int attemps) {
//        this.attempts = attemps;
//    }
}
