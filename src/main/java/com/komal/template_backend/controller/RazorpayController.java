

// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import com.komal.template_backend.service.DonationService;
// import com.komal.template_backend.service.MailService;
// import com.komal.template_backend.service.PdfReceiptServic;
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
//     private PdfReceiptServic pdfReceiptService;
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
//                             pdf = pdfReceiptService.generateOneTimeDonationReceipt(
//                                     decrypted,
//                                     decrypted.getPaymentId(),
//                                     decrypted.getAmount()
//                             );
//                         } else if (decrypted.getSubscriptionId() != null && decrypted.getPaymentId() == null) {
//                             // Mandate confirmation (subscription created but no payment yet)
//                             pdf = pdfReceiptService.generateMandateConfirmation(decrypted);
//                         } else {
//                             // Monthly debit / subscription payment
//                             pdf = pdfReceiptService.generateMonthlyDebitReceipt(
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


// ----------------------------------------------------------------------------------------------------------
package com.komal.template_backend.controller;

import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import com.komal.template_backend.service.DonationService;
import com.komal.template_backend.service.MailService;
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

    @Autowired
    private PdfReceiptServic pdfReceiptService;

    @Autowired
    private MailService mailService;

    @Autowired
    private DonationRepo donationRepo;

    // --------------------------------------------------------------------
    // ONE-TIME ORDER CREATION
    // --------------------------------------------------------------------

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Donourentity donor) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", donor.getAmount() * 100);
            options.put("currency", "INR");
            options.put("receipt", "receipt_" + System.currentTimeMillis());
            options.put("payment_capture", 1);

            Order order = client.orders.create(options);

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
        try {

            String razorpayOrderId = (String) body.get("razorpay_order_id");
            String razorpayPaymentId = (String) body.get("razorpay_payment_id");
            String razorpaySignature = (String) body.get("razorpay_signature");

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing required fields"
                ));
            }

            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = hmacSha256(payload, keySecret);

            if (!generatedSignature.equals(razorpaySignature)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid signature"
                ));
            }

            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            com.razorpay.Payment payment = client.payments.fetch(razorpayPaymentId);
            JSONObject paymentJson = payment.toJson();

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
                donor.setUpiId(vpa);
                donor.setWallet(wallet);
                donor.setPaymentInfo(bank.isEmpty() ? vpa : bank);
                donor.setPayerEmail(payerEmail);
                donor.setPayerContact(payerContact);
                donor.setAmount(amount / 100.0);
                donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

                donationService.saveDonation(donor);

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
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", status,
                    "amount", amount / 100.0
            ));
        } catch (Exception e) {
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
//     private String hmacSha256Hex(String data, String secret) throws Exception {
//     Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//     SecretKeySpec key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
//     sha256_HMAC.init(key);
//     byte[] hash = sha256_HMAC.doFinal(data.getBytes("UTF-8"));

//     StringBuilder hex = new StringBuilder();
//     for (byte b : hash) {
//         String h = Integer.toHexString(0xff & b);
//         if (h.length() == 1) hex.append('0');
//         hex.append(h);
//     }
//     return hex.toString();
// }


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
        try {
            donor.setStatus("PENDING");
            donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            Donourentity saved = donationService.saveDonation(donor);
            return ResponseEntity.ok(Map.of("success", true, "donorId", saved.getId()));
        } catch (Exception e) {
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

    private long getNextStartDate(int startDay) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime next;

        int today = now.getDayOfMonth();
        if (today < startDay) {
            next = now.withDayOfMonth(startDay).withHour(0).withMinute(0).withSecond(0);
        } else {
            next = now.plusMonths(1).withDayOfMonth(startDay).withHour(0).withMinute(0).withSecond(0);
        }

        return next.atZone(ZoneId.of("Asia/Kolkata")).toEpochSecond();
    }

    // --------------------------------------------------------------------
    // SUBSCRIPTION CREATE (CLEAN VERSION)
    // --------------------------------------------------------------------

    // @PostMapping("/create-subscription")
    // public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {
    //     System.out.println("====================== CREATE SUBSCRIPTION START ======================");
    //     System.out.println("Received: " + req);

    //     try {
    //         String donorId = String.valueOf(req.get("donorId"));
    //         int amount = parseIntSafe(req.get("amount"), 0);

    //         if (amount <= 0) {
    //             return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid amount"));
    //         }

    //         int starterAmount = parseIntSafe(req.get("starterAmount"), 10);
    //         if (starterAmount < 1 || starterAmount > 28) {
    //             starterAmount = 10;
    //         }

    //         Donourentity donor = donationRepo.findById(donorId)
    //                 .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

    //         RazorpayClient client = new RazorpayClient(keyId, keySecret);

    //         // -------- PLAN REQUEST USING MAPS ONLY --------

    //         Map<String, Object> item = Map.of(
    //                 "name", "Monthly Donation",
    //                 "amount", amount * 100,
    //                 "currency", "INR"
    //         );

    //         Map<String, Object> planRequest = Map.of(
    //                 "period", "monthly",
    //                 "interval", 1,
    //                 "item", item
    //         );

    //         Plan plan = client.plans.create(new JSONObject(planRequest));

    //         donor.setPlanId(plan.get("id"));
    //         donationRepo.save(donor);

    //         // -------- SUBSCRIPTION REQUEST --------

    //         Map<String, Object> addon1 = Map.of(
    //                 "item", Map.of(
    //                         "name", "Auth 1 Rupee",
    //                         "amount", 100,
    //                         "currency", "INR"
    //                 )
    //         );

    //         Map<String, Object> addon2 = Map.of(
    //                 "item", Map.of(
    //                         "name", "First Debit",
    //                         "amount", starterAmount * 100,
    //                         "currency", "INR"
    //                 )
    //         );

    //         Map<String, Object> notes = Map.of(
    //                 "donorId", donorId,
    //                 "monthlyAmount", String.valueOf(amount)
    //         );

    //         Map<String, Object> subscriptionRequest = new HashMap<>();
    //         subscriptionRequest.put("plan_id", plan.get("id"));
    //         subscriptionRequest.put("customer_notify", 1);
    //         subscriptionRequest.put("total_count", subscriptionYears * 12);
    //         subscriptionRequest.put("addons", List.of(addon1, addon2));
    //         subscriptionRequest.put("notes", notes);

    //         if (donor.getStartDay() != null) {
    //             subscriptionRequest.put("start_at", getNextStartDate(donor.getStartDay()));
    //         }

    //         Subscription subscription = client.subscriptions.create(new JSONObject(subscriptionRequest));

    //         donor.setSubscriptionId(subscription.get("id"));
    //         donor.setSubscriptionStatus("CREATED");
    //         donor.setMonthlyAmount((double) amount);
    //         donor.setStartDay(starterAmount);
    //         donationRepo.save(donor);

    //         return ResponseEntity.ok(Map.of(
    //                 "success", true,
    //                 "subscription_id", subscription.get("id"),
    //                 "plan_id", plan.get("id"),
    //                 "keyId", keyId
    //         ));

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return ResponseEntity.status(500)
    //                 .body(Map.of("success", false, "message", e.getMessage()));
    //     }
    // }
@PostMapping("/create-subscription")
public ResponseEntity<?> createSubscription(@RequestBody Map<String, Object> req) {
    System.out.println("====================== CREATE SUBSCRIPTION START ======================");
    System.out.println("Received: " + req);

    try {
        String donorId = String.valueOf(req.get("donorId"));
        int amount = parseIntSafe(req.get("amount"), 0);

        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid amount"));
        }

        // starterAmount is the day-of-month the user wants the first debit (or fallback)
        int starterAmount = parseIntSafe(req.get("starterAmount"), 10);
        if (starterAmount < 1 || starterAmount > 28) {
            starterAmount = 10;
        }

        Donourentity donor = donationRepo.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        // -------- PLAN REQUEST USING MAPS ONLY --------
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

        Plan plan = client.plans.create(new JSONObject(planRequest));
        donor.setPlanId(plan.get("id"));
        donationRepo.save(donor);

        // -------- SUBSCRIPTION REQUEST --------
        // Only include the ₹1 authorization addon.
        Map<String, Object> authAddon = Map.of(
                "item", Map.of(
                        "name", "Mandate Authorization",
                        "amount", 100,            // ₹1 in paise
                        "currency", "INR"
                )
        );

        Map<String, Object> notes = Map.of(
                "donorId", donorId,
                "monthlyAmount", String.valueOf(amount)
        );

        Map<String, Object> subscriptionRequest = new HashMap<>();
        subscriptionRequest.put("plan_id", plan.get("id"));
        subscriptionRequest.put("customer_notify", 1);
      System.out.println("Subscription yeasrs"+subscriptionYears);
        subscriptionRequest.put("total_count", (subscriptionYears * 12)-1 );
        // ONLY the ₹1 auth addon here — do NOT add starterAmount as addon
        subscriptionRequest.put("addons", List.of(authAddon));
        subscriptionRequest.put("notes", notes);

        // Set start_at so Razorpay charges the first real debit on that date.
        // If donor has a stored start day, use it; otherwise use the starterAmount provided in request.
        Integer useStartDay = donor.getStartDay() != null ? donor.getStartDay() : starterAmount;
        if (useStartDay != null) {
            subscriptionRequest.put("start_at", getNextStartDate(useStartDay));
        }

        System.out.println("📦 Subscription Payload: " + new JSONObject(subscriptionRequest).toString(2));

        Subscription subscription = client.subscriptions.create(new JSONObject(subscriptionRequest));

        donor.setSubscriptionId(subscription.get("id"));
        donor.setSubscriptionStatus("CREATED");
        donor.setMonthlyAmount((double) amount);
        donor.setStartDay(useStartDay);
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

//     // --------------------------------------------------------------------
//     // SUBSCRIPTION VERIFY
//     // --------------------------------------------------------------------
@PostMapping("/verify-subscription")
public ResponseEntity<?> verifySubscription(@RequestBody Map<String, Object> req) {

    System.out.println("====================== VERIFY SUBSCRIPTION ======================");
    System.out.println("Received: " + req);

    try {
        String subId = (String) req.get("razorpay_subscription_id");
        String sig = (String) req.get("razorpay_signature");
        String payId = (String) req.get("razorpay_payment_id");

        if (subId == null || sig == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "msg", "Missing fields"));
        }

        // Razorpay verification expects Map<String, String>
        Map<String, String> data = new HashMap<>();
        data.put("razorpay_subscription_id", subId);

        if (payId != null && !payId.isBlank()) {
            data.put("razorpay_payment_id", payId);
        } else {
            data.put("razorpay_payment_id", "created");
        }

        data.put("razorpay_signature", sig);

        // ✔ FIX: Do NOT wrap in JSONObject
        boolean isValid = Utils.verifyPaymentSignature(data, keySecret);

        if (!isValid) {
            System.out.println("❌ INVALID SIGNATURE");
            return ResponseEntity.status(400)
                    .body(Map.of("success", false, "msg", "Invalid signature"));
        }

        // Update donor record
        Donourentity donor = donationRepo.findBySubscriptionId(subId).orElse(null);
        if (donor != null) {
            donor.setSubscriptionStatus("ACTIVE");
            donor.setStatus("SUCCESS");
            donor.setPaymentId(payId); // may be null
            donationRepo.save(donor);
        }

        return ResponseEntity.ok(Map.of("success", true));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(Map.of("success", false, "error", e.getMessage()));
    }
}


    // @PostMapping("/verify-subscription")
