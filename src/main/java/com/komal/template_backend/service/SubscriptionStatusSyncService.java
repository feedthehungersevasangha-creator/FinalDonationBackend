package com.komal.template_backend.service;
// test
import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import com.razorpay.RazorpayClient;
import com.razorpay.Subscription;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionStatusSyncService {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    private final DonationRepo donationRepo;
    private final MailService mailService;
    private final PdfReceiptServic pdfReceiptService;
    private final DonationService donationService;

    public SubscriptionStatusSyncService(
            DonationRepo donationRepo,
            MailService mailService,
            PdfReceiptServic pdfReceiptService,
            DonationService donationService
    ) {
        this.donationRepo = donationRepo;
        this.mailService = mailService;
        this.pdfReceiptService = pdfReceiptService;
        this.donationService = donationService;
    }

    // ✅ EVERY 10 MINUTES
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void syncSubscriptionsFromRazorpay() {

        System.out.println("🔁 Razorpay → Mongo subscription sync started");

        List<Donourentity> donors = donationRepo.findActiveSubscriptions();

        if (donors.isEmpty()) {
            System.out.println("ℹ No subscriptions found");
            return;
        }

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            for (Donourentity donor : donors) {

                if (donor.getSubscriptionId() == null) continue;

                try {
                    Subscription subscription =
                            client.subscriptions.fetch(donor.getSubscriptionId());

                    JSONObject subJson = subscription.toJson();
                    String razorpayStatus =
                            subJson.optString("status", "UNKNOWN").toUpperCase();

                    String currentStatus = donor.getSubscriptionStatus();

                    if (!razorpayStatus.equals(currentStatus)) {

                        donor.setSubscriptionStatus(razorpayStatus);

                        // ✅ AUTHENTICATED / ACTIVE
                        if ("AUTHENTICATED".equals(razorpayStatus)
                                || "ACTIVE".equals(razorpayStatus)) {

                            donor.setStatus("SUCCESS");

                            // ✅ SEND MAIL ONLY ONCE
                            if (Boolean.FALSE.equals(donor.getMandateMailSent())) {

                                try {
                                    Donourentity decrypted =
                                            donationService.findByIdDecrypt(donor.getId());

                                    byte[] pdf =
                                            pdfReceiptService.generateMandateConfirmation(decrypted);

                                    mailService.sendDonationReceiptWithAttachment(
                                            decrypted.getEmail(),
                                            decrypted.getFirstName() + " " + decrypted.getLastName(),
                                            decrypted.getMonthlyAmount(),
                                            decrypted.getSubscriptionId(),
                                            pdf,
                                            "Mandate_Confirmation_" + decrypted.getSubscriptionId() + ".pdf"
                                    );

                                    donor.setMandateMailSent(true);

                                    System.out.println(
                                            "📧 Mandate confirmation mail sent → "
                                                    + donor.getSubscriptionId()
                                    );

                                } catch (Exception mailEx) {
                                    System.out.println("❌ Mail failed for " + donor.getSubscriptionId());
                                    mailEx.printStackTrace();
                                }
                            }

                        } else if ("HALTED".equals(razorpayStatus)
                                || "CANCELLED".equals(razorpayStatus)) {

                            donor.setStatus("FAILED");
                        }

                        donationRepo.save(donor);

                        System.out.println(
                                "✅ Synced: " + donor.getSubscriptionId() +
                                " → " + razorpayStatus
                        );
                    }

                } catch (Exception perSubEx) {
                    System.out.println("⚠ Failed syncing " + donor.getSubscriptionId());
                    perSubEx.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Razorpay sync crashed");
            e.printStackTrace();
        }
    }
}
// package com.komal.template_backend.service;
// // test
// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import com.komal.template_backend.service.MailService;
// import com.komal.template_backend.service.PdfReceiptServic;
// import com.komal.template_backend.service.DonationService;
// import com.razorpay.RazorpayClient;
// import com.razorpay.Subscription;
// import org.json.JSONObject;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import java.util.List;
// @Service
// public class SubscriptionStatusSyncService {

//     @Value("${razorpay.key_id}")
//     private String keyId;

//     @Value("${razorpay.key_secret}")
//     private String keySecret;

//     private final DonationRepo donationRepo;
//     private final MailService mailService;
//     private final PdfReceiptServic pdfReceiptService;
//     private final DonationService donationService;

//     public SubscriptionStatusSyncService(
//             DonationRepo donationRepo,
//             MailService mailService,
//             PdfReceiptServic pdfReceiptService,
//             DonationService donationService
//     ) {
//         this.donationRepo = donationRepo;
//         this.mailService = mailService;
//         this.pdfReceiptService = pdfReceiptService;
//         this.donationService = donationService;
//     }

//     // ✅ Runs every 10 minutes
//     @Scheduled(fixedDelay = 10 * 60 * 1000)
//     public void syncSubscriptionsFromRazorpay() {

//         System.out.println("🔁 Subscription watcher started");

//         // List<Donourentity> donors = donationRepo.findActiveSubscriptions();
//         List<Donourentity> donors = donationRepo.findSubscriptionsForSync();

//         if (donors.isEmpty()) return;

//         try {
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);

//             for (Donourentity donor : donors) {

//                 if (donor.getSubscriptionId() == null) continue;

