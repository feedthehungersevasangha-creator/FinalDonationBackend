//package com.komal.template_backend.controller;
//
//import com.komal.template_backend.service.OtpService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/otp")
////@CrossOrigin(origins = "http://localhost:5173")
//public class OtpMailController {
//    private final OtpService otpService;
//
//
//    public OtpMailController(OtpService otpService) {
//        this.otpService = otpService;
//    }
//    @PostMapping("/request")
//    public ResponseEntity<String> requestOtp(@RequestParam String email){
//        try{
//            otpService.requestOtp(email);
//            return ResponseEntity.ok("OTP sent to "+email);
//        } catch (Exception e) {
//return ResponseEntity.status(500) .body("error"+e.getMessage())  ;     }
//    }
//    @PostMapping("/verify")
//    public ResponseEntity<String> verifyOtp(@RequestParam String email,@RequestParam String otp){
//        try {
//            boolean success = otpService.verifyOtp(email, otp);
//            return success ? ResponseEntity.ok("Otp verified successfully") :
//                    ResponseEntity.badRequest().body("invalied otp or expired");
//        }catch (Exception e) {
//                return ResponseEntity.status(500).body("Error: " + e.getMessage());
//            }
//        }
//    }
//
//
package com.komal.template_backend.controller;

import com.komal.template_backend.model.OtpResponse;
import com.komal.template_backend.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
//@CrossOrigin(origins = "http://localhost:5173")
public class OtpMailController {

    private final OtpService otpService;

    public OtpMailController(OtpService otpService) {
        this.otpService = otpService;
    }
    // ------------------ Request OTP ------------------
    @PostMapping("/request")
    public ResponseEntity<String> requestOtp(@RequestParam String email) {
        try {
            otpService.requestOtp(email);
            return ResponseEntity.ok("OTP sent successfully to " + email);
        } catch (RuntimeException e) {
            // Blocked users
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating OTP: " + e.getMessage());
        }
    }
    // ------------------ Verify OTP ------------------
    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(@RequestParam String email,
                                                 @RequestParam String otp) {
        try {
            OtpResponse response = otpService.verifyOtp(email, otp);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response); // OTP verified
            } else if (response.getBlockedUntil() > 0) {
                // OTP blocked → 403 Forbidden
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            } else {
                // Invalid OTP → 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OtpResponse(false, "Server error: " + e.getMessage(), 0, 0));
        }
    }
}
