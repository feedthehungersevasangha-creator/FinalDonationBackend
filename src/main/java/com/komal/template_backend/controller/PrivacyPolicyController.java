package com.komal.template_backend.controller;
//
//import com.komal.template_backend.model.PrivacyPolicy;
//import com.komal.template_backend.service.PrivacyPolicyService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/privacy-policy")
////@CrossOrigin(origins = "*")
//public class PrivacyPolicyController {
//    private final PrivacyPolicyService service;
//
//    public PrivacyPolicyController(PrivacyPolicyService service) {
//        this.service = service;
//    }
//
//    @GetMapping
//    public PrivacyPolicy getPolicy() {
//        return service.getPolicy();
//    }
//
//    @PutMapping
//    public PrivacyPolicy updatePolicy(@RequestBody PrivacyPolicy policy) {
//        return service.updatePolicy(policy);
//    }
//}
import com.komal.template_backend.model.PrivacyPolicy;
import com.komal.template_backend.service.PrivacyPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privacy-policy")
//@CrossOrigin(origins = "*") // enable if you serve frontend from another origin
public class PrivacyPolicyController {
    private final PrivacyPolicyService service;

    public PrivacyPolicyController(PrivacyPolicyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PrivacyPolicy> getPolicy() {
        PrivacyPolicy p = service.getPolicy();
        return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }

    @PutMapping
    public ResponseEntity<PrivacyPolicy> updatePolicy(@RequestBody PrivacyPolicy policy) {
        return ResponseEntity.ok(service.updatePolicy(policy));
    }
}
