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
    @Value("${app.mail.username}")   // 👈 recipient from properties
    private String adminEmail;


    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail); // Important
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
    public  void sendDonationReceipt(String toEmail, String donorName, double amount, String paymentId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Thank You for Your Donation ❤️");

        String body = String.format(
                "Dear %s,\n\nThank you for your generous donation to Feed The Hunger Foundation!\n\n" +
                        "Donation Details:\n" +
                        "--------------------------------\n" +
                        "Amount: ₹%.2f\n" +
                        "Payment ID: %s\n" +
                        "Date: %s\n" +
                        "--------------------------------\n\n" +
                        "Your support helps us feed more families in need.\n\n" +
                        "With gratitude,\nFeed The Hunger Foundation",
                donorName, amount, paymentId, java.time.LocalDateTime.now()
        );

        message.setText(body);
        mailSender.send(message);
    }
}

