// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import com.komal.template_backend.service.DonationService;
// import com.razorpay.Order;
// import com.razorpay.RazorpayClient;
// import org.json.JSONObject;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.time.LocalDateTime;
// import java.util.Base64;
// import java.util.Map;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/payment")
// public class RazorpayController {

//     @Value("${razorpay.key_id}")
//     private String keyId;

//     @Value("${razorpay.key_secret}")
//     private String keySecret;
//     @Autowired
//     DonationService donationService;

//     // Rough India bounding box check
//     private boolean coordsInIndia(Double lat, Double lon) {
//         if (lat == null || lon == null) return false;
//         return lat >= 6.5 && lat <= 35.5 && lon >= 68.0 && lon <= 97.5;
//     }

// // 
// @PostMapping("/create-order")
// public ResponseEntity<?> createOrder(@RequestBody Donourentity donor) {
//     try {


//         // ✅ Create Razorpay Order
//         RazorpayClient client = new RazorpayClient(keyId, keySecret);
//         JSONObject options = new JSONObject();
//         options.put("amount", donor.getAmount() * 100); // paise
//         options.put("currency", "INR");
//         options.put("receipt", "receipt_" + System.currentTimeMillis());
//         options.put("payment_capture", 1);

//         Order order = client.orders.create(options);

//         // ✅ Attach orderId + set pending
//         donor.setOrderId(order.get("id"));
//         donor.setStatus("PENDING");
//         donor.setDonationDate(LocalDateTime.now());

//         // ✅ Save donor details (with encryption inside your service)
//         donationService.saveDonation(donor);

//         // ✅ Send response to frontend
//         return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "id", order.get("id"),
//                 "amount", donor.getAmount() * 100,
//                 "currency", "INR",
//                 "keyId", keyId,
//                 "message", "Order created successfully"
//         ));
//     } catch (Exception e) {
//         e.printStackTrace();
//         return ResponseEntity.status(500)
//                 .body(Map.of("success", false, "message", "Server error: " + e.getMessage()));
//     }
// }

//     @Autowired
//     private DonationRepo donationRepo;

//     @PostMapping("/verify")
//     public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
//         try {
//             System.out.println("🟡 Received /verify payload: " + body);

//             // Extract fields
//             String razorpayOrderId = (String) body.get("razorpay_order_id");
//             String razorpayPaymentId = (String) body.get("razorpay_payment_id");
//             String razorpaySignature = (String) body.get("razorpay_signature");

//             // Basic validation
//             if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
//                 System.err.println("❌ Missing one or more fields in verify payload");
//                 return ResponseEntity.badRequest().body(Map.of(
//                         "success", false,
//                         "message", "Missing one or more required fields"
//                 ));
//             }

//             // ✅ Verify Signature
//             String payload = razorpayOrderId + "|" + razorpayPaymentId;
//             String generatedSignature = hmacSha256(payload, keySecret);

//             System.out.println("🧩 Signature payload: " + payload);
//             System.out.println("🧩 Generated: " + generatedSignature);
//             System.out.println("🧩 Received: " + razorpaySignature);

//             if (!generatedSignature.equals(razorpaySignature)) {
//                 System.err.println("❌ Invalid signature received");
//                 return ResponseEntity.badRequest().body(Map.of(
//                         "success", false,
//                         "message", "Invalid signature"
//                 ));
//             }

//             // ✅ Fetch Razorpay Payment Details
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);
//             com.razorpay.Payment payment = client.payments.fetch(razorpayPaymentId);
//             JSONObject paymentJson = payment.toJson();

//             String status = paymentJson.getString("status"); // captured, failed, refunded
//             String method = paymentJson.optString("method", "UNKNOWN");
//             String bank = paymentJson.optString("bank", "");
//             String vpa = paymentJson.optString("vpa", "");
//             int amount = paymentJson.getInt("amount"); // in paise
//             String currency = paymentJson.getString("currency");

//             // ✅ Update Donor record
//             Optional<Donourentity> donorOpt = donationRepo.findByOrderId(razorpayOrderId);
//             if (donorOpt.isPresent()) {
//                 Donourentity donor = donorOpt.get();
//                 donor.setPaymentId(razorpayPaymentId);
//                 donor.setSignature(razorpaySignature);
//                 donor.setStatus(status.equalsIgnoreCase("captured") ? "SUCCESS" : status.toUpperCase());
//                 donor.setPaymentMethod(method);
//                 donor.setBankName(bank.isEmpty() ? vpa : bank);
//                 donor.setAmount(amount / 100.0); // Convert paise to rupees
//                 donor.setDonationDate(LocalDateTime.now());

//                 donationService.saveDonation(donor);
//                 System.out.println("✅ Donor updated successfully");
//             } else {
//                 System.err.println("⚠️ Donor not found for orderId: " + razorpayOrderId);
//             }

//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "status", status,
//                     "method", method,
//                     "amount", amount / 100.0,
//                     "currency", currency
//             ));

//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of(
//                     "success", false,
//                     "message", "Server error: " + e.getMessage()
//             ));
//         }
//     }

//     private String hmacSha256(String data, String secret) throws Exception {
//         Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//         SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
//         sha256_HMAC.init(secret_key);
//         byte[] hash = sha256_HMAC.doFinal(data.getBytes());

//         // Convert to HEX string (Razorpay uses hex encoding)
//         StringBuilder hexString = new StringBuilder();
//         for (byte b : hash) {
//             String hex = Integer.toHexString(0xff & b);
//             if (hex.length() == 1) hexString.append('0');
//             hexString.append(hex);
//         }
//         return hexString.toString();
//     }
// }
  

package com.komal.template_backend.controller;

import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import com.komal.template_backend.service.DonationService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Subscription;
import com.razorpay.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class RazorpayController {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;
    @Value("${razorpay.variable_plan_id}")
    private String variablePlanId;
    @Value("${razorpay.webhook_secret}")
    private String webhookSecret ;
    @Autowired
    DonationService donationService;


@PostMapping("/create-order")
public ResponseEntity<?> createOrder(@RequestBody Donourentity donor) {
    try {


        // ✅ Create Razorpay Order
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject options = new JSONObject();
        options.put("amount", donor.getAmount() * 100); // paise
        options.put("currency", "INR");
        options.put("receipt", "receipt_" + System.currentTimeMillis());
        options.put("payment_capture", 1);

        Order order = client.orders.create(options);

        // ✅ Attach orderId + set pending
        donor.setOrderId(order.get("id"));
        donor.setStatus("PENDING");
        donor.setDonationDate(LocalDateTime.now());

        // ✅ Save donor details (with encryption inside your service)
        donationService.saveDonation(donor);

        // ✅ Send response to frontend
        return ResponseEntity.ok(Map.of(
                "success", true,
                "id", order.get("id"),
                "amount", donor.getAmount() * 100,
                "currency", "INR",
                "keyId", keyId,
                "message", "Order created successfully"
        ));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Server error: " + e.getMessage()));
    }
}
    @Autowired
    private DonationRepo donationRepo;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
        try {
            System.out.println("🟡 Received /verify payload: " + body);

            // Extract fields
            String razorpayOrderId = (String) body.get("razorpay_order_id");
            String razorpayPaymentId = (String) body.get("razorpay_payment_id");
            String razorpaySignature = (String) body.get("razorpay_signature");

            // Basic validation
            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                System.err.println("❌ Missing one or more fields in verify payload");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing one or more required fields"
                ));
            }

            // ✅ Verify Signature
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = hmacSha256(payload, keySecret);

            System.out.println("🧩 Signature payload: " + payload);
            System.out.println("🧩 Generated: " + generatedSignature);
            System.out.println("🧩 Received: " + razorpaySignature);

            if (!generatedSignature.equals(razorpaySignature)) {
                System.err.println("❌ Invalid signature received");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid signature"
                ));
            }

            // ✅ Fetch Razorpay Payment Details
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            com.razorpay.Payment payment = client.payments.fetch(razorpayPaymentId);
            JSONObject paymentJson = payment.toJson();

            String status = paymentJson.getString("status"); // captured, failed, refunded
            String method = paymentJson.optString("method", "UNKNOWN");
            String bank = paymentJson.optString("bank", "");
            String vpa = paymentJson.optString("vpa", "");
            int amount = paymentJson.getInt("amount"); // in paise
            String currency = paymentJson.getString("currency");

            // ✅ Update Donor record
            Optional<Donourentity> donorOpt = donationRepo.findByOrderId(razorpayOrderId);
            if (donorOpt.isPresent()) {
                Donourentity donor = donorOpt.get();
                donor.setPaymentId(razorpayPaymentId);
                donor.setSignature(razorpaySignature);
                donor.setStatus(status.equalsIgnoreCase("captured") ? "SUCCESS" : status.toUpperCase());
                donor.setPaymentMethod(method);
                donor.setBankName(bank.isEmpty() ? vpa : bank);
                donor.setAmount(amount / 100.0); // Convert paise to rupees
                donor.setDonationDate(LocalDateTime.now());

                donationService.saveDonation(donor);
                System.out.println("✅ Donor updated successfully");
            } else {
                System.err.println("⚠️ Donor not found for orderId: " + razorpayOrderId);
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", status,
                    "method", method,
                    "amount", amount / 100.0,
                    "currency", currency
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error: " + e.getMessage()
            ));
        }
    }
    private String hmacSha256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes());

        // Convert to HEX string (Razorpay uses hex encoding)
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    @PostMapping("/create-donor-record")
    public ResponseEntity<?> createDonorRecord(@RequestBody Donourentity donor) {
        try {
            donor.setStatus("PENDING");
            donor.setDonationDate(LocalDateTime.now());

            Donourentity saved = donationService.saveDonation(donor);

            return ResponseEntity.ok(Map.of("success", true, "donorId", saved.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }

    }
    @PostMapping("/create-subscription")
    public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {
        try {
            String donorId = (String) req.get("donorId");
            int amountRupees = Integer.parseInt(String.valueOf(req.get("amount"))); // must be integer rupees
System.out.println("🔍 DEBUG keyId = " + keyId);
System.out.println("🔍 DEBUG keySecret = " + keySecret);
System.out.println("🔍 DEBUG keyId length = " + (keyId != null ? keyId.length() : 0));
System.out.println("🔍 DEBUG keySecret length = " + (keySecret != null ? keySecret.length() : 0));
System.out.println("🔍 DEBUG variablePlanId = " + variablePlanId);

            System.out.println("🔵 create-subscription called: donorId=" + donorId + " amount=" + amountRupees);
            System.out.println("🔵 Using plan_id=" + variablePlanId + " key=" + keyId);

            Donourentity donor = donationRepo.findById(donorId)
                    .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // Build addon item (paise)
            JSONObject item = new JSONObject();
            item.put("amount", amountRupees * 100);
            item.put("currency", "INR");
            item.put("name", "Monthly Donation");

            JSONObject addon = new JSONObject();
            addon.put("item", item);

            JSONArray addons = new JSONArray();
            addons.put(addon);

            JSONObject options = new JSONObject();
            options.put("plan_id", variablePlanId);
            options.put("total_count", 12);
            options.put("quantity", 1);
            options.put("addons", addons);

            JSONObject notes = new JSONObject();
            notes.put("donorId", donorId);
            options.put("notes", notes);

            System.out.println("🔵 subscription request options: " + options.toString());

            Subscription sub = client.subscriptions.create(options);

            donor.setSubscriptionId(sub.get("id"));
            donor.setSubscriptionStatus("CREATED");
            donationRepo.save(donor);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "subscription_id", sub.get("id"),
                    "keyId", keyId,
                    "short_url", sub.toJson().optString("short_url")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/razorpay-webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload, @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            System.out.println("🔔 webhook payload: " + payload);
            if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                System.out.println("❌ invalid webhook signature");
                return ResponseEntity.status(400).body("Invalid signature");
            }
            JSONObject json = new JSONObject(payload);
            // handle events as already implemented (subscription.activated, subscription.charged, mandate.authorized)
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Webhook error");
        }
    }


}

