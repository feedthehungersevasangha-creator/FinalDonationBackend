// package com.komal.template_backend.service;
// // test
// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
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

//     // ✅ EVERY 10 MINUTES
//     @Scheduled(fixedDelay = 10 * 60 * 1000)
//     public void syncSubscriptionsFromRazorpay() {

//         System.out.println("🔁 Razorpay → Mongo subscription sync started");

//         List<Donourentity> donors = donationRepo.findActiveSubscriptions();

//         if (donors.isEmpty()) {
//             System.out.println("ℹ No subscriptions found");
//             return;
//         }

//         try {
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);

//             for (Donourentity donor : donors) {

//                 if (donor.getSubscriptionId() == null) continue;

//                 try {
//                     Subscription subscription =
//                             client.subscriptions.fetch(donor.getSubscriptionId());

//                     JSONObject subJson = subscription.toJson();
//                     String razorpayStatus =
//                             subJson.optString("status", "UNKNOWN").toUpperCase();

//                     String currentStatus = donor.getSubscriptionStatus();

//                     if (!razorpayStatus.equals(currentStatus)) {

//                         donor.setSubscriptionStatus(razorpayStatus);

//                         // ✅ AUTHENTICATED / ACTIVE
//                         if ("AUTHENTICATED".equals(razorpayStatus)
//                                 || "ACTIVE".equals(razorpayStatus)) {

//                             donor.setStatus("SUCCESS");

//                             // ✅ SEND MAIL ONLY ONCE
//                             if (Boolean.FALSE.equals(donor.getMandateMailSent())) {

//                                 try {
//                                     Donourentity decrypted =
//                                             donationService.findByIdDecrypt(donor.getId());

//                                     byte[] pdf =
//                                             pdfReceiptService.generateMandateConfirmation(decrypted);

//                                     mailService.sendDonationReceiptWithAttachment(
//                                             decrypted.getEmail(),
//                                             decrypted.getFirstName() + " " + decrypted.getLastName(),
//                                             decrypted.getMonthlyAmount(),
//                                             decrypted.getSubscriptionId(),
//                                             pdf,
//                                             "Mandate_Confirmation_" + decrypted.getSubscriptionId() + ".pdf"
//                                     );

//                                     donor.setMandateMailSent(true);

//                                     System.out.println(
//                                             "📧 Mandate confirmation mail sent → "
//                                                     + donor.getSubscriptionId()
//                                     );

//                                 } catch (Exception mailEx) {
//                                     System.out.println("❌ Mail failed for " + donor.getSubscriptionId());
//                                     mailEx.printStackTrace();
//                                 }
//                             }

//                         } else if ("HALTED".equals(razorpayStatus)
//                                 || "CANCELLED".equals(razorpayStatus)) {

//                             donor.setStatus("FAILED");
//                         }

//                         donationRepo.save(donor);

//                         System.out.println(
//                                 "✅ Synced: " + donor.getSubscriptionId() +
//                                 " → " + razorpayStatus
//                         );
//                     }

//                 } catch (Exception perSubEx) {
//                     System.out.println("⚠ Failed syncing " + donor.getSubscriptionId());
//                     perSubEx.printStackTrace();
//                 }
//             }

//         } catch (Exception e) {
//             System.out.println("❌ Razorpay sync crashed");
//             e.printStackTrace();
//         }
//     }
// }
package com.komal.template_backend.service;

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

    // ✅ Runs every 10 minutes (webhook fallback)
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void syncSubscriptionsFromRazorpay() {

        System.out.println("🔁 Razorpay → Mongo subscription sync started");

        // ✅ Only subscriptions that are NOT terminal
        List<Donourentity> donors = donationRepo.findSubscriptionsForSync();

        if (donors.isEmpty()) {
            System.out.println("ℹ No subscriptions to sync");
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

                    String localStatus = donor.getSubscriptionStatus();

                    // ✅ No change → skip
                    if (razorpayStatus.equals(localStatus)) {
                        continue;
                    }

                    donor.setSubscriptionStatus(razorpayStatus);

                    // -------------------------------
                    // STATUS MAPPING (SAFE & FINAL)
                    // -------------------------------

                    if ("AUTHENTICATED".equals(razorpayStatus)
                            || "ACTIVE".equals(razorpayStatus)) {

                        donor.setStatus("SUCCESS");

                        // ✅ Mandate mail ONLY if not yet sent
                        if (Boolean.FALSE.equals(donor.getMandateMailSent())
                                && donor.getRazorpayMandateId() != null) {

                            try {
                                Donourentity decrypted =
                                        donationService.findByIdDecrypt(donor.getId());

                                byte[] pdf =
                                        pdfReceiptService.generateMandateConfirmation(decrypted);

                                mailService.sendDonationReceiptWithAttachment(
                                        decrypted.getEmail(),
                                        decrypted.getFirstName() + " " + decrypted.getLastName(),
                                        decrypted.getMonthlyAmount(),
                                        decrypted.getRazorpayMandateId(),
                                        pdf,
                                        "Mandate_Confirmation_" + decrypted.getRazorpayMandateId() + ".pdf"
                                );

                                donor.setMandateMailSent(true);
                                System.out.println(
                                        "📧 Mandate confirmation mail sent → "
                                                + donor.getSubscriptionId()
                                );

                            } catch (Exception mailEx) {
                                System.out.println("❌ Mandate mail failed → " + donor.getSubscriptionId());
                                mailEx.printStackTrace();
                            }
                        }

                    } else if ("HALTED".equals(razorpayStatus)
                            || "CANCELLED".equals(razorpayStatus)
                            || "EXPIRED".equals(razorpayStatus)) {

                        donor.setStatus("FAILED");

                    } else if ("COMPLETED".equals(razorpayStatus)) {

                        // ✅ Completed = lifecycle finished successfully
                        donor.setStatus("SUCCESS");
                    }

                    donationRepo.save(donor);

                    System.out.println(
                            "✅ Synced subscription " + donor.getSubscriptionId()
                            + " → " + razorpayStatus
                    );

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


