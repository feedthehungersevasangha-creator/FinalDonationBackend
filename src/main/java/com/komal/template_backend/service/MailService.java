// package com.komal.template_backend.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// @Service
// public class MailService {
//     private final JavaMailSender mailSender;
//     private final Logger logger = LoggerFactory.getLogger(MailService.class);
//     @Value("${app.mail.recipient}")   // 👈 recipient from properties
//     private String hostEmail;

//     @Autowired
//     public MailService(JavaMailSender mailSender) {
//         this.mailSender = mailSender;
//     }

//     public void sendMail(String to, String subject, String text) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setTo(to);
//         message.setSubject(subject);
//         message.setText(text);
//         try {
//             mailSender.send(message);
//             logger.info("mail  send to {}", to);
//         }catch (Exception s) {
//             logger.error("Failed to send email to {}:{}", to, s.getMessage(), s);
//             throw s;
//         }
//     }
//     public void sendUserMessage(String fromEmail, String subject, String text) {
//             SimpleMailMessage message1= new SimpleMailMessage();
//             message1.setTo(hostEmail); // admin/support email
//             message1.setSubject("Contact Us: " + subject);
//             message1.setText("From: " + fromEmail + "\n\nMessage:\n" + text);
//             try {
//                 mailSender.send(message1);
//                 logger.info("User message from {} sent successfully", fromEmail);
//             } catch (Exception s) {
//                 logger.error("Failed to send user message from {}: {}", fromEmail, s.getMessage(), s);
//                 throw s;
//             }
//         }
//     public  void sendDonationReceipt(String toEmail, String donorName, double amount, String paymentId) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setFrom(hostEmail); // Important
//         message.setTo(toEmail);
//         message.setSubject("Thank You for Your Donation ❤️");

        // String body = String.format(
        //         "Dear %s,\n\nThank you for your generous donation to Feed The Hunger Foundation!\n\n" +
        //                 "Donation Details:\n" +
        //                 "--------------------------------\n" +
        //                 "Amount: ₹%.2f\n" +
        //                 "Payment ID: %s\n" +
        //                 "Date: %s\n" +
        //                 "--------------------------------\n\n" +
        //                 "Your support helps us feed more families in need.\n\n" +
        //                 "With gratitude,\nFeed The Hunger Foundation",
        //         donorName, amount, paymentId, java.time.LocalDateTime.now()
        // );

//         message.setText(body);
//         mailSender.send(message);
//     }
// }

package com.komal.template_backend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(MailService.class);
    @Value("${app.mail.recipient}")   // 👈 recipient from properties
    private String hostEmail;
    @Autowired
    private JavaMailSender javaMailSender;

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
//     public  void sendDonationReceipt(String toEmail, String donorName, double amount, String paymentId) {
//         SimpleMailMessage message = new SimpleMailMessage();
// //        message.setFrom("supporter.services@feedthehungersevasangha.org"); // Important
//         message.setFrom(hostEmail); // Important

//         message.setTo(toEmail);
//         message.setSubject("Thank You for Your Donation ❤️");
//         String body = String.format(
//                 "Dear %s,\n\nThank you for your generous donation to Feed The Hunger Foundation!\n\n" +
//                         "Donation Details:\n" +
//                         "--------------------------------\n" +
//                         "Amount: ₹%.2f\n" +
//                         "Payment ID: %s\n" +
//                         "Date: %s\n" +
//                         "--------------------------------\n\n" +
//                         "Your support helps us feed more families in need.\n\n" +
//                         "With gratitude,\nFeed The Hunger Foundation",
//                 donorName, amount, paymentId, java.time.LocalDateTime.now()
//         );
//         message.setText(body);
//         mailSender.send(message);
//     }
    // new overloaded variant to send PDF attachment
    public void sendDonationReceiptWithAttachment(String email, String name, double amount, String paymentId, byte[] pdfBytes, String filename) {
        try {
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setTo(email);
            helper.setSubject("Donation Receipt - " + paymentId);
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
                name, amount, paymentId, java.time.LocalDateTime.now()
        );
            helper.setText(body);
            helper.addAttachment(filename, new ByteArrayResource(pdfBytes));
            javaMailSender.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  public void sendMandateConfirmationMail(
        String email,
        String name,
        String subscriptionId,
        double monthlyAmount,
        byte[] pdfBytes
) {
    try {
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo(email);
        helper.setSubject("e-Mandate Activated Successfully");

        String body =
                "Dear " + name + ",\n\n" +
                "Your e-Mandate has been successfully activated for recurring donations to " +
                "Feed The Hunger Seva Sangha.\n\n" +
                "Mandate Details:\n" +
                "--------------------------------\n" +
                "Mandate ID: " + subscriptionId + "\n" +
                "Monthly Amount: ₹" + String.format("%.2f", monthlyAmount) + "\n" +
                "Frequency: Monthly\n" +
                "--------------------------------\n\n" +
                "Important:\n" +
                "• Please maintain sufficient balance in your bank account\n" +
                "• Failed debits may attract bank charges or mandate penalties\n" +
                "• You can cancel this mandate anytime through your bank or by contacting us\n\n" +
                "Thank you for supporting our mission.\n\n" +
                "Warm regards,\n" +
                "Feed The Hunger Seva Sangha";

        helper.setText(body);
        helper.addAttachment(
                "Mandate_Confirmation_" + subscriptionId + ".pdf",
                new ByteArrayResource(pdfBytes)
        );

        javaMailSender.send(msg);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
        public void sendMandateCancellationMail(
        String email,
        String name,
        String subscriptionId
       
) {
    try {
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo(email);
        helper.setSubject("e-Mandate n Cancelled");

        String body =
                "Dear " + name + ",\n\n" +
                "This is to confirm that your e-Mandate / subscription with " +
                "Feed The Hunger Seva Sangha has been cancelled.\n\n" +
                "Subscription / Mandate ID: " + subscriptionId + "\n" +
                "Status: Inactive\n\n" +
                "No further auto-debits will be made from your bank account.\n\n" +
                "If you wish to restart your support, please contact us.\n\n" +
                "Warm regards,\n" +
                "Feed The Hunger Seva Sangha";

        helper.setText(body);
        

        javaMailSender.send(msg);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

      

}

