
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

    public SubscriptionStatusSyncService(DonationRepo donationRepo) {
        this.donationRepo = donationRepo;
    }

    // ✅ Runs every 10 minutes
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void syncSubscriptionsFromRazorpay() {

        System.out.println("🔁 Mongo Sync: Subscription reconciliation started");

        List<Donourentity> donors =
                donationRepo.findActiveSubscriptions();

        if (donors.isEmpty()) {
            System.out.println("ℹ No active subscriptions to sync");
            return;
        }

        try {
            RazorpayClient client =
                    new RazorpayClient(keyId, keySecret);

            for (Donourentity donor : donors) {

                String subId = donor.getSubscriptionId();
                if (subId == null) continue;

                try {
                    Subscription sub =
                            client.subscriptions.fetch(subId);

                    JSONObject json = sub.toJson();
                    String razorpayStatus =
                            json.optString("status", "")
                                .toUpperCase();

                    if (!razorpayStatus.equals(donor.getSubscriptionStatus())) {

                        donor.setSubscriptionStatus(razorpayStatus);

                        if ("AUTHENTICATED".equals(razorpayStatus)
                                || "ACTIVE".equals(razorpayStatus)) {

                            donor.setStatus("SUCCESS");

                        } else if ("HALTED".equals(razorpayStatus)
                                || "CANCELLED".equals(razorpayStatus)) {

                            donor.setStatus("FAILED");
                        }

                        donationRepo.save(donor);

                        System.out.println(
                                "✅ Mongo Sync updated: " + subId
                                + " → " + razorpayStatus
                        );
                    }

                } catch (Exception ex) {
                    System.out.println(
                            "⚠ Failed to sync subscription " + subId
                    );
                    ex.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Mongo sync job crashed");
            e.printStackTrace();
        }
    }
}