//                 Subscription subscription =
//                         client.subscriptions.fetch(donor.getSubscriptionId());

//                 JSONObject sub = subscription.toJson();
//                 String rzpStatus =
//                         sub.optString("status", "UNKNOWN").toUpperCase();

//                 String localStatus = donor.getSubscriptionStatus();

//                 // ✅ No change
//                 if (rzpStatus.equals(localStatus)) continue;

//                 donor.setSubscriptionStatus(rzpStatus);
// if ("CREATED".equals(rzpStatus)) {
//     donor.setMandateStatus("NOT_STARTED");
//     donor.setStatus("PENDING");
// }

//                 // ==================================================
//                 // ✅ MANDATE BANK APPROVAL (DERIVED)
//                 // ==================================================
//          else if ("CANCELLED".equals(rzpStatus)
//                         || "HALTED".equals(rzpStatus)
//                         || "EXPIRED".equals(rzpStatus)) {

//                     donor.setStatus("FAILED");
//                     donor.setMandateStatus("CANCELLED");
//                 }

//                 donationRepo.save(donor);
//                 System.out.println("✅ Synced " + donor.getSubscriptionId() + " → " + rzpStatus);
//             }

//         } catch (Exception e) {
//             System.out.println("❌ Subscription watcher crashed");
//             e.printStackTrace();
//         }
//     }
// }
// ------------------------------------------------------------------------------------------------
// package com.komal.template_backend.service;

// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import com.komal.template_backend.service.MailService;
// import com.komal.template_backend.service.PdfReceiptServic;
// import com.komal.template_backend.service.DonationService;
// import com.razorpay.RazorpayClient;
// import com.razorpay.Subscription;

// import org.json.JSONObject;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.List;

// @Service
// public class SubscriptionStatusSyncService {

//     @Value("${razorpay.key_id}")
//     private String keyId;

//     @Value("${razorpay.key_secret}")
//     private String keySecret;

//     private final DonationRepo donationRepo;
//     private final MailService mailService;
//     private final PdfReceiptServic pdfReceiptService;
//     private final DonationService donationService;

//     public SubscriptionStatusSyncService(
//             DonationRepo donationRepo,
//             MailService mailService,
//             PdfReceiptServic pdfReceiptService,
//             DonationService donationService
//     ) {
//         this.donationRepo = donationRepo;
//         this.mailService = mailService;
//         this.pdfReceiptService = pdfReceiptService;
//         this.donationService = donationService;
//     }

//     // Runs every 10 minutes
//     @Scheduled(fixedDelay = 10 * 60 * 1000)
//     public void syncSubscriptionsFromRazorpay() {

//         List<Donourentity> donors = donationRepo.findSubscriptionsForSync();
//         if (donors.isEmpty()) return;

//         try {
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);

//             for (Donourentity donor : donors) {

//                 if (donor.getSubscriptionId() == null) continue;

//                 Subscription subscription =
//                         client.subscriptions.fetch(donor.getSubscriptionId());

//                 JSONObject sub = subscription.toJson();
//                 String rzpStatus =
//                         sub.optString("status", "UNKNOWN").toUpperCase();

//                 if (rzpStatus.equals(donor.getSubscriptionStatus())) {
//                     continue;
//                 }

//                 donor.setSubscriptionStatus(rzpStatus);

//                 if ("AUTHENTICATED".equals(rzpStatus)) {
//                     donor.setMandateStatus("USER_AUTHENTICATED");
//                     donor.setStatus("USER_AUTHENTICATED");
//                         if (Boolean.FALSE.equals(donor.getSetupMailSent())) {
//                         mailService.sendMail(
//                                 donor.getEmail(),
//                                 "Mandate authentication successful",
//                                 "Dear " + donor.getFirstName() + ",\n\n"
//                               + "You have successfully authenticated the mandate.\n"
//                               + "Bank approval may take 24–48 hours.\n\n"
//                               + "Regards,\nFeed The Hunger Seva Sangha"
//                         );
//                         donor.setSetupMailSent(true);
//                         donationRepo.save(donor);
//                     }
//                 }

//                 if ("ACTIVE".equals(rzpStatus)) {
//                     donor.setMandateStatus("AUTHORIZED");
//                     donor.setStatus("SUCCESS");

//                     if (Boolean.FALSE.equals(donor.getMandateMailSent())) {
//                         Donourentity decrypted =
//                                 donationService.findByIdDecrypt(donor.getId());

//                         byte[] pdf =
//                                 pdfReceiptService.generateMandateConfirmation(decrypted);

//                         mailService.sendDonationReceiptWithAttachment(
//                                 decrypted.getEmail(),
//                                 decrypted.getFirstName() + " " + decrypted.getLastName(),
//                                 decrypted.getMonthlyAmount(),
//                                 donor.getSubscriptionId(),
//                                 pdf,
//                                 "Mandate_Approved_" + donor.getSubscriptionId() + ".pdf"
//                         );

//                         donor.setMandateMailSent(true);
//                     }
//                 }

//                 if ("CANCELLED".equals(rzpStatus)
//                         || "HALTED".equals(rzpStatus)
//                         || "EXPIRED".equals(rzpStatus)) {

//                     donor.setStatus("FAILED");
//                     donor.setMandateStatus("CANCELLED");
//                 }

//                 donationRepo.save(donor);
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }




