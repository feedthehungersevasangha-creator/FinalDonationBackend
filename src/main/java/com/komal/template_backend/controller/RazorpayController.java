

// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import com.komal.template_backend.service.DonationService;
// import com.komal.template_backend.service.MailService;
// import com.komal.template_backend.service.pdfReceiptServiceee;
// import com.razorpay.Order;
// import com.razorpay.RazorpayClient;
// import com.razorpay.Subscription;
// import com.razorpay.Utils;
// import org.json.JSONArray;
// import org.json.JSONObject;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.Map;
// import java.util.Optional;
// import com.razorpay.Plan;

// @RestController
// @RequestMapping("/api/payment")
// public class RazorpayController {
//     @Value("${razorpay.key_id}")
//     private String keyId;
//     @Value("${razorpay.key_secret}")
//     private String keySecret;
//     // @Value("${razorpay.variable_plan_id}")
//     // private String variablePlanId;
//     @Value("${razorpay.webhook_secret}")
//     private String webhookSecret ;
//     @Value("${razorpay.subscription_years:40}")
//     private int subscriptionYears;

//     @Autowired
//     DonationService donationService;

//     @Autowired
//     private pdfReceiptServiceee pdfReceiptServicee;
//     @Autowired
//     private MailService mailService;

//     @PostMapping("/create-order")
//     public ResponseEntity<?> createOrder(@RequestBody Donourentity donor) {
//         try {
//             // ✅ Create Razorpay Order
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);
//             JSONObject options = new JSONObject();
//             options.put("amount", donor.getAmount() * 100); // paise
//             options.put("currency", "INR");
//             options.put("receipt", "receipt_" + System.currentTimeMillis());
//             options.put("payment_capture", 1);
//             Order order = client.orders.create(options);

//             // ✅ Attach orderId + set pending
//             donor.setOrderId(order.get("id"));
//             donor.setStatus("PENDING");
//             donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//             // ✅ Save donor details (with encryption inside your service)
//             Donourentity saved = donationService.saveDonation(donor);

//             // IMPORTANT: do NOT send receipt here — paymentId will be null at this stage.
//             // Email will be sent after verification in /verify so receipt contains correct paymentId.

//             // ✅ Send response to frontend (includes donorId so frontend can use it)
//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "id", order.get("id"),
//                     "donorId", saved.getId(),
//                     "amount", donor.getAmount() * 100,
//                     "currency", "INR",
//                     "keyId", keyId,
//                     "message", "Order created successfully"
//             ));
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500)
//                     .body(Map.of("success", false, "message", "Server error: " + e.getMessage()));
//         }
//     }

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

//             // ✅ Fetch Razorpay Payment Details (for debugging and authoritative data)
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);
//             com.razorpay.Payment payment = client.payments.fetch(razorpayPaymentId);
//             JSONObject paymentJson = payment.toJson();

//             // --- Debug output: full payment JSON from Razorpay
//             System.out.println("🔍 Razorpay payment JSON: " + paymentJson.toString(2));

//             String status = paymentJson.optString("status", "UNKNOWN"); // captured, failed, refunded
//             String method = paymentJson.optString("method", "UNKNOWN");
//             String bank = paymentJson.optString("bank", "");
//             String vpa = paymentJson.optString("vpa", "");
//             int amount = paymentJson.optInt("amount", 0); // in paise
//             String currency = paymentJson.optString("currency", "");
//             String payerEmail = paymentJson.optString("email", "");
//             String payerContact = paymentJson.optString("contact", "");
//             String wallet = paymentJson.optString("wallet", "");

//             // human-friendly paymentInfo: prefer vpa then bank then method
//             String paymentInfo = "";
//             if (!vpa.isEmpty()) paymentInfo = vpa;
//             else if (!bank.isEmpty()) paymentInfo = bank;
//             else paymentInfo = method;

//             // ✅ Update Donor record
//             Optional<Donourentity> donorOpt = donationRepo.findByOrderId(razorpayOrderId);
//             if (donorOpt.isPresent()) {
//                 Donourentity donor = donorOpt.get();

//                 // --- Debug: donor before update (show limited info to avoid logging sensitive fields)
//                 System.out.println("🔍 Donor before update (id=" + donor.getId() + "): paymentId="
//                         + donor.getPaymentId() + ", subscriptionId=" + donor.getSubscriptionId()
//                         + ", orderId=" + donor.getOrderId());

//                 donor.setPaymentId(razorpayPaymentId);
//                 donor.setSignature(razorpaySignature);
//                 donor.setStatus(status.equalsIgnoreCase("captured") ? "SUCCESS" : status.toUpperCase());

//                 donor.setPaymentMethod(method);     // store RAW payment method
//                 donor.setUpiId(vpa);                // store UPI ID
//                 donor.setWallet(wallet);            // store wallet (if exists)
//                 donor.setPaymentInfo(bank.isEmpty() ? vpa : bank);

//                 donor.setPayerEmail(payerEmail);
//                 donor.setPayerContact(payerContact);

//                 donor.setAmount(amount / 100.0);
//                 donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//                 // Save the updated donor (donationService.saveDonation handles existing-case re-encryption)
//                 Donourentity saved = donationService.saveDonation(donor);

//                 // --- Debug: donor after update
//                 System.out.println("🔍 Donor after update (id=" + saved.getId() + "): paymentId="
//                         + saved.getPaymentId() + ", subscriptionId=" + saved.getSubscriptionId()
//                         + ", status=" + saved.getStatus() + ", amount=" + saved.getAmount());

//                 // ⭐ Send email receipt now that payment is verified and paymentId is present
//                 try {
//                     Donourentity decrypted = donationService.findByIdDecrypt(saved.getId());
//                     if (decrypted != null) {
//                         // Choose correct receipt type and generate appropriate PDF
//                         byte[] pdf;
//                         if ("onetime".equalsIgnoreCase(decrypted.getFrequency())) {
//                             pdf = pdfReceiptServicee.generateOneTimeDonationReceipt(
//                                     decrypted,
//                                     decrypted.getPaymentId(),
//                                     decrypted.getAmount()
//                             );
//                         } else if (decrypted.getSubscriptionId() != null && decrypted.getPaymentId() == null) {
//                             // Mandate confirmation (subscription created but no payment yet)
//                             pdf = pdfReceiptServicee.generateMandateConfirmation(decrypted);
//                         } else {
//                             // Monthly debit / subscription payment
//                             pdf = pdfReceiptServicee.generateMonthlyDebitReceipt(
//                                     decrypted,
//                                     decrypted.getPaymentId(),
//                                     decrypted.getAmount()
//                             );
//                         }

//                         // Email only if we have recipient email
//                         String recipient = (decrypted.getPayerEmail() != null && !decrypted.getPayerEmail().isBlank())
//                                 ? decrypted.getPayerEmail() : decrypted.getEmail();

//                         if (recipient != null && !recipient.isBlank()) {
//                             mailService.sendDonationReceiptWithAttachment(
//                                     recipient,
//                                     decrypted.getFirstName() + " " + decrypted.getLastName(),
//                                     decrypted.getAmount(),
//                                     decrypted.getPaymentId(),
//                                     pdf,
//                                     "DonationReceipt_" + (decrypted.getPaymentId() != null ? decrypted.getPaymentId() : decrypted.getId()) + ".pdf"
//                             );
//                             System.out.println("📧 Email receipt queued/sent to " + recipient);
//                         } else {
//                             System.err.println("⚠️ No recipient email available; skipping email send.");
//                         }
//                     } else {
//                         System.err.println("⚠️ Decrypted donor returned null; skipping email send.");
//                     }
//                 } catch (Exception e) {
//                     System.err.println("⚠️ Failed to generate/send PDF email after verification: " + e.getMessage());
//                     e.printStackTrace();
//                 }

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
// // ===========================================================
//     // 1️⃣ CREATE DONOR RECORD FOR SUBSCRIPTION
//     // ===========================================================
//     @PostMapping("/create-donor-record")
//     public ResponseEntity<?> createDonor(@RequestBody Donourentity donor) {

//         System.out.println("\n\n====================== 📌 CREATE DONOR RECORD ======================");
//         System.out.println("👉 Input Donor: " + donor);

//         try {
//             donor.setStatus("PENDING");
//             donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//             Donourentity saved = donationService.saveDonation(donor);

//             System.out.println("🟢 Donor record created with ID: " + saved.getId());
//             return ResponseEntity.ok(Map.of("success", true, "donorId", saved.getId()));

//         } catch (Exception e) {
//             System.err.println("❌ Error creating donor: " + e.getMessage());
//             return ResponseEntity.status(500).body(Map.of("success", false));
//         }
//     }


//     // ===========================================================
//     // 2️⃣ CREATE SUBSCRIPTION (MANDATE)
//     // ===========================================================
//     // @PostMapping("/create-subscription")
//     // public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {

//     //     System.out.println("\n\n====================== 🔵 CREATE SUBSCRIPTION START ======================");
//     //     System.out.println("👉 Received: " + req);

//     //     try {

//     //         int donorId = (String) req.get("donorId");
//     //         int amount = (Integer) req.get("amount"); // rupees
//     //         int authAmount = 1; // ₹1 mandate auth
//     //         int smallDebit = req.get("starterAmount") == null ? 10 : (Integer) req.get("starterAmount");

//     //         System.out.println("🔵 DonorId=" + donorId + ", MonthlyAmount=" + amount + ", FirstDebit=" + smallDebit);

//     //         Donourentity donor = donationRepo.findById(donorId)
//     //                 .orElseThrow(() -> new RuntimeException("Donor not found"));

//     //         RazorpayClient client = new RazorpayClient(keyId, keySecret);

//     //         // ---------- CREATE PLAN ----------
//     //         JSONObject planJson = new JSONObject();
//     //         planJson.put("period", "monthly");
//     //         planJson.put("interval", 1);

//     //         JSONObject item = new JSONObject();
//     //         item.put("name", "Monthly Donation");
//     //         item.put("amount", amount * 100);
//     //         item.put("currency", "INR");

//     //         planJson.put("item", item);

//     //         System.out.println("📦 Plan Payload: " + planJson.toString(2));
//     //         Plan plan = client.plans.create(planJson);
//     //         System.out.println("🟢 Plan Created: " + plan.get("id"));


//     //         // ---------- CREATE SUBSCRIPTION ----------
//     //         JSONObject subJson = new JSONObject();
//     //         subJson.put("plan_id", plan.get("id"));
//     //         subJson.put("customer_notify", 1);
//     //         subJson.put("total_count", 120);

//     //         JSONArray addons = new JSONArray();

//     //         // ₹1 MANDATE AUTH
//     //         JSONObject addon1 = new JSONObject();
//     //         JSONObject addon1Item = new JSONObject();
//     //         addon1Item.put("name", "Auth ₹1");
//     //         addon1Item.put("amount", authAmount * 100);
//     //         addon1Item.put("currency", "INR");
//     //         addon1.put("item", addon1Item);
//     //         addons.put(addon1);