// public ResponseEntity<?> verifySubscription(@RequestBody Map<String, Object> req) {

//     String subId = (String) req.get("razorpay_subscription_id");
//     String sig = (String) req.get("razorpay_signature");
//     String payId = (String) req.get("razorpay_payment_id");

//     if (subId == null || sig == null) {
//         return ResponseEntity.badRequest().body(Map.of("success", false, "msg", "Missing fields"));
//     }

//     String payload = (payId != null && !payId.isBlank())
//             ? subId + "|" + payId
//             : subId + "|created";

//     Map<String, String> data = new HashMap<>();
//     data.put("razorpay_subscription_id", subId);
//     if (payId != null && !payId.isBlank())
//         data.put("razorpay_payment_id", payId);
//     else
//         data.put("razorpay_payment_id", "created");

//     data.put("razorpay_signature", sig);

//     boolean isValid = Utils.verifyPaymentSignature(data, keySecret);

//     if (!isValid) {
//         return ResponseEntity.status(400).body(Map.of("success", false, "msg", "Invalid signature"));
//     }

//     // Update donor
//     Donourentity donor = donationRepo.findBySubscriptionId(subId).orElse(null);
//     if (donor != null) {
//         donor.setSubscriptionStatus("ACTIVE");
//         donor.setStatus("SUCCESS");
//         donor.setPaymentId(payId); // may be null
//         donationRepo.save(donor);
//     }

