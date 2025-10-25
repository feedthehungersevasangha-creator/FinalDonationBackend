package com.komal.template_backend.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private static final String SECRET_KEY = "123"; // ideally from env

    @PostMapping("/admin")
    public ResponseEntity<Map<String, Boolean>> login(@RequestBody Map<String, String> request) {
        String passkey = request.get("passkey");
        boolean valid = SECRET_KEY.equals(passkey);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}