//     //         // FIRST SMALL DEBIT
//     //         JSONObject addon2 = new JSONObject();
//     //         JSONObject addon2Item = new JSONObject();
//     //         addon2Item.put("name", "First Debit");
//     //         addon2Item.put("amount", smallDebit * 100);
//     //         addon2Item.put("currency", "INR");
//     //         addon2.put("item", addon2Item);
//     //         addons.put(addon2);

//     //         subJson.put("addons", addons);

//     //         System.out.println("📦 Subscription Payload: " + subJson.toString(2));
//     //         Subscription subscription = client.subscriptions.create(subJson);

//     //         System.out.println("🟢 Subscription Created: " + subscription.get("id"));

//     //         donor.setSubscriptionId(subscription.get("id"));
//     //         donor.setSubscriptionStatus("CREATED");
//     //         donationRepo.save(donor);

//     //         System.out.println("====================== 🟢 CREATE SUBSCRIPTION COMPLETE ======================\n");
//     //         return ResponseEntity.ok(Map.of(
//     //                 "success", true,
//     //                 "subscription_id", subscription.get("id"),
//     //                 "keyId", keyId
//     //         ));

//     //     } catch (Exception e) {
//     //         System.err.println("❌ SUBSCRIPTION ERROR: " + e.getMessage());
//     //         e.printStackTrace();
//     //         return ResponseEntity.status(500).body(Map.of("success", false));
//     //     }
//     // }
//   private int parseIntSafe(Object o, int fallback) {
//     try {
//         if (o == null) return fallback;
//         if (o instanceof Number) return ((Number) o).intValue();
//         String s = String.valueOf(o).trim();
//         if (s.isEmpty()) return fallback;
//         return Integer.parseInt(s);
//     } catch (Exception ex) {
//         return fallback;
//     }
// }
// @PostMapping("/create-subscription")
// public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {
//     System.out.println("\n\n====================== 🔵 CREATE SUBSCRIPTION START ======================");
//     System.out.println("👉 Received: " + req);

//     try {
//         // ----- robust parsing helpers -----
//         String donorId = String.valueOf(req.get("donorId"));
//         int amount = parseIntSafe(req.get("amount"), 0); // rupees
//         if (amount <= 0) {
//             return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid amount"));
//         }

//         int authAmount = 1; // ₹1 mandate auth
//         int starterAmount = parseIntSafe(req.get("starterAmount"), 10);

//         // Sanitize starterAmount (prefer 1..28 for monthly start day)
//         if (starterAmount < 1 || starterAmount > 28) {
//             System.out.println("⚠️ invalid starterAmount " + starterAmount + " -> using 10");
//             starterAmount = 10;
//         }

//         System.out.println("🔵 donorId=" + donorId + ", MonthlyAmount=" + amount + ", FirstDebit=" + starterAmount);

//         Donourentity donor = donationRepo.findById(donorId)
//                 .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

//         RazorpayClient client = new RazorpayClient(keyId, keySecret);

//         // ---------------- CREATE PLAN ----------------
//         JSONObject planJson = new JSONObject();
//         planJson.put("period", "monthly");
//         planJson.put("interval", 1);

//         JSONObject item = new JSONObject();
//         item.put("name", "Monthly Donation");
//         item.put("amount", amount * 100);   // paise
//         item.put("currency", "INR");

//         planJson.put("item", item);

//         System.out.println("📦 Plan Payload: " + planJson.toString(2));
//         Plan plan = client.plans.create(planJson);
//         System.out.println("🟢 Plan Created: " + plan.get("id"));

//         donor.setPlanId(plan.get("id"));
//         donationRepo.save(donor);

//         // ---------------- CREATE SUBSCRIPTION ----------------
//         JSONObject subJson = new JSONObject();
//         subJson.put("plan_id", plan.get("id"));
//         subJson.put("customer_notify", 1);
//         subJson.put("total_count", subscriptionYears * 12);

//         JSONArray addons = new JSONArray();

//         // ₹1 auth
//         JSONObject addon1 = new JSONObject();
//         JSONObject addon1Item = new JSONObject();
//         addon1Item.put("name", "Auth ₹1");
//         addon1Item.put("amount", authAmount * 100);
//         addon1Item.put("currency", "INR");
//         addon1.put("item", addon1Item);
//         addons.put(addon1);

//         // First debit
//         JSONObject addon2 = new JSONObject();
//         JSONObject addon2Item = new JSONObject();
//         addon2Item.put("name", "First Debit");
//         addon2Item.put("amount", starterAmount * 100);
//         addon2Item.put("currency", "INR");
//         addon2.put("item", addon2Item);
//         addons.put(addon2);

//         // avoid ambiguous JSONObject.put overload
//         subJson.put("addons", (Object) addons);

//         // set optional start_at if donor has requested a day
//         if (donor.getStartDay() != null) {
//             long startAt = getNextStartDate(donor.getStartDay());
//             subJson.put("start_at", startAt);
//             System.out.println("➡ Subscription start_at set to epoch: " + startAt);
//         }

//         JSONObject notes = new JSONObject();
//         notes.put("donorId", donorId);
//         notes.put("monthlyAmount", String.valueOf(amount));
//         subJson.put("notes", notes);

//         System.out.println("📦 Subscription Payload: " + subJson.toString(2));

//         Subscription subscription = client.subscriptions.create(subJson);
//         System.out.println("🟢 Subscription Created: " + subscription.get("id"));

//         donor.setSubscriptionId(subscription.get("id"));
//         donor.setSubscriptionStatus("CREATED");
//         donor.setMonthlyAmount((double) amount);
//         donor.setStartDay(starterAmount);
//         donationRepo.save(donor);

//         System.out.println("====================== 🟢 CREATE SUBSCRIPTION COMPLETE ======================\n");

//         return ResponseEntity.ok(Map.of(
//                 "success", true,
//                 "subscription_id", subscription.get("id"),
//                 "plan_id", plan.get("id"),
//                 "keyId", keyId
//         ));

//     } catch (Exception e) {
//         System.err.println("❌ SUBSCRIPTION ERROR: " + e.getMessage());
//         e.printStackTrace();
//         return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
//     }
// }



//     // ===========================================================
//     // 3️⃣ VERIFY SUBSCRIPTION CHECKOUT
//     // ===========================================================
//     @PostMapping("/verify-subscription")
//     public ResponseEntity<?> verifySubscription(@RequestBody Map<String, Object> req) {

//         System.out.println("\n\n====================== 🔐 VERIFY SUBSCRIPTION ======================");
//         System.out.println("👉 Received: " + req);

//         try {

//             String subId = (String) req.get("razorpay_subscription_id");
//             String payId = (String) req.get("razorpay_payment_id");
//             String sig = (String) req.get("razorpay_signature");

//             if (subId == null || payId == null || sig == null) {
//                 System.out.println("❌ Missing fields!");
//                 return ResponseEntity.badRequest().body("Missing");
//             }

//             String payload = subId + "|" + payId;
//             String generatedSig = hmacSha256(payload, keySecret);

//             System.out.println("🧩 Compare Signatures:");
//             System.out.println("Generated: " + generatedSig);
//             System.out.println("Received : " + sig);

//             if (!generatedSig.equals(sig)) {
//                 System.out.println("❌ Signature mismatch");
//                 return ResponseEntity.badRequest().body("Invalid");
//             }

//             Donourentity donor =
//                     donationRepo.findBySubscriptionId(subId).orElse(null);

//             if (donor != null) {
//                 donor.setPaymentId(payId);
//                 donor.setSubscriptionStatus("ACTIVE");
//                 donationRepo.save(donor);
//             }

//             System.out.println("🟢 Subscription Verified Successfully");
//             return ResponseEntity.ok(Map.of("success", true));

//         } catch (Exception e) {
//             System.err.println("❌ verify-subscription ERROR: " + e);
//             return ResponseEntity.status(500).body("error");
//         }
//     }



//     // ===========================================================
//     // 4️⃣ WEBHOOK — mandate authorized + subscription activated + charged
//     // ===========================================================
//     @PostMapping("/razorpay-webhook")
//     public ResponseEntity<?> webhook(@RequestBody String payload,
//                                      @RequestHeader("X-Razorpay-Signature") String signature) {

//         System.out.println("\n\n====================== 🔔 WEBHOOK RECEIVED ======================");
//         System.out.println("👉 Payload:\n" + payload);
//         System.out.println("👉 Signature = " + signature);

//         try {

//             boolean valid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
//             System.out.println("🔐 Signature valid? " + valid);

//             if (!valid) {
//                 return ResponseEntity.status(400).body("Invalid signature");
//             }

//             JSONObject json = new JSONObject(payload);
//             String event = json.optString("event");

//             System.out.println("📣 EVENT: " + event);

//             // -------------------------------------------------------
//             // 1️⃣ MANDATE AUTHORIZED
//             // -------------------------------------------------------
//             if (event.equals("mandate.authorized")) {

//                 JSONObject m = json.getJSONObject("payload")
//                         .getJSONObject("mandate")
//                         .getJSONObject("entity");

//                 String subscriptionId = m.getString("subscription_id");
//                 String mandateId = m.getString("id");

//                 Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);

//                 if (donor != null) {
//                     donor.setRazorpayMandateId(mandateId);
//                     donor.setMandateStatus("AUTHORIZED");
//                     donationRepo.save(donor);

//                     System.out.println("🟢 Mandate Authorized Saved for donor: " + donor.getId());
//                 }
//             }


//             // -------------------------------------------------------
//             // 2️⃣ SUBSCRIPTION ACTIVATED
//             // -------------------------------------------------------
//             if (event.equals("subscription.activated")) {

//                 JSONObject sub = json.getJSONObject("payload")
//                         .getJSONObject("subscription")
//                         .getJSONObject("entity");

//                 String subscriptionId = sub.getString("id");

//                 Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);

//                 if (donor != null) {
//                     donor.setSubscriptionStatus("ACTIVE");
//                     donationRepo.save(donor);

//                     System.out.println("🟢 Subscription Activated Saved for donor: " + donor.getId());
//                 }
//             }


//             // -------------------------------------------------------
//             // 3️⃣ SUBSCRIPTION CHARGED (MONTHLY PAYMENT)
//             // -------------------------------------------------------
//             if (event.equals("subscription.charged")) {

//                 JSONObject p = json.getJSONObject("payload")
//                         .getJSONObject("payment")
//                         .getJSONObject("entity");

//                 String subscriptionId = p.getString("subscription_id");
//                 String paymentId = p.getString("id");
//                 double amountPaid = p.getInt("amount") / 100.0;

//                 Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);

//                 if (donor != null) {

//                     // Save as new monthly entry - SAME STYLE as your one-time record
//                     Donourentity monthly = new Donourentity();

//                     monthly.setFirstName(donor.getFirstName());
//                     monthly.setLastName(donor.getLastName());
//                     monthly.setEmail(donor.getEmail());
//                     monthly.setPayerEmail(donor.getPayerEmail());
//                     monthly.setPayerContact(donor.getPayerContact());