//     return ResponseEntity.ok(Map.of("success", true));
// }

//     @PostMapping("/verify-subscription")
// public ResponseEntity<?> verifySubscription(@RequestBody Map<String, Object> req) {
//     System.out.println("====================== VERIFY SUBSCRIPTION ======================");
//     System.out.println("Received: " + req);

//     try {
//         String subId = (String) req.get("razorpay_subscription_id");
//         String sig = (String) req.get("razorpay_signature");
//         String payId = (String) req.get("razorpay_payment_id");  // may be null

//         if (subId == null || sig == null) {
//             return ResponseEntity.badRequest().body("Missing required fields");
//         }

//         String payload;

//         if (payId != null && !payId.isBlank()) {
//             // CASE 1: AUTODEBIT ₹1 VERIFIED
//             payload = subId + "|" + payId;
//         } else {
//             // CASE 2: NO PAYMENT_ID → mandate created with NO initial debit
//             payload = subId + "|created";
//         }

//         String expectedSignature = hmacSha256(payload, keySecret);

//         if (!expectedSignature.equals(sig)) {
//             System.out.println("❌ INVALID SIGNATURE\nExpected= " + expectedSignature + "\nGot= " + sig);
//             return ResponseEntity.status(400).body("Invalid signature");
//         }

//         // Update donor status
//         Donourentity donor = donationRepo.findBySubscriptionId(subId).orElse(null);
//         if (donor != null) {
//             donor.setSubscriptionStatus("ACTIVE");
//             donor.setStatus("Success");
//             donor.setPaymentId(payId);   // may remain null → correct for UPI autopay
//             donationRepo.save(donor);
//         }

