package com.komal.template_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(MailService.class);
    @Value("${app.mail.recipient}")   // 👈 recipient from properties
    private String hostEmail;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
            logger.info("mail  send to {}", to);
        }catch (Exception s) {
            logger.error("Failed to send email to {}:{}", to, s.getMessage(), s);
            throw s;
        }
    }
    public void sendUserMessage(String fromEmail, String subject, String text) {
            SimpleMailMessage message1= new SimpleMailMessage();
            message1.setTo(hostEmail); // admin/support email
            message1.setSubject("Contact Us: " + subject);
            message1.setText("From: " + fromEmail + "\n\nMessage:\n" + text);
            try {
                mailSender.send(message1);
                logger.info("User message from {} sent successfully", fromEmail);
            } catch (Exception s) {
                logger.error("Failed to send user message from {}: {}", fromEmail, s.getMessage(), s);
                throw s;
            }
        }
}