//                     monthly.setAmount(amountPaid);
//                     monthly.setSubscriptionId(subscriptionId);
//                     monthly.setPaymentId(paymentId);
//                     monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//                     monthly.setStatus("SUCCESS");

//                     donationService.saveDonation(monthly);

//                     System.out.println("🟢 MONTHLY PAYMENT Saved: donorRef=" + donor.getId());
//                 }
//             }


//             return ResponseEntity.ok("OK");

//         } catch (Exception e) {
//             System.err.println("❌ WEBHOOK ERROR: " + e);
//             return ResponseEntity.status(500).body("error");
//         }
//     }


    
 

// }



// --------------------------------------------------------------------------------------
// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import com.komal.template_backend.service.DonationService;
// import com.komal.template_backend.service.MailService;
// // import com.komal.template_backend.service.pdfReceiptServiceee;
// import com.komal.template_backend.service.PdfReceiptServic;
// import com.razorpay.Order;
// import com.razorpay.Plan;
// import com.razorpay.RazorpayClient;
// import com.razorpay.Subscription;
// import com.razorpay.Utils;
// import org.json.JSONObject;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.*;

// @RestController
// @RequestMapping("/api/payment")
// public class RazorpayController {

//     @Value("${razorpay.key_id}")
//     private String keyId;

//     @Value("${razorpay.key_secret}")
//     private String keySecret;

//     @Value("${razorpay.webhook_secret}")
//     private String webhookSecret;

//     @Value("${razorpay.subscription_years}")
//     private int subscriptionYears;

//     @Autowired
//     private DonationService donationService;

//     // @Autowired
//     // private pdfReceiptServiceee pdfReceiptServicee;
//     @Autowired
// private PdfReceiptServic pdfReceiptService;
// ;


//     @Autowired
//     private MailService mailService;

//     @Autowired
//     private DonationRepo donationRepo;

//     // --------------------------------------------------------------------
//     // ONE-TIME ORDER CREATION
//     // --------------------------------------------------------------------

//     @PostMapping("/create-order")
//     public ResponseEntity<?> createOrder(@RequestBody Donourentity donor) {
//         System.out.println("========= CREATE ONE-TIME ORDER =========");
//         try {
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);

//             JSONObject options = new JSONObject();
//             options.put("amount", donor.getAmount() * 100);
//             options.put("currency", "INR");
//             options.put("receipt", "receipt_" + System.currentTimeMillis());
//             options.put("payment_capture", 1);

//             System.out.println("Order options: " + options);
//             Order order = client.orders.create(options);
//             System.out.println("Created order: " + order);

//             donor.setOrderId(order.get("id"));
//             donor.setStatus("PENDING");
//             donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//             Donourentity saved = donationService.saveDonation(donor);

//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "id", order.get("id"),
//                     "donorId", saved.getId(),
//                     "amount", donor.getAmount() * 100,
//                     "currency", "INR",
//                     "keyId", keyId
//             ));
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500)
//                     .body(Map.of("success", false, "message", e.getMessage()));
//         }
//     }

//     // --------------------------------------------------------------------
//     // ONE-TIME PAYMENT VERIFY
//     // --------------------------------------------------------------------

//     @PostMapping("/verify")
//     public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
//         System.out.println("========= VERIFY ONE-TIME PAYMENT =========");
//         System.out.println("RAW BODY: " + body);

//         try {
//             String razorpayOrderId = (String) body.get("razorpay_order_id");
//             String razorpayPaymentId = (String) body.get("razorpay_payment_id");
//             String razorpaySignature = (String) body.get("razorpay_signature");

//             System.out.println("➡ order_id   = " + razorpayOrderId);
//             System.out.println("➡ payment_id = " + razorpayPaymentId);
//             System.out.println("➡ signature  = " + razorpaySignature);

//             if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
//                 return ResponseEntity.badRequest().body(Map.of(
//                         "success", false,
//                         "message", "Missing required fields"
//                 ));
//             }

//             String payload = razorpayOrderId + "|" + razorpayPaymentId;
//             String generatedSignature = hmacSha256(payload, keySecret);

//             System.out.println("VERIFY SIGNATURE");
//             System.out.println("  payload           = " + payload);
//             System.out.println("  expectedSignature = " + generatedSignature);
//             System.out.println("  receivedSignature = " + razorpaySignature);

//             if (!generatedSignature.equals(razorpaySignature)) {
//                 System.out.println("❌ INVALID SIGNATURE (ONE-TIME)");
//                 return ResponseEntity.badRequest().body(Map.of(
//                         "success", false,
//                         "message", "Invalid signature"
//                 ));
//             }

//             RazorpayClient client = new RazorpayClient(keyId, keySecret);
//             com.razorpay.Payment payment = client.payments.fetch(razorpayPaymentId);
//             JSONObject paymentJson = payment.toJson();
//             System.out.println("Payment JSON: " + paymentJson);

//             String status = paymentJson.optString("status", "UNKNOWN");
//             int amount = paymentJson.optInt("amount", 0);
//             String payerEmail = paymentJson.optString("email", "");
//             String payerContact = paymentJson.optString("contact", "");
//             String method = paymentJson.optString("method", "");
//             String bank = paymentJson.optString("bank", "");
//             String vpa = paymentJson.optString("vpa", "");
//             String wallet = paymentJson.optString("wallet", "");

//             Optional<Donourentity> donorOpt = donationRepo.findByOrderId(razorpayOrderId);
//             if (donorOpt.isPresent()) {
//                 Donourentity donor = donorOpt.get();

//                 donor.setPaymentId(razorpayPaymentId);
//                 donor.setSignature(razorpaySignature);
//                 donor.setStatus(status.equalsIgnoreCase("captured") ? "SUCCESS" : status.toUpperCase());
//                 donor.setPaymentMethod(method);
//                 donor.setUpiId(vpa);
//                 donor.setWallet(wallet);
//                 donor.setPaymentInfo(bank.isEmpty() ? vpa : bank);
//                 donor.setPayerEmail(payerEmail);
//                 donor.setPayerContact(payerContact);
//                 donor.setAmount(amount / 100.0);
//                 donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//                 donationService.saveDonation(donor);

//                 // ----- Receipt + Email -----
//                 try {
//                     Donourentity decrypted = donationService.findByIdDecrypt(donor.getId());
//                     if (decrypted != null) {
//                         byte[] pdf = pdfReceiptService.generateOneTimeDonationReceipt(
//                                 decrypted,
//                                 decrypted.getPaymentId(),
//                                 decrypted.getAmount()
//                         );

//                         String recipient = (decrypted.getPayerEmail() != null &&
//                                 !decrypted.getPayerEmail().isBlank())
//                                 ? decrypted.getPayerEmail()
//                                 : decrypted.getEmail();

//                         if (recipient != null && !recipient.isBlank()) {
//                             mailService.sendDonationReceiptWithAttachment(
//                                     recipient,
//                                     decrypted.getFirstName() + " " + decrypted.getLastName(),
//                                     decrypted.getAmount(),
//                                     decrypted.getPaymentId(),
//                                     pdf,
//                                     "DonationReceipt_" + decrypted.getPaymentId() + ".pdf"
//                             );
//                         }
//                     }
//                 } catch (Exception e) {
//                     System.out.println("⚠ Error while sending one-time receipt");
//                     e.printStackTrace();
//                 }
//             } else {
//                 System.out.println("⚠ No donor found for orderId=" + razorpayOrderId);
//             }

//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "status", status,
//                     "amount", amount / 100.0
//             ));
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
//         }
//     }

//     // --------------------------------------------------------------------
//     // HMAC UTILITY
//     // --------------------------------------------------------------------

//     private String hmacSha256(String data, String secret) throws Exception {
//         Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//         SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
//         sha256_HMAC.init(key);
//         byte[] hash = sha256_HMAC.doFinal(data.getBytes());

//         StringBuilder sb = new StringBuilder();
//         for (byte b : hash) {
//             String hex = Integer.toHexString(0xff & b);
//             if (hex.length() == 1) sb.append('0');
//             sb.append(hex);
//         }
//         return sb.toString();
//     }

//     private boolean verifyWebhook(String payload, String headerSignature) {
//         try {
//             String expected = hmacSha256(payload, webhookSecret);
//             return expected.equals(headerSignature);
//         } catch (Exception e) {
//             return false;
//         }
//     }

//     // --------------------------------------------------------------------
//     // CREATE DONOR (SUBSCRIPTION)
//     // --------------------------------------------------------------------

//     @PostMapping("/create-donor-record")
//     public ResponseEntity<?> createDonor(@RequestBody Donourentity donor) {
//         System.out.println("========= CREATE SUBSCRIPTION DONOR RECORD =========");
//         try {
//             donor.setStatus("PENDING");
//             donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//             Donourentity saved = donationService.saveDonation(donor);
//             return ResponseEntity.ok(Map.of("success", true, "donorId", saved.getId()));
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
//         }
//     }

//     private int parseIntSafe(Object o, int fallback) {
//         try {
//             if (o == null) return fallback;
//             if (o instanceof Number) return ((Number) o).intValue();
//             String s = String.valueOf(o).trim();
//             return s.isEmpty() ? fallback : Integer.parseInt(s);
//         } catch (Exception ex) {
//             return fallback;
//         }
//     }

//     private long getNextStartDate(int startDay) {
//         LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
//         LocalDateTime next;

//         int today = now.getDayOfMonth();
//         if (today < startDay) {
//             next = now.withDayOfMonth(startDay).withHour(0).withMinute(0).withSecond(0);
//         } else {
//             next = now.plusMonths(1).withDayOfMonth(startDay).withHour(0).withMinute(0).withSecond(0);
//         }

//         return next.atZone(ZoneId.of("Asia/Kolkata")).toEpochSecond();
//     }

//     // --------------------------------------------------------------------
//     // SUBSCRIPTION CREATE
//     // --------------------------------------------------------------------

//     @PostMapping("/create-subscription")
//     public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {
//         System.out.println("====================== CREATE SUBSCRIPTION START ======================");
//         System.out.println("Received: " + req);

//         try {
//             String donorId = String.valueOf(req.get("donorId"));
//             int amount = parseIntSafe(req.get("amount"), 0);

//             if (amount <= 0) {
//                 return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid amount"));
//             }

//             int starterAmount = parseIntSafe(req.get("starterAmount"), 10);
//             if (starterAmount < 1 || starterAmount > 28) {
//                 starterAmount = 10;
//             }

//             Donourentity donor = donationRepo.findById(donorId)
//                     .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

//             RazorpayClient client = new RazorpayClient(keyId, keySecret);

//             Map<String, Object> item = Map.of(
//                     "name", "Monthly Donation",
//                     "amount", amount * 100,   // paise
//                     "currency", "INR"
//             );

//             Map<String, Object> planRequest = Map.of(
//                     "period", "monthly",
//                     "interval", 1,
//                     "item", item
//             );

//             System.out.println("Plan request: " + new JSONObject(planRequest).toString(2));
//             Plan plan = client.plans.create(new JSONObject(planRequest));
//             System.out.println("Created plan: " + plan);

//             donor.setPlanId(plan.get("id"));
//             donationRepo.save(donor);

