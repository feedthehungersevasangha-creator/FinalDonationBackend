package com.komal.template_backend.controller;

import com.komal.template_backend.service.AdminAuthService;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminService;

    public AdminAuthController(AdminAuthService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> req) {
        boolean ok = adminService.register(req.get("email"), req.get("password"));
        return Map.of("success", ok);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {
        boolean valid = adminService.login(req.get("email"), req.get("password"));
        return Map.of("valid", valid);
    }
    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> req) throws UnsupportedEncodingException {
        boolean sent = adminService.initiateForgotPassword(req.get("email"));
        String message = sent
                ? "Reset link sent to your email."
                : "If this email exists, a reset link was sent.";
        return Map.of("sent", sent, "message", message);
    }

//    @PostMapping("/forgot-password")
//    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> req) throws UnsupportedEncodingException {
//        boolean sent = adminService.initiateForgotPassword(req.get("email"));
//        return Map.of("sent", sent);
//    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> req) {
        boolean done = adminService.resetPassword(req.get("token"), req.get("newPassword"));
        return Map.of("reset", done);
    }
}