//         return ResponseEntity.ok(Map.of("success", true));

//     } catch (Exception e) {
//         return ResponseEntity.status(500).body("error");
//     }
// }

// -----------------------------------------------------------------------------------
    // @PostMapping("/verify-subscription")
    // public ResponseEntity<?> verifySubscription(@RequestBody Map<String, Object> req) {
    //     System.out.println("====================== VERIFY SUBSCRIPTION ======================");
    //     System.out.println("Received: " + req);

    //     try {
    //         String subId = (String) req.get("razorpay_subscription_id");
    //         String payId = (String) req.get("razorpay_payment_id");
    //         String sig = (String) req.get("razorpay_signature");

    //         if (subId == null || payId == null || sig == null) {
    //             return ResponseEntity.badRequest().body("Missing required fields");
    //         }

    //         String payload = subId + "|" + payId;
    //         String generatedSig = hmacSha256(payload, keySecret);

    //         if (!generatedSig.equals(sig)) {
    //             return ResponseEntity.badRequest().body("Invalid signature");
    //         }

    //         Donourentity donor = donationRepo.findBySubscriptionId(subId).orElse(null);
    //         if (donor != null) {
    //             donor.setPaymentId(payId);
    //             donor.setSubscriptionStatus("ACTIVE");
    //             donor.setStatus("Success");
    //             donationRepo.save(donor);
    //         }

    //         return ResponseEntity.ok(Map.of("success", true));

    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("error");
    //     }
    // }

    // --------------------------------------------------------------------
    // WEBHOOK HANDLER
    // --------------------------------------------------------------------

    @PostMapping("/razorpay-webhook")
    public ResponseEntity<?> webhook(@RequestBody String payload,
                                     @RequestHeader("X-Razorpay-Signature") String signature) {

        System.out.println("====================== WEBHOOK RECEIVED ======================");
        System.out.println(payload);

        try {
            boolean valid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!valid) {
                return ResponseEntity.status(400).body("Invalid signature");
            }

            JSONObject json = new JSONObject(payload);
            String event = json.optString("event");

            // Mandate authorized
            if ("mandate.authorized".equals(event)) {
                JSONObject m = json.getJSONObject("payload")
                        .getJSONObject("mandate")
                        .getJSONObject("entity");

                String subscriptionId = m.getString("subscription_id");
                String mandateId = m.getString("id");

                Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);
                if (donor != null) {
                    donor.setRazorpayMandateId(mandateId);
                    donor.setMandateStatus("AUTHORIZED");
                    donationRepo.save(donor);
                }
            }

            // Subscription activated
            if ("subscription.activated".equals(event)) {
                JSONObject sub = json.getJSONObject("payload")
                        .getJSONObject("subscription")
                        .getJSONObject("entity");

                String subscriptionId = sub.getString("id");

                Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);
                if (donor != null) {
                    donor.setSubscriptionStatus("ACTIVE");
                    donationRepo.save(donor);
                }
            }

            // Subscription charged (monthly debit)
            if ("subscription.charged".equals(event)) {
                JSONObject p = json.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String subscriptionId = p.getString("subscription_id");
                String paymentId = p.getString("id");
                double amountPaid = p.getInt("amount") / 100.0;

                Donourentity donor = donationRepo.findBySubscriptionId(subscriptionId).orElse(null);
                if (donor != null) {
                    Donourentity monthly = new Donourentity();
                    monthly.setFirstName(donor.getFirstName());
                    monthly.setLastName(donor.getLastName());
                    monthly.setEmail(donor.getEmail());
                    monthly.setAmount(amountPaid);
                    monthly.setSubscriptionId(subscriptionId);
                    monthly.setPaymentId(paymentId);
                    monthly.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    donationService.saveDonation(monthly);
                }
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("error");
        }
    }
}





































































































