//             // ₹1 authorization addon
//             Map<String, Object> authAddon = Map.of(
//                     "item", Map.of(
//                             "name", "Mandate Authorization",
//                             "amount", 100,
//                             "currency", "INR"
//                     )
//             );

//             Map<String, Object> notes = Map.of(
//                     "donorId", donorId,
//                     "monthlyAmount", String.valueOf(amount)
//             );

//             Map<String, Object> subscriptionRequest = new HashMap<>();
//             subscriptionRequest.put("plan_id", plan.get("id"));
//             subscriptionRequest.put("customer_notify", 1);
//             System.out.println("Subscription years: " + subscriptionYears);
//             subscriptionRequest.put("total_count", (subscriptionYears * 12) - 1);
//             subscriptionRequest.put("addons", List.of(authAddon));
//             subscriptionRequest.put("notes", notes);

//             Integer useStartDay = donor.getStartDay() != null ? donor.getStartDay() : starterAmount;
//             if (useStartDay != null) {
//                 subscriptionRequest.put("start_at", getNextStartDate(useStartDay));
//             }

//             System.out.println("📦 Subscription Payload: " + new JSONObject(subscriptionRequest).toString(2));

//             Subscription subscription = client.subscriptions.create(new JSONObject(subscriptionRequest));
//             System.out.println("Created subscription: " + subscription);

//             donor.setSubscriptionId(subscription.get("id"));
//             donor.setSubscriptionStatus("CREATED");
//             donor.setMonthlyAmount((double) amount);
//             donor.setStartDay(useStartDay);
//             donationRepo.save(donor);

//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "subscription_id", subscription.get("id"),
//                     "plan_id", plan.get("id"),
//                     "keyId", keyId
//             ));

//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500)
//                     .body(Map.of("success", false, "message", e.getMessage()));
//         }
//     }

//     // --------------------------------------------------------------------
//     // SUBSCRIPTION VERIFY  (CARD / NETBANKING ONLY)
//     // --------------------------------------------------------------------

//     @PostMapping("/verify-subscription")
//     public ResponseEntity<?> verifySubscription(@RequestBody Map<String, Object> req) {
//         System.out.println("====================== VERIFY SUBSCRIPTION ======================");
//         System.out.println("RAW BODY: " + req);

//         try {
//             String subId = (String) req.get("razorpay_subscription_id");
//             String payId = (String) req.get("razorpay_payment_id");
//             String sig   = (String) req.get("razorpay_signature");

//             System.out.println("➡ subId = " + subId);
//             System.out.println("➡ payId = " + payId);
//             System.out.println("➡ sig   = " + sig);

//             // You said you only want card / netbanking → payment_id must be present
//             if (subId == null || subId.isBlank()
//                     || payId == null || payId.isBlank()
//                     || sig == null || sig.isBlank()) {

//                 System.out.println("❌ Missing required fields for subscription verification");
//                 return ResponseEntity.badRequest().body(
//                         Map.of("success", false, "message", "Missing required fields")
//                 );
//             }

//             // Razorpay expects: payment_id + "|" + subscription_id
//             String payload = payId + "|" + subId;
//             String expectedSignature = hmacSha256(payload, keySecret);

//             System.out.println("✅ VERIFY SUBSCRIPTION SIGNATURE");
//             System.out.println("   payload           = " + payload);
//             System.out.println("   expectedSignature = " + expectedSignature);
//             System.out.println("   receivedSignature = " + sig);

//             if (!expectedSignature.equals(sig)) {
//                 System.out.println("❌ INVALID SIGNATURE (SUBSCRIPTION)");
//                 return ResponseEntity.status(400).body(
//                         Map.of("success", false, "message", "Invalid signature")
//                 );
//             }

//             // Fetch subscription from Razorpay to get REAL status (authenticated/active/…)
//             RazorpayClient client = new RazorpayClient(keyId, keySecret);
//             Subscription sub = client.subscriptions.fetch(subId);
//             JSONObject subJson = sub.toJson();
//             System.out.println("Subscription JSON (verify): " + subJson);

//             String rzpStatus = subJson.optString("status", "unknown").toUpperCase();

//             Donourentity donor = donationRepo.findBySubscriptionId(subId).orElse(null);
//             if (donor != null) {
//                 donor.setPaymentId(payId);
//                 donor.setSubscriptionStatus(rzpStatus);

//                 // Final donation "status" should be SUCCESS when subscription is authenticated/active
//                 if ("AUTHENTICATED".equals(rzpStatus) || "ACTIVE".equals(rzpStatus)) {
//                     donor.setStatus("SUCCESS");
//                 } else {
//                     donor.setStatus(rzpStatus); // fallback
//                 }

//                 donationRepo.save(donor);
//                 System.out.println("✅ Updated donor for subscriptionId=" + subId + ", donorId=" + donor.getId());
//             } else {
//                 System.out.println("⚠ No donor found for subscriptionId=" + subId);
//             }

//             return ResponseEntity.ok(Map.of("success", true));

//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500).body(
//                     Map.of("success", false, "message", "Server error")
//             );
//         }
//     }

//     // --------------------------------------------------------------------
//     // WEBHOOK HANDLER
//     // --------------------------------------------------------------------


// @PostMapping("/razorpay-webhook")
// public ResponseEntity<?> webhook(@RequestBody String payload,
//                                  @RequestHeader("X-Razorpay-Signature") String signature) {

//     System.out.println("====================== WEBHOOK RECEIVED ======================");
//     System.out.println("Signature: " + signature);

//     try {
//         if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
//             System.out.println("❌ Invalid webhook signature");
//             return ResponseEntity.status(400).body("Invalid signature");
//         }

//         JSONObject json = new JSONObject(payload);
//         String event = json.optString("event");
//         System.out.println("📣 EVENT = " + event);

//         // ------------------------------------------------------------------
//         // 1️⃣ ONE-TIME PAYMENT SAFETY (payment.captured)
//         // ------------------------------------------------------------------
//         if ("payment.captured".equals(event)) {

//             JSONObject p = json.getJSONObject("payload")
//                     .getJSONObject("payment")
//                     .getJSONObject("entity");

//             String orderId = p.optString("order_id", null);
//             String paymentId = p.optString("id", null);

//             if (orderId != null) {
//                 donationRepo.findByOrderId(orderId).ifPresent(donor -> {
//                     donor.setPaymentId(paymentId);
//                     donor.setStatus("SUCCESS");
//                     donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//                     donationRepo.save(donor);
//                     System.out.println("✅ payment.captured → updated donor");
//                 });
//             }
//         }

//         // ------------------------------------------------------------------
//         // 2️⃣ MANDATE AUTHORIZED
//         // ------------------------------------------------------------------
//         if ("mandate.authorized".equals(event)) {

//             JSONObject m = json.getJSONObject("payload")
//                     .getJSONObject("mandate")
//                     .getJSONObject("entity");

//             String subscriptionId = m.getString("subscription_id");
//             String mandateId = m.getString("id");

//             donationRepo.findBySubscriptionId(subscriptionId).ifPresent(donor -> {
//                 donor.setRazorpayMandateId(mandateId);
//                 donor.setMandateStatus("AUTHORIZED");
//                 donor.setSubscriptionStatus("AUTHENTICATED");
//                 donor.setStatus("SUCCESS");   // ⭐ IMPORTANT
//                 donationRepo.save(donor);
//                 System.out.println("✅ mandate.authorized → SUCCESS");
//                        // ✅ SEND CONFIRMATION RECEIPT ONCE
//         try {
//             Donourentity decrypted = donationService.findByIdDecrypt(donor.getId());

//             byte[] pdf = pdfReceiptService.generateMandateConfirmation(decrypted);

//             mailService.sendDonationReceiptWithAttachment(
//                     decrypted.getEmail(),
//                     decrypted.getFirstName() + " " + decrypted.getLastName(),
//                     decrypted.getMonthlyAmount(),
//                     mandateId,
//                     pdf,
//                     "Mandate_Confirmation_" + mandateId + ".pdf"
//             );

//             System.out.println("📧 Mandate confirmation mail sent");

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//             });
//         }

//         // ------------------------------------------------------------------
//         // 3️⃣ ALL SUBSCRIPTION STATUS EVENTS
//         // ------------------------------------------------------------------
//         if ("subscription.charged".equals(event)) {

//     JSONObject p = json.getJSONObject("payload")
//             .getJSONObject("payment")
//             .getJSONObject("entity");

//     String subscriptionId = p.getString("subscription_id");
//     String paymentId = p.getString("id");
//     double amountPaid = p.getInt("amount") / 100.0;

//     donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

//         Donourentity monthly = new Donourentity();

//         monthly.setFirstName(parent.getFirstName());
//         monthly.setLastName(parent.getLastName());
//         monthly.setEmail(parent.getEmail());
//         monthly.setMobile(parent.getMobile());

//         monthly.setSubscriptionId(subscriptionId);
//         monthly.setPaymentId(paymentId);
//         monthly.setAmount(amountPaid);
//         monthly.setStatus("SUCCESS");
//         monthly.setSubscriptionStatus("CHARGED");
//         monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//         donationRepo.save(monthly);

//         System.out.println("✅ Monthly debit saved (new row)");

//         try {
//             Donourentity decrypted = donationService.findByIdDecrypt(monthly.getId());

//             byte[] pdf = pdfReceiptService.generateMonthlyDebitReceipt(
//                     decrypted, paymentId, amountPaid
//             );

//             mailService.sendDonationReceiptWithAttachment(
//                     decrypted.getEmail(),
//                     decrypted.getFirstName() + " " + decrypted.getLastName(),
//                     amountPaid,
//                     paymentId,
//                     pdf,
//                     "Monthly_Donation_" + paymentId + ".pdf"
//             );

//             System.out.println("📧 Monthly receipt sent");

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     });
// }

// //         if (event.startsWith("subscription.")) {

// //             JSONObject sub = json.getJSONObject("payload")
// //                     .getJSONObject("subscription")
// //                     .getJSONObject("entity");

// //             String subscriptionId = sub.getString("id");
// //             String subStatus = sub.optString("status", "").toUpperCase();

// //             donationRepo.findBySubscriptionId(subscriptionId).ifPresent(donor -> {

// //                 donor.setSubscriptionStatus(subStatus);

// //                 // ✅ AUTHENTICATED / ACTIVE = SUCCESS
// //                 if ("AUTHENTICATED".equals(subStatus) || "ACTIVE".equals(subStatus)) {
// //                     donor.setStatus("SUCCESS");
// //                 }

// //                 donationRepo.save(donor);
// //                 System.out.println("✅ subscription update → " + subStatus);
// //             });
// //         }

// //         // ------------------------------------------------------------------
// //         // 4️⃣ MONTHLY CHARGE (IMPORTANT FIX)
// //         // ------------------------------------------------------------------
// //         if ("subscription.charged".equals(event)) {

// //             JSONObject p = json.getJSONObject("payload")
// //                     .getJSONObject("payment")
// //                     .getJSONObject("entity");

