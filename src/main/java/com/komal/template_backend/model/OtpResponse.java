package com.komal.template_backend.model;

public class OtpResponse {
    private boolean success;    // was verification successful
    private String message;     // human-readable message
    private int attemptsLeft;   // remaining attempts
    private long blockedUntil;  // timestamp when user can retry (0 if not blocked)

    public OtpResponse(boolean success, String message, int attemptsLeft, long blockedUntil) {
        this.success = success;
        this.message = message;
        this.attemptsLeft = attemptsLeft;
        this.blockedUntil = blockedUntil;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public long getBlockedUntil() { return blockedUntil; }
}
