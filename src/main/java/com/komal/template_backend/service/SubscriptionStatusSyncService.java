@Scheduled(fixedDelay = 10 * 60 * 1000)
public void syncSubscriptionsFromRazorpay() {

    System.out.println("🔁 Mongo Sync: Subscription reconciliation started");

    List<Donourentity> donors = donationRepo.findActiveSubscriptions();

    if (donors.isEmpty()) {
        System.out.println("ℹ No active subscriptions to sync");
        return;
    }

    try {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        for (Donourentity donor : donors) {

            String subId = donor.getSubscriptionId();
            if (subId == null) continue;

            try {
                Subscription sub = client.subscriptions.fetch(subId);
                JSONObject json = sub.toJson();
                String razorpayStatus =
                        json.optString("status", "")
                            .toUpperCase();

                String oldStatus = donor.getSubscriptionStatus();

                // ✅ STATUS CHANGE DETECTED
                if (!razorpayStatus.equals(oldStatus)) {

                    donor.setSubscriptionStatus(razorpayStatus);

                    if ("AUTHENTICATED".equals(razorpayStatus)
                            || "ACTIVE".equals(razorpayStatus)) {

                        donor.setStatus("SUCCESS");

                        // ✅ SEND EMAIL ONLY ON FIRST AUTHENTICATION
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
                                        "📧 Mandate confirmation email sent for " + subId
                                );

                            } catch (Exception mailEx) {
                                System.out.println(
                                        "⚠ Failed to send mandate mail for " + subId
                                );
                                mailEx.printStackTrace();
                            }
                        }

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