// //             String subscriptionId = p.getString("subscription_id");
// //             String paymentId = p.getString("id");
// //             double amountPaid = p.getInt("amount") / 100.0;

// //             donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

// //                 Donourentity monthly = new Donourentity();

// //                 // ✅ Copy NON-ENCRYPTED fields only
// //                 monthly.setFirstName(parent.getFirstName());
// //                 monthly.setLastName(parent.getLastName());
// //                 monthly.setEmail(parent.getEmail());
// //                 monthly.setMobile(parent.getMobile());

// //                 monthly.setSubscriptionId(subscriptionId);
// //                 monthly.setPaymentId(paymentId);
// //                 monthly.setAmount(amountPaid);
// //                 monthly.setStatus("SUCCESS");
// //                 monthly.setSubscriptionStatus("CHARGED");
// //                 monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

// //                 // ✅ DIRECT SAVE (DO NOT call saveDonation)
// //                 donationRepo.save(monthly);

// //                 System.out.println("✅ Monthly debit saved (new row)");
// //                 // ✅ SEND MONTHLY RECEIPT
// //         try {
// //             Donourentity decrypted = donationService.findByIdDecrypt(monthly.getId());

// //             byte[] pdf = pdfReceiptServiceee.generateMonthlyDebitReceipt(
// //                     decrypted, paymentId, amount
// //             );

// //             mailService.sendDonationReceiptWithAttachment(
// //                     decrypted.getEmail(),
// //                     decrypted.getFirstName() + " " + decrypted.getLastName(),
// //                     amount,
// //                     paymentId,
// //                     pdf,
// //                     "Monthly_Donation_" + paymentId + ".pdf"
// //             );

// //             System.out.println("📧 Monthly receipt sent");

// //         } catch (Exception e) {
// //             e.printStackTrace();
// //         }
// //             });
// //         }

//         return ResponseEntity.ok("OK");

//     } catch (Exception e) {
//         e.printStackTrace();
//         return ResponseEntity.status(500).body("error");
//     }
// }

//     // Helper to sync subscription status from webhook into DB
//     private void updateDonorFromSubscriptionEntity(JSONObject sub, String event) {
//         try {
//             String subscriptionId = sub.optString("id", null);
//             String rzpStatus = sub.optString("status", "").toUpperCase();

//             System.out.println(event + " → subscriptionId=" + subscriptionId + ", status=" + rzpStatus);

//             if (subscriptionId == null) {
//                 System.out.println("⚠ subscription entity has no id");
//                 return;
//             }

//             Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);
//             if (donor == null) {
//                 System.out.println("⚠ No donor found for subscriptionId=" + subscriptionId);
//                 return;
//             }

//             donor.setSubscriptionStatus(rzpStatus);

//             if ("AUTHENTICATED".equals(rzpStatus) || "ACTIVE".equals(rzpStatus)) {
//                 donor.setStatus("SUCCESS");
//             } else if ("HALTED".equals(rzpStatus) || "CANCELLED".equals(rzpStatus)) {
//                 donor.setStatus("FAILED");
//             }

//             donationRepo.save(donor);
//             System.out.println("✅ Subscription status updated from webhook for donorId=" + donor.getId());
//         } catch (Exception e) {
//             System.out.println("⚠ Error updating donor from subscription entity");
//             e.printStackTrace();
//         }
//     }
// }
// ------------------------------------------------------------------------------------------------------
package com.komal.template_backend.controller;

