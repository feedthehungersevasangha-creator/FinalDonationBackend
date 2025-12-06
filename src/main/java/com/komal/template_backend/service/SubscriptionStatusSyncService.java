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
