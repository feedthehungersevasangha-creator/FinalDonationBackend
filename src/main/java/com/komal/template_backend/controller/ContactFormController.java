package com.komal.template_backend.controller;

import com.komal.template_backend.model.ContactRequest;
import com.komal.template_backend.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactFormController {
    private final MailService mailService;

    @Autowired
    public ContactFormController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping
    public ResponseEntity<String> sendContactMessage(@RequestBody ContactRequest request) {
        try {
            mailService.sendUserMessage(request.getEmail(), request.getTitle(), request.getMessage());
            return ResponseEntity.ok("Message sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send message: " + e.getMessage());
        }
    }
}