import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import com.komal.template_backend.service.DonationService;
import com.komal.template_backend.service.MailService;
// import com.komal.template_backend.service.pdfReceiptServiceee;
import com.komal.template_backend.service.PdfReceiptServic;
import com.razorpay.Order;
import com.razorpay.Plan;
import com.razorpay.RazorpayClient;
import com.razorpay.Subscription;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class RazorpayController {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    @Value("${razorpay.webhook_secret}")
    private String webhookSecret;

    @Value("${razorpay.subscription_years}")
    private int subscriptionYears;

    @Autowired
    private DonationService donationService;

    // @Autowired
    // private pdfReceiptServiceee pdfReceiptServicee;
    @Autowired
private PdfReceiptServic pdfReceiptService;
;


    @Autowired
    private MailService mailService;

    @Autowired
    private DonationRepo donationRepo;

    // --------------------------------------------------------------------
    // ONE-TIME ORDER CREATION
    // --------------------------------------------------------------------

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Donourentity donor) {
        System.out.println("========= CREATE ONE-TIME ORDER =========");
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", donor.getAmount() * 100);
            options.put("currency", "INR");
            options.put("receipt", "receipt_" + System.currentTimeMillis());
            options.put("payment_capture", 1);

            System.out.println("Order options: " + options);
            Order order = client.orders.create(options);
            System.out.println("Created order: " + order);

            donor.setOrderId(order.get("id"));
            donor.setStatus("PENDING");
            donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

            Donourentity saved = donationService.saveDonation(donor);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", order.get("id"),
                    "donorId", saved.getId(),
                    "amount", donor.getAmount() * 100,
                    "currency", "INR",
                    "keyId", keyId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --------------------------------------------------------------------
    // ONE-TIME PAYMENT VERIFY
    // --------------------------------------------------------------------

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
        System.out.println("========= VERIFY ONE-TIME PAYMENT =========");
        System.out.println("RAW BODY: " + body);

        try {
            String razorpayOrderId = (String) body.get("razorpay_order_id");
            String razorpayPaymentId = (String) body.get("razorpay_payment_id");
            String razorpaySignature = (String) body.get("razorpay_signature");

            System.out.println("➡ order_id   = " + razorpayOrderId);
            System.out.println("➡ payment_id = " + razorpayPaymentId);
            System.out.println("➡ signature  = " + razorpaySignature);

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing required fields"
                ));
            }

            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = hmacSha256(payload, keySecret);

            System.out.println("VERIFY SIGNATURE");
            System.out.println("  payload           = " + payload);
            System.out.println("  expectedSignature = " + generatedSignature);
            System.out.println("  receivedSignature = " + razorpaySignature);

            if (!generatedSignature.equals(razorpaySignature)) {
                System.out.println("❌ INVALID SIGNATURE (ONE-TIME)");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid signature"
                ));
            }

            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            com.razorpay.Payment payment = client.payments.fetch(razorpayPaymentId);
            JSONObject paymentJson = payment.toJson();
            System.out.println("Payment JSON: " + paymentJson);

            String status = paymentJson.optString("status", "UNKNOWN");
            int amount = paymentJson.optInt("amount", 0);
            String payerEmail = paymentJson.optString("email", "");
            String payerContact = paymentJson.optString("contact", "");
            String method = paymentJson.optString("method", "");
            String bank = paymentJson.optString("bank", "");
            String vpa = paymentJson.optString("vpa", "");
            String wallet = paymentJson.optString("wallet", "");

            Optional<Donourentity> donorOpt = donationRepo.findByOrderId(razorpayOrderId);
            if (donorOpt.isPresent()) {
                Donourentity donor = donorOpt.get();

                donor.setPaymentId(razorpayPaymentId);
                donor.setSignature(razorpaySignature);
                donor.setStatus(status.equalsIgnoreCase("captured") ? "SUCCESS" : status.toUpperCase());
                donor.setPaymentMethod(method);
                donor.setRecordType("ONE_TIME");
                donor.setUpiId(vpa);
                donor.setWallet(wallet);
                donor.setPaymentInfo(bank.isEmpty() ? vpa : bank);
                donor.setPayerEmail(payerEmail);
                donor.setPayerContact(payerContact);
                donor.setAmount(amount / 100.0);
                donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

                donationService.saveDonation(donor);
                donorId = donor.getId(); 

                // ----- Receipt + Email -----
                try {
                    Donourentity decrypted = donationService.findByIdDecrypt(donor.getId());
                    if (decrypted != null) {
                        byte[] pdf = pdfReceiptService.generateOneTimeDonationReceipt(
                                decrypted,
                                decrypted.getPaymentId(),
                                decrypted.getAmount()
                        );

                        String recipient = (decrypted.getPayerEmail() != null &&
                                !decrypted.getPayerEmail().isBlank())
                                ? decrypted.getPayerEmail()
                                : decrypted.getEmail();

                        if (recipient != null && !recipient.isBlank()) {
                            mailService.sendDonationReceiptWithAttachment(
                                    recipient,
                                    decrypted.getFirstName() + " " + decrypted.getLastName(),
                                    decrypted.getAmount(),
                                    decrypted.getPaymentId(),
                                    pdf,
                                    "DonationReceipt_" + decrypted.getPaymentId() + ".pdf"
                            );
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Error while sending one-time receipt");
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠ No donor found for orderId=" + razorpayOrderId);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", status,
                    "amount", amount / 100.0,
                    "donorId", donorId  , // ✅ THIS WAS MISSING
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --------------------------------------------------------------------
    // HMAC UTILITY
    // --------------------------------------------------------------------

    private String hmacSha256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
    private boolean verifyWebhook(String payload, String headerSignature) {
        try {
            String expected = hmacSha256(payload, webhookSecret);
            return expected.equals(headerSignature);
        } catch (Exception e) {
            return false;
        }
    }
    // --------------------------------------------------------------------
    // CREATE DONOR (SUBSCRIPTION)
    // --------------------------------------------------------------------

    @PostMapping("/create-donor-record")
    public ResponseEntity<?> createDonor(@RequestBody Donourentity donor) {
        System.out.println("========= CREATE SUBSCRIPTION DONOR RECORD =========");
        try {
            donor.setStatus("PENDING");
            donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            Donourentity saved = donationService.saveDonation(donor);
            return ResponseEntity.ok(Map.of("success", true, "donorId", saved.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private int parseIntSafe(Object o, int fallback) {
        try {
            if (o == null) return fallback;
            if (o instanceof Number) return ((Number) o).intValue();
            String s = String.valueOf(o).trim();
            return s.isEmpty() ? fallback : Integer.parseInt(s);
        } catch (Exception ex) {
            return fallback;
        }
    }

    // private long getNextStartDate(int startDay) {
    //     LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    //     LocalDateTime next;

    //     int today = now.getDayOfMonth();
    //     if (today < startDay) {
    //         next = now.withDayOfMonth(startDay).withHour(0).withMinute(0).withSecond(0);
    //     } else {
    //         next = now.plusMonths(1).withDayOfMonth(startDay).withHour(0).withMinute(0).withSecond(0);
    //     }

    //     return next.atZone(ZoneId.of("Asia/Kolkata")).toEpochSecond();
    // }
private long getNextStartDate(int startDay) {

    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

    // ✅ ALWAYS move to NEXT month
    LocalDateTime nextMonth =
            now.plusMonths(1)
               .withDayOfMonth(Math.min(startDay,
                       now.plusMonths(1).toLocalDate().lengthOfMonth()))
               .withHour(0)
               .withMinute(0)
               .withSecond(0)
               .withNano(0);

    return nextMonth.atZone(ZoneId.of("Asia/Kolkata")).toEpochSecond();
}

  // --------------------------------------------------------------------
// SUBSCRIPTION CREATE  (E-MANDATE)
// --------------------------------------------------------------------

@PostMapping("/create-subscription")
public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {
    System.out.println("====================== CREATE SUBSCRIPTION START ======================");
    System.out.println("Received: " + req);

    try {
        String donorId = String.valueOf(req.get("donorId"));
        int amount = parseIntSafe(req.get("amount"), 0);

        if (amount <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Invalid amount"));
        }

        int starterAmount = parseIntSafe(req.get("starterAmount"), 10);
        if (starterAmount < 1 || starterAmount > 28) {
            starterAmount = 10;
        }

        Donourentity donor = donationRepo.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        // --------- PLAN ---------
        Map<String, Object> item = Map.of(
                "name", "Monthly Donation",
                "amount", amount * 100,   // paise
                "currency", "INR"
        );

        Map<String, Object> planRequest = Map.of(
                "period", "monthly",
                "interval", 1,
                "item", item
        );

        System.out.println("Plan request: " + new JSONObject(planRequest).toString(2));
        Plan plan = client.plans.create(new JSONObject(planRequest));
        System.out.println("Created plan: " + plan);

        donor.setPlanId(plan.get("id"));
        donor.setRazorpayPlanId(plan.get("id"));
        donor.setMonthlyAmount((double) amount);
        donationRepo.save(donor);

        // --------- SUBSCRIPTION (E-MANDATE) ---------
        Map<String, Object> notes = Map.of(
                "donorId", donorId,
                "monthlyAmount", String.valueOf(amount)
        );

        Map<String, Object> subscriptionRequest = new HashMap<>();
        subscriptionRequest.put("plan_id", plan.get("id"));
        subscriptionRequest.put("customer_notify", 1);
        subscriptionRequest.put("total_count", (subscriptionYears * 12) - 1);
        subscriptionRequest.put("notes", notes);

        // ⚠ For e-mandate, do NOT force a separate ₹1 upfront payment from our side.
        // Razorpay/bank will handle the auth txn and may show 0/1 rupee.
        // So we REMOVE the addons block that we had earlier.

        Integer useStartDay = donor.getStartDay() != null ? donor.getStartDay() : starterAmount;
        if (useStartDay != null) {
            subscriptionRequest.put("start_at", getNextStartDate(useStartDay));
        }

        System.out.println("📦 Subscription Payload: " + new JSONObject(subscriptionRequest).toString(2));

        Subscription subscription = client.subscriptions.create(new JSONObject(subscriptionRequest));
        System.out.println("Created subscription: " + subscription);

        donor.setSubscriptionId(subscription.get("id"));
        donor.setSubscriptionStatus("CREATED");
        donor.setStartDay(useStartDay);

        // e-mandate fields – initially pending
        donor.setMandateStatus("PENDING");
        donor.setMandateAmount((double) amount);
        donor.setMandateFrequency("MONTHLY");

        donationRepo.save(donor);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "subscription_id", subscription.get("id"),
                "plan_id", plan.get("id"),
                "keyId", keyId
        ));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", e.getMessage()));
    }
}
@PostMapping("/razorpay-webhook")
public ResponseEntity<?> webhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Signature") String signature) {

    System.out.println("====================== WEBHOOK RECEIVED ======================");

    try {
        // ✅ Signature verification
        if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.optString("event");

        JSONObject payloadObj = json.optJSONObject("payload");
        if (payloadObj == null) return ResponseEntity.ok("IGNORED");

        JSONObject paymentEntity = payloadObj
                .optJSONObject("payment")
                .optJSONObject("entity");

        JSONObject subEntity = payloadObj
                .optJSONObject("subscription")
                .optJSONObject("entity");

        JSONObject mandateEntity = payloadObj
                .optJSONObject("mandate")
                .optJSONObject("entity");

        // ============================================================
        // 1️⃣ ONE-TIME PAYMENT SAFETY
        // ============================================================
        if ("payment.captured".equals(event) && paymentEntity != null) {

            String orderId = paymentEntity.optString("order_id");
            String paymentId = paymentEntity.optString("id");

            donationRepo.findByOrderId(orderId).ifPresent(donor -> {
                donor.setPaymentId(paymentId);
                donor.setStatus("SUCCESS");
                donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                donationRepo.save(donor);
            });
        }

        // ============================================================
        // 2️⃣ SUBSCRIPTION AUTHENTICATION (MANDATE APPROVED)
        // ============================================================
        if (("subscription.authenticated".equals(event)
                || "subscription.activated".equals(event))
                && subEntity != null) {

            String subscriptionId = subEntity.optString("id");

            donationRepo.findBySubscriptionId(subscriptionId).ifPresent(donor -> {

                donor.setSubscriptionStatus("AUTHENTICATED");
                donor.setMandateStatus("AUTHORIZED");
                donor.setRecordType("SUBSCRIPTION_PARENT");
                donor.setStatus("SUCCESS");

                if (mandateEntity != null) {
                    donor.setMandateId(mandateEntity.optString("id"));
                    donor.setRazorpayMandateId(mandateEntity.optString("id"));
                }

                donationRepo.save(donor);

                // ✅ send mail ONCE
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
                                subscriptionId,
                                pdf,
                                "Mandate_Confirmation_" + subscriptionId + ".pdf"
                        );

                        donor.setMandateMailSent(true);
                        donationRepo.save(donor);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // ============================================================
        // 3️⃣ MONTHLY DEBIT (subscription.charged)
        // ============================================================
        if ("subscription.charged".equals(event)
                && paymentEntity != null
                && subEntity != null) {

            final String subscriptionId =
                    paymentEntity.getString("subscription_id");

            final String paymentId =
                    paymentEntity.getString("id");

            final double amountPaid =
                    paymentEntity.getInt("amount") / 100.0;

            final int cycleCount =
                    subEntity.optInt("paid_count", 0);

            donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

                Donourentity monthly = new Donourentity();

                monthly.setFirstName(parent.getFirstName());
                monthly.setLastName(parent.getLastName());
                monthly.setEmail(parent.getEmail());
                monthly.setMobile(parent.getMobile());

                monthly.setSubscriptionId(subscriptionId);
                monthly.setPaymentId(paymentId);
                monthly.setAmount(amountPaid);
                monthly.setStatus("SUCCESS");
                monthly.setSubscriptionStatus("CHARGED");
                monthly.setFrequency("MONTHLY");
                monthly.setRecordType("SUBSCRIPTION_MONTHLY");
                monthly.setCycleCount(cycleCount);
                // monthly.setCycleCount(paidCount);   // ✅ REQUIRED
               

                monthly.setDonationDate(
                        LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                );

                donationRepo.save(monthly);

                // ✅ Monthly receipt
                try {
                    Donourentity decryptedParent =
                            donationService.findByIdDecrypt(parent.getId());

                    byte[] pdf =
                            pdfReceiptService.generateMonthlyDebitReceipt(
                                    decryptedParent,
                                    paymentId,
                                    amountPaid
                            );

                    mailService.sendDonationReceiptWithAttachment(
                            decryptedParent.getEmail(),
                            decryptedParent.getFirstName() + " " + decryptedParent.getLastName(),
                            amountPaid,
                            paymentId,
                            pdf,
                            "Monthly_Donation_" + cycleCount + "_" + paymentId + ".pdf"
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return ResponseEntity.ok("OK");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("ERROR");
    }
}


// --------------------------------------------------------------------
// WEBHOOK HANDLER -latest
// --------------------------------------------------------------------
// @PostMapping("/razorpay-webhook")
// public ResponseEntity<?> webhook(
//         @RequestBody String payload,
//         @RequestHeader("X-Razorpay-Signature") String signature) {

//     System.out.println("====================== WEBHOOK RECEIVED ======================");
//     System.out.println("Signature: " + signature);
//     System.out.println("Payload: " + payload);

//     try {
//         if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
//             System.out.println("❌ Invalid webhook signature");
//             return ResponseEntity.status(400).body("Invalid signature");
//         }

//         JSONObject json = new JSONObject(payload);
//         String event = json.optString("event");
//         System.out.println("📣 EVENT = " + event);

//         // Safe extraction of payload objects
//         JSONObject payloadObj = json.optJSONObject("payload");

//         JSONObject payment = null;
//         JSONObject subscription = null;
//         JSONObject mandate = null;

//         if (payloadObj != null) {
//             JSONObject pWrap = payloadObj.optJSONObject("payment");
//             if (pWrap != null) payment = pWrap.optJSONObject("entity");

//             JSONObject sWrap = payloadObj.optJSONObject("subscription");
//             if (sWrap != null) subscription = sWrap.optJSONObject("entity");

//             JSONObject mWrap = payloadObj.optJSONObject("mandate");
//             if (mWrap != null) mandate = mWrap.optJSONObject("entity");
//         }

//         // ------------------------------------------------------------------
//         // 1️⃣ ONE-TIME PAYMENT SAFETY (payment.captured)
//         // ------------------------------------------------------------------
//         if ("payment.captured".equals(event) && payment != null) {

//             String orderId   = payment.optString("order_id", null);
//             String paymentId = payment.optString("id", null);

//             if (orderId != null) {
//                 donationRepo.findByOrderId(orderId).ifPresent(donor -> {
//                     donor.setPaymentId(paymentId);
//                     donor.setStatus("SUCCESS");
//                     donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//                     donationRepo.save(donor);
//                     System.out.println("✅ payment.captured → updated donor");
//                 });
//             }
//         }

//         // ------------------------------------------------------------------
//         // 2️⃣ SUBSCRIPTION AUTHENTICATED  → BANK MANDATE APPROVED
//         // ------------------------------------------------------------------
//         if ("subscription.authenticated".equals(event)
//                 || "subscription.activated".equals(event)) {

//             if (subscription == null) {
//                 System.out.println("⚠ subscription entity missing in payload");
//             } else {
//                 String subscriptionId = subscription.optString("id", null);
//                 System.out.println("✅ subscription.authenticated for subId=" + subscriptionId);

//                 if (subscriptionId != null) {
//                     donationRepo.findBySubscriptionId(subscriptionId).ifPresent(donor -> {

//                         // --- Update statuses ---
//                         donor.setSubscriptionStatus("AUTHENTICATED");
//                         donor.setRecordType("SUBSCRIPTION_PARENT");
//                         donor.setStatus("SUCCESS");

//                         // Treat this as mandate authorized
//                         donor.setMandateStatus("AUTHORIZED");

//                         // Save payer details from payment if present
//                         if (payment != null) {
//                             donor.setPaymentId(payment.optString("id"));
//                             donor.setPayerEmail(
//                                     payment.optString("email", donor.getEmail()));
//                             donor.setPayerContact(payment.optString("contact"));
//                             donor.setPaymentMethod(payment.optString("method"));
//                         }

//                         // Save mandate id/amount if mandate object present
//                         if (mandate != null) {
//                             donor.setRazorpayMandateId(mandate.optString("id"));
//                             donor.setMandateId(mandate.optString("id"));
//                             donor.setMandateAmount(mandate.optDouble("amount", donor.getMandateAmount() != null ?
//                                     donor.getMandateAmount() * 100 : 0.0) / 100.0);
//                             donor.setMandateStatus(mandate.optString("status", "AUTHORIZED"));
//                         }

//                         donationRepo.save(donor);

//                         // --- Send confirmation mail ONCE after bank approval ---
//                         if (Boolean.FALSE.equals(donor.getMandateMailSent())) {
//                             try {
//                                 Donourentity decrypted =
//                                         donationService.findByIdDecrypt(donor.getId());

//                                 byte[] pdf =
//                                         pdfReceiptService.generateMandateConfirmation(decrypted);

//                                 mailService.sendDonationReceiptWithAttachment(
//                                         decrypted.getEmail(),
//                                         decrypted.getFirstName() + " " + decrypted.getLastName(),
//                                         decrypted.getMonthlyAmount(),
//                                         subscriptionId,
//                                         pdf,
//                                         "Mandate_Confirmation_" + subscriptionId + ".pdf"
//                                 );

//                                 donor.setMandateMailSent(true);
//                                 donationRepo.save(donor);

//                                 System.out.println("📧 Mandate confirmation mail sent");

//                             } catch (Exception e) {
//                                 System.out.println("❌ Error sending mandate confirmation mail");
//                                 e.printStackTrace();
//                             }
//                         }
//                     });
//                 }
//             }
//         }

//         // ------------------------------------------------------------------
//         // 3️⃣ MONTHLY DEBIT (subscription.charged) → MONTHLY RECEIPT
//         // ------------------------------------------------------------------
//         // if ("subscription.charged".equals(event) && payment != null) {

//         //     String subscriptionId = payment.optString("subscription_id", null);
//         //     String paymentId      = payment.optString("id", null);
//         //     double amountPaid     = payment.optInt("amount", 0) / 100.0;

//         //     System.out.println("💳 subscription.charged for sub=" + subscriptionId +
//         //                        " payment=" + paymentId + " amount=" + amountPaid);

//         //     if (subscriptionId != null) {
//         //         donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

//         //             // 👉 Save monthly transaction row (child record)
//         //             Donourentity monthly = new Donourentity();
//         //             monthly.setFirstName(parent.getFirstName());
//         //             monthly.setLastName(parent.getLastName());
//         //             monthly.setEmail(parent.getEmail());
//         //             monthly.setMobile(parent.getMobile());
//         //             monthly.setSubscriptionId(subscriptionId);
//         //             monthly.setPaymentId(paymentId);
//         //             monthly.setAmount(amountPaid);
//         //             monthly.setStatus("SUCCESS");
//         //             monthly.setSubscriptionStatus("CHARGED");
//         //             monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//         //             donationRepo.save(monthly);
//         //             System.out.println("✅ Monthly debit saved (new row)");

//         //             // 👉 Send monthly receipt using PARENT donor details
//         //             try {
//         //                 Donourentity decryptedParent =
//         //                         donationService.findByIdDecrypt(parent.getId());

//         //                 byte[] pdf =
//         //                         pdfReceiptService.generateMonthlyDebitReceipt(
//         //                                 decryptedParent, paymentId, amountPaid
//         //                         );

//         //                 mailService.sendDonationReceiptWithAttachment(
//         //                         decryptedParent.getEmail(),
//         //                         decryptedParent.getFirstName() + " " + decryptedParent.getLastName(),
//         //                         amountPaid,
//         //                         paymentId,
//         //                         pdf,
//         //                         "Monthly_Donation_" + paymentId + ".pdf"
//         //                 );

//         //                 System.out.println("📧 Monthly receipt sent");

//         //             } catch (Exception e) {
//         //                 System.out.println("❌ Error sending monthly debit receipt");
//         //                 e.printStackTrace();
//         //             }
//         //         });
//         //     }
//         // }
//         // -----------------------------------------------------------------------------------------
// //         if ("subscription.charged".equals(event)) {

// //     JSONObject payment = json.getJSONObject("payload")
// //             .getJSONObject("payment")
// //             .getJSONObject("entity");

// //     JSONObject sub = json.getJSONObject("payload")
// //             .getJSONObject("subscription")
// //             .getJSONObject("entity");

// //     String subscriptionId = payment.getString("subscription_id");
// //     String paymentId = payment.getString("id");
// //     double amountPaid = payment.getInt("amount") / 100.0;

// //     int paidCount = sub.optInt("paid_count", 0); // ✅ cycle number

// //     donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

// //         // ✅ CREATE MONTHLY CHILD RECORD
// //         Donourentity monthly = new Donourentity();

// //         // -----------------------------
// //         // INHERIT FROM PARENT
// //         // -----------------------------
// //         monthly.setFirstName(parent.getFirstName());
// //         monthly.setLastName(parent.getLastName());
// //         monthly.setEmail(parent.getEmail());
// //         monthly.setMobile(parent.getMobile());

// //         monthly.setSubscriptionId(subscriptionId);
// //         monthly.setFrequency("MONTHLY");
// //         monthly.setPaymentMode("SUBSCRIPTION");
// //         monthly.setReceiptType("SUBSCRIPTION_MONTHLY");

// //         // -----------------------------
// //         // DEBIT-SPECIFIC DATA
// //         // -----------------------------
// //         monthly.setPaymentId(paymentId);
// //         monthly.setAmount(amountPaid);
// //         monthly.setStatus("SUCCESS");
// //         monthly.setSubscriptionStatus("CHARGED");
// //         monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
// //         monthly.setCycleCount(paidCount);
// //         monthly.setRecordType("SUBSCRIPTION_MONTHLY");

// //         // optional
// //         monthly.setPaymentMethod(payment.optString("method"));
// //         monthly.setPaymentInfo(
// //                 payment.optString("bank",
// //                 payment.optString("wallet",
// //                 payment.optString("vpa", "")))
// //         );

// //         donationRepo.save(monthly);

// //         System.out.println(
// //                 "✅ Monthly debit saved | sub=" + subscriptionId +
// //                 " cycle=" + paidCount
// //         );

// //         // -----------------------------
// //         // ✅ SEND MONTHLY RECEIPT
// //         // -----------------------------
// //         try {
// //             Donourentity decryptedParent =
// //                     donationService.findByIdDecrypt(parent.getId());

// //             byte[] pdf =
// //                     pdfReceiptService.generateMonthlyDebitReceipt(
// //                             decryptedParent,
// //                             paymentId,
// //                             amountPaid,
// //                             paidCount // ✅ pass cycle count
// //                     );

// //             mailService.sendDonationReceiptWithAttachment(
// //                     decryptedParent.getEmail(),
// //                     decryptedParent.getFirstName() + " " + decryptedParent.getLastName(),
// //                     amountPaid,
// //                     paymentId,
// //                     pdf,
// //                     "Monthly_Donation_" + paidCount + "_" + paymentId + ".pdf"
// //             );

// //             System.out.println("📧 Monthly receipt sent (cycle " + paidCount + ")");

// //         } catch (Exception e) {
// //             e.printStackTrace();
// //         }
// //     });
// // }
//         // -----------------------------------------------------------------------------------
// // if ("subscription.charged".equals(event)) {

// //     JSONObject paymentEntity = json.getJSONObject("payload")
// //             .getJSONObject("payment")
// //             .getJSONObject("entity");

// //     JSONObject sub = json.getJSONObject("payload")
// //             .getJSONObject("subscription")
// //             .getJSONObject("entity");

// //     String subscriptionId = paymentEntity.getString("subscription_id");
// //     String paymentId = paymentEntity.getString("id");
// //     double amountPaid = paymentEntity.getInt("amount") / 100.0;

// //     int paidCount = sub.optInt("paid_count", 0);

// //     donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

// //         Donourentity monthly = new Donourentity();

// //         monthly.setFirstName(parent.getFirstName());
// //         monthly.setLastName(parent.getLastName());
// //         monthly.setEmail(parent.getEmail());
// //         monthly.setMobile(parent.getMobile());

// //         monthly.setSubscriptionId(subscriptionId);
// //         monthly.setFrequency("MONTHLY");
// //         monthly.setPaymentMode("SUBSCRIPTION");
// //         monthly.setReceiptType("SUBSCRIPTION_MONTHLY");

// //         monthly.setPaymentId(paymentId);
// //         monthly.setAmount(amountPaid);
// //         monthly.setStatus("SUCCESS");
// //         monthly.setSubscriptionStatus("CHARGED");
// //         monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
// //         monthly.setCycleCount(paidCount);
// //         monthly.setRecordType("SUBSCRIPTION_MONTHLY");

// //         monthly.setPaymentMethod(paymentEntity.optString("method"));
// //         monthly.setPaymentInfo(
// //                 paymentEntity.optString("bank",
// //                 paymentEntity.optString("wallet",
// //                 paymentEntity.optString("vpa", "")))
// //         );

// //         donationRepo.save(monthly);

// //         System.out.println("✅ Monthly debit saved | sub=" + subscriptionId +
// //                 " cycle=" + paidCount);

// //         try {
// //             Donourentity decryptedParent =
// //                     donationService.findByIdDecrypt(parent.getId());

// //             byte[] pdf =
// //                     pdfReceiptService.generateMonthlyDebitReceipt(
// //                             decryptedParent,
// //                             paymentId,
// //                             amountPaid
// //                     );

// //             mailService.sendDonationReceiptWithAttachment(
// //                     decryptedParent.getEmail(),
// //                     decryptedParent.getFirstName() + " " + decryptedParent.getLastName(),
// //                     amountPaid,
// //                     paymentId,
// //                     pdf,
// //                     "Monthly_Donation_" + paidCount + "_" + paymentId + ".pdf"
// //             );

// //         } catch (Exception e) {
// //             e.printStackTrace();
// //         }
// //     });
// // }


// //         return ResponseEntity.ok("OK");

// //     } catch (Exception e) {
// //         e.printStackTrace();
// //         return ResponseEntity.status(500).body("error");
// //     }
// // }
// // ------------------------------------------------------------------------
//         if ("subscription.charged".equals(event)) {

//     JSONObject paymentEntity = json.getJSONObject("payload")
//             .getJSONObject("payment")
//             .getJSONObject("entity");

//     JSONObject sub = json.getJSONObject("payload")
//             .getJSONObject("subscription")
//             .getJSONObject("entity");

//     String subscriptionId = paymentEntity.getString("subscription_id");
//     String paymentId = paymentEntity.getString("id");
//     double amountPaid = paymentEntity.getInt("amount") / 100.0;
//     int paidCount = sub.optInt("paid_count", 0);

//     // ✅ FINAL COPIES FOR LAMBDA
//     final String finalPaymentId = paymentId;
//     final double finalAmountPaid = amountPaid;
//     final int finalPaidCount = paidCount;
//     final JSONObject finalPaymentEntity = paymentEntity;

//     donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

//         Donourentity monthly = new Donourentity();

//         monthly.setFirstName(parent.getFirstName());
//         monthly.setLastName(parent.getLastName());
//         monthly.setEmail(parent.getEmail());
//         monthly.setMobile(parent.getMobile());

//         monthly.setSubscriptionId(subscriptionId);
//         monthly.setFrequency("MONTHLY");
//         monthly.setPaymentMode("SUBSCRIPTION");
//         monthly.setReceiptType("SUBSCRIPTION_MONTHLY");

//         monthly.setPaymentId(finalPaymentId);
//         monthly.setAmount(finalAmountPaid);
//         monthly.setStatus("SUCCESS");
//         monthly.setSubscriptionStatus("CHARGED");
//         monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//         monthly.setCycleCount(finalPaidCount);
//         monthly.setRecordType("SUBSCRIPTION_MONTHLY");

//         monthly.setPaymentMethod(finalPaymentEntity.optString("method"));
//         monthly.setPaymentInfo(
//                 finalPaymentEntity.optString(
//                         "bank",
//                         finalPaymentEntity.optString(
//                                 "wallet",
//                                 finalPaymentEntity.optString("vpa", "")
//                         )
//                 )
//         );

//         donationRepo.save(monthly);

//         System.out.println(
//                 "✅ Monthly debit saved | sub=" + subscriptionId +
//                 " cycle=" + finalPaidCount
//         );

//         try {
//             Donourentity decryptedParent =
//                     donationService.findByIdDecrypt(parent.getId());

//             byte[] pdf =
//                     pdfReceiptService.generateMonthlyDebitReceipt(
//                             decryptedParent,
//                             finalPaymentId,
//                             finalAmountPaid
//                     );

//             mailService.sendDonationReceiptWithAttachment(
//                     decryptedParent.getEmail(),
//                     decryptedParent.getFirstName() + " " + decryptedParent.getLastName(),
//                     finalAmountPaid,
//                     finalPaymentId,
//                     pdf,
//                     "Monthly_Donation_" + finalPaidCount + "_" + finalPaymentId + ".pdf"
//             );

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     });
// }

 // --------------------------------------------------------------------
// WEBHOOK HANDLER
// --------------------------------------------------------------------
// @PostMapping("/razorpay-webhook")
// public ResponseEntity<?> webhook(@RequestBody String payload,
//                                  @RequestHeader("X-Razorpay-Signature") String signature) {

//     System.out.println("====================== WEBHOOK RECEIVED ======================");
//     System.out.println("Signature: " + signature);

//     try {
//         if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
//             System.out.println("❌ Invalid webhook signature");
//             return ResponseEntity.status(400).body("Invalid signature");
//         }

//         JSONObject json = new JSONObject(payload);
//         String event = json.optString("event");
//         System.out.println("📣 EVENT = " + event);

//         // ------------------------------------------------------------------
//         // 1️⃣ ONE-TIME PAYMENT SAFETY (payment.captured)
//         // ------------------------------------------------------------------
//         if ("payment.captured".equals(event)) {

//             JSONObject p = json.getJSONObject("payload")
//                     .getJSONObject("payment")
//                     .getJSONObject("entity");

//             String orderId = p.optString("order_id", null);
//             String paymentId = p.optString("id", null);

//             if (orderId != null) {
//                 donationRepo.findByOrderId(orderId).ifPresent(donor -> {
//                     donor.setPaymentId(paymentId);
//                     donor.setStatus("SUCCESS");
//                     donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//                     donationRepo.save(donor);
//                     System.out.println("✅ payment.captured → updated donor");
//                 });
//             }
//         }

//         // ------------------------------------------------------------------
//         // 2️⃣ MANDATE EVENTS (for e-mandate, if enabled in webhooks)
//         // ------------------------------------------------------------------
//         // Razorpay emits mandate events from the Mandates product. The exact
//         // list depends on what you subscribed to (authorized/paused/revoked/etc).
//         if (event.startsWith("mandate.")) {
//             try {
//                 JSONObject m = json.getJSONObject("payload")
//                         .getJSONObject("mandate")
//                         .getJSONObject("entity");

//                 String subscriptionId = m.optString("subscription_id", null);
//                 String mandateId      = m.optString("id", null);
//                 String mandateStatus  = m.optString("status", "").toUpperCase();

//                 System.out.println("🔔 mandate event → sub=" + subscriptionId +
//                                    " mandate=" + mandateId +
//                                    " status=" + mandateStatus);

//                 if (subscriptionId != null) {
//                     donationRepo.findBySubscriptionId(subscriptionId).ifPresent(donor -> {

//                         donor.setRazorpayMandateId(mandateId);
//                         donor.setMandateId(mandateId);            // your generic field
//                         donor.setMandateStatus(mandateStatus);
//                         donor.setStoredFromWebhook(true);

//                         // Amount / frequency / dates – set if present
//                         if (m.has("amount")) {
//                             donor.setMandateAmount(m.getDouble("amount") / 100.0);
//                         }
//                         if (m.has("start_at")) {
//                             LocalDateTime start = LocalDateTime.ofEpochSecond(
//                                     m.getLong("start_at"), 0, ZoneId.of("Asia/Kolkata").getRules().getOffset(LocalDateTime.now())
//                             );
//                             donor.setMandateStartDate(start);
//                         }
//                         if (m.has("end_at")) {
//                             long endAt = m.optLong("end_at", 0L);
//                             if (endAt > 0) {
//                                 LocalDateTime end = LocalDateTime.ofEpochSecond(
//                                         endAt, 0, ZoneId.of("Asia/Kolkata").getRules().getOffset(LocalDateTime.now())
//                                 );
//                                 donor.setMandateEndDate(end);
//                             }
//                         }

//                         // If mandate is authorized/active → mark subscription/donation success
//                         if ("AUTHORIZED".equals(mandateStatus) ||
//                                 "ACTIVE".equals(mandateStatus)) {
//                             donor.setSubscriptionStatus("AUTHENTICATED");
//                             donor.setStatus("SUCCESS");
//                         } else if ("REVOKED".equals(mandateStatus) ||
//                                    "PAUSED".equals(mandateStatus) ||
//                                    "CANCELLED".equals(mandateStatus)) {
//                             donor.setStatus("FAILED");
//                         }

//                         donationRepo.save(donor);
//                         System.out.println("✅ mandate.* → updated donor " + donor.getId());

//                         // Send confirmation mail ONLY when first time becomes authorized
//                         if ("AUTHORIZED".equals(mandateStatus) &&
//                                 Boolean.FALSE.equals(donor.getMandateMailSent())) {

//                             try {
//                                 Donourentity decrypted = donationService.findByIdDecrypt(donor.getId());

//                                 byte[] pdf = pdfReceiptService.generateMandateConfirmation(decrypted);

//                                 mailService.sendDonationReceiptWithAttachment(
//                                         decrypted.getEmail(),
//                                         decrypted.getFirstName() + " " + decrypted.getLastName(),
//                                         decrypted.getMonthlyAmount(),
//                                         mandateId,
//                                         pdf,
//                                         "Mandate_Confirmation_" + mandateId + ".pdf"
//                                 );

//                                 donor.setMandateMailSent(true);
//                                 donationRepo.save(donor);

//                                 System.out.println("📧 Mandate confirmation mail sent");
//                             } catch (Exception e) {
//                                 e.printStackTrace();
//                             }
//                         }
//                     });
//                 }
//             } catch (Exception e) {
//                 System.out.println("⚠ Error in mandate.* handler");
//                 e.printStackTrace();
//             }
//         }

//         // ------------------------------------------------------------------
//         // 3️⃣ SUBSCRIPTION STATE EVENTS
//         // ------------------------------------------------------------------
//         if (event.startsWith("subscription.")) {

//             // subscription.charged also has payment entity; handle below for debits
//             JSONObject sub = json.getJSONObject("payload")
//                     .getJSONObject("subscription")
//                     .getJSONObject("entity");

//             updateDonorFromSubscriptionEntity(sub, event);
//         }

//         // ------------------------------------------------------------------
//         // 4️⃣ MONTHLY CHARGE (subscription.charged)
//         // ------------------------------------------------------------------
//         if ("subscription.charged".equals(event)) {

//             JSONObject p = json.getJSONObject("payload")
//                     .getJSONObject("payment")
//                     .getJSONObject("entity");

//             String subscriptionId = p.getString("subscription_id");
//             String paymentId = p.getString("id");
//             double amountPaid = p.getInt("amount") / 100.0;

//             donationRepo.findBySubscriptionId(subscriptionId).ifPresent(parent -> {

//                 Donourentity monthly = new Donourentity();

//                 monthly.setFirstName(parent.getFirstName());
//                 monthly.setLastName(parent.getLastName());
//                 monthly.setEmail(parent.getEmail());
//                 monthly.setMobile(parent.getMobile());

//                 monthly.setSubscriptionId(subscriptionId);
//                 monthly.setPaymentId(paymentId);
//                 monthly.setAmount(amountPaid);
//                 monthly.setStatus("SUCCESS");
//                 monthly.setSubscriptionStatus("CHARGED");
//                 monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

//                 donationRepo.save(monthly);

//                 System.out.println("✅ Monthly debit saved (new row)");

//                 try {
//                     Donourentity decrypted = donationService.findByIdDecrypt(monthly.getId());

//                     byte[] pdf = pdfReceiptService.generateMonthlyDebitReceipt(
//                             decrypted, paymentId, amountPaid
//                     );

//                     mailService.sendDonationReceiptWithAttachment(
//                             decrypted.getEmail(),
//                             decrypted.getFirstName() + " " + decrypted.getLastName(),
//                             amountPaid,
//                             paymentId,
//                             pdf,
//                             "Monthly_Donation_" + paymentId + ".pdf"
//                     );

//                     System.out.println("📧 Monthly receipt sent");

//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             });
//         }

//         return ResponseEntity.ok("OK");

//     } catch (Exception e) {
//         e.printStackTrace();
//         return ResponseEntity.status(500).body("error");
//     }
// }


  // Helper to sync subscription status from webhook into DB
private void updateDonorFromSubscriptionEntity(JSONObject sub, String event) {
    try {
        String subscriptionId = sub.optString("id", null);
        String rzpStatus = sub.optString("status", "").toUpperCase();

        System.out.println(event + " → subscriptionId=" + subscriptionId + ", status=" + rzpStatus);

        if (subscriptionId == null) {
            System.out.println("⚠ subscription entity has no id");
            return;
        }

        Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);
        if (donor == null) {
            System.out.println("⚠ No donor found for subscriptionId=" + subscriptionId);
            return;
        }

        donor.setSubscriptionStatus(rzpStatus);

        if ("AUTHENTICATED".equals(rzpStatus) || "ACTIVE".equals(rzpStatus)) {
            donor.setStatus("SUCCESS");
        } else if ("HALTED".equals(rzpStatus) || "CANCELLED".equals(rzpStatus)
                || "EXPIRED".equals(rzpStatus) || "COMPLETED".equals(rzpStatus)) {
            donor.setStatus("FAILED");
        }

        donationRepo.save(donor);
        System.out.println("✅ Subscription status updated from webhook for donorId=" + donor.getId());
    } catch (Exception e) {
        System.out.println("⚠ Error updating donor from subscription entity");
        e.printStackTrace();
    }
}

        
}




































































































































































