
// // -----------------------------------------------------------------------------------------------------
// package com.komal.template_backend.service;

// import com.komal.template_backend.Util.AESUtil;
// import com.komal.template_backend.Util.MaskingUtil;
// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import javax.crypto.spec.SecretKeySpec;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.List;

// @Service
// public class DonationService {

//     private final DonationRepo donationRepo;

//     @Autowired
//     private PdfReceiptServic pdfReceiptService;

//     @Autowired
//     private MailService mailService;

//     public DonationService(DonationRepo donationRepo) {
//         this.donationRepo = donationRepo;
//     }

//     // =====================================================================================
//     // SAVE DONATION (for create-order, verify, subscription-charged, mandate-authorized)
//     // =====================================================================================
//     public Donourentity saveDonation(Donourentity donor) throws Exception {

//         // Always save in IST
//         if (donor.getDonationDate() == null) {
//             donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//         }

//         // Only search if orderId is present
//         Donourentity existing = null;
//         if (donor.getOrderId() != null) {
//             existing = donationRepo.findByOrderId(donor.getOrderId()).orElse(null);
//         }

//         // ===========================================
//         // UPDATE EXISTING (VERIFY / WEBHOOK)
//         // ===========================================
//         if (existing != null) {

//             System.out.println("🟡 Updating existing donor - encryption preserved");

//             SecretKeySpec key = AESUtil.deobfuscateKey(existing.getEncKey());

//             existing.setPaymentId(donor.getPaymentId());
//             existing.setSignature(donor.getSignature());
//             existing.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");

//             // Subscription / mandate extra fields
//             if (donor.getSubscriptionId() != null) existing.setSubscriptionId(donor.getSubscriptionId());
//             if (donor.getSubscriptionStatus() != null) existing.setSubscriptionStatus(donor.getSubscriptionStatus());
//             if (donor.getRazorpayMandateId() != null) existing.setRazorpayMandateId(donor.getRazorpayMandateId());
//             if (donor.getMandateStartDate() != null) existing.setMandateStartDate(donor.getMandateStartDate());
//             if (donor.getMandateEndDate() != null) existing.setMandateEndDate(donor.getMandateEndDate());
//             if (donor.getMandateStatus() != null) existing.setMandateStatus(donor.getMandateStatus());
//             if (donor.getMandateAmount() != null) existing.setMandateAmount(donor.getMandateAmount());
//             if (donor.getMandateFrequency() != null) existing.setMandateFrequency(donor.getMandateFrequency());
//             if (donor.getMonthlyAmount() != null) existing.setMonthlyAmount(donor.getMonthlyAmount());
//             if (donor.getDonorMandateRefId() != null) existing.setDonorMandateRefId(donor.getDonorMandateRefId());

//             // Encrypted Razorpay fields
//             if (donor.getPaymentMethod() != null)
//                 existing.setPaymentMethod(AESUtil.encryptIfNotNull(donor.getPaymentMethod(), key));

//             if (donor.getWallet() != null)
//                 existing.setWallet(AESUtil.encryptIfNotNull(donor.getWallet(), key));

//             if (donor.getUpiId() != null)
//                 existing.setUpiId(AESUtil.encryptIfNotNull(donor.getUpiId(), key));

//             if (donor.getPayerEmail() != null)
//                 existing.setPayerEmail(AESUtil.encryptIfNotNull(donor.getPayerEmail(), key));

//             if (donor.getPayerContact() != null)
//                 existing.setPayerContact(AESUtil.encryptIfNotNull(donor.getPayerContact(), key));

//             if (donor.getPaymentInfo() != null)
//                 existing.setPaymentInfo(AESUtil.encryptIfNotNull(donor.getPaymentInfo(), key));

//             return donationRepo.save(existing);
//         }

//         // ===========================================
//         // NEW DONOR (ENCRYPT EVERYTHING)
//         // ===========================================
//         System.out.println("🟢 New donor — performing encryption");

//         String salt = AESUtil.generateSalt();
//         donor.setEncSalt(salt);
//         donor.setEncMonth(donor.getDonationDate().getMonthValue());

//         // Generate key using identity + date + salt
//         SecretKeySpec key = AESUtil.generateKey(
//                 donor.getMobile() != null ? donor.getMobile() : "",
//                 donor.getUniqueId() != null ? donor.getUniqueId() : "",
//                 donor.getDob() != null ? donor.getDob() : "",
//                 donor.getDonationDate(),
//                 salt
//         );

//         donor.setEncKey(AESUtil.obfuscateKey(key));

//         // Encrypt sensitive fields
//         donor.setEmail(AESUtil.encryptIfNotNull(donor.getEmail(), key));
//         donor.setMobile(AESUtil.encryptIfNotNull(donor.getMobile(), key));
//         donor.setUniqueId(AESUtil.encryptIfNotNull(donor.getUniqueId(), key));
//         // donor.setBankName(AESUtil.encryptIfNotNull(donor.getBankName(), key));
//         // donor.setIfsc(AESUtil.encryptIfNotNull(donor.getIfsc(), key));
//         // donor.setAccountNumber(AESUtil.encryptIfNotNull(donor.getAccountNumber(), key));

//         donor.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");
//                // 🔥 Auto-generate receipt only after successful payment
// if ("SUCCESS".equalsIgnoreCase(existing.getStatus())) {

//     // Set receipt type
//     if (existing.getSubscriptionId() != null) {
//         existing.setReceiptType("SUBSCRIPTION");
//     } else {
//         existing.setReceiptType("ONE_TIME");
//     }

//     // Generate receipt number only if empty (avoid overriding)
//     if (existing.getInvoiceNumber() == null || existing.getInvoiceNumber().isBlank()) {
//         String receipt = generateReceiptNumber(existing);
//         existing.setInvoiceNumber(receipt);
//     }
// }


//         return donationRepo.save(donor);
//     }

//     // =====================================================================================
//     // SAFE DECRYPT UTIL
//     // =====================================================================================
//     private String safeDecrypt(String data, SecretKeySpec key) {
//         try { if (data != null && !data.isEmpty()) return AESUtil.decrypt(data, key); }
//         catch (Exception ignored) {}
//         return null;
//     }

//     // =====================================================================================
//     // FETCH ALL DONORS (DECRYPT + MASK)
//     // =====================================================================================
//     public List<Donourentity> getAllDonors() {
//         List<Donourentity> donors = donationRepo.findAll();

//         donors.forEach(d -> {
//             try {
//                 SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

//                 d.setEmail(safeDecrypt(d.getEmail(), key));
//                 d.setMobile(safeDecrypt(d.getMobile(), key));
//                 // d.setUniqueId(safeDecrypt(d.getUniqueId(), key));
//                 // d.setBankName(safeDecrypt(d.getBankName(), key));
//                 // d.setIfsc(MaskingUtil.maskIfsc(safeDecrypt(d.getIfsc(), key)));
//                 // d.setAccountNumber(MaskingUtil.maskAccountNumber(safeDecrypt(d.getAccountNumber(), key)));
//                 d.setPaymentMethod(safeDecrypt(d.getPaymentMethod(), key));
//                 d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
//                 d.setUpiId(safeDecrypt(d.getUpiId(), key));
//                 d.setWallet(safeDecrypt(d.getWallet(), key));
//                 d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
//                 d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

//             } catch (Exception ignored) {}
//         });

//         return donors;
//     }

//     // =====================================================================================
//     // UPDATE DONOR (ADMIN PANEL)
//     // =====================================================================================
//     private void updateEncryptedField(java.util.function.Consumer<String> setter,
//                                       String newValue,
//                                       SecretKeySpec key) throws Exception {
//         if (newValue != null && !newValue.isBlank()) {
//             setter.accept(AESUtil.encrypt(newValue, key));
//         }
//     }

//     public Donourentity updateDonor(String id, Donourentity updated) throws Exception {

//         Donourentity existing = donationRepo.findById(id).orElseThrow(() -> new Exception("Donor not found"));
//         SecretKeySpec key = AESUtil.deobfuscateKey(existing.getEncKey());

//         // Plain editable fields
//         if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
//         if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
//         if (updated.getDob() != null) existing.setDob(updated.getDob());
//         if (updated.getFrequency() != null) existing.setFrequency(updated.getFrequency());
//         // if (updated.getPaymentMode() != null) existing.setPaymentMode(updated.getPaymentMode());
//         if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
//         if (updated.getDeclaration() != null) existing.setDeclaration(updated.getDeclaration());
//         if (updated.getCurrency() != null) existing.setCurrency(updated.getCurrency());

//         // Razorpay fields (ENCRYPT THESE)
//         updateEncryptedField(existing::setPaymentMethod, updated.getPaymentMethod(), key);
//         updateEncryptedField(existing::setWallet, updated.getWallet(), key);
//         updateEncryptedField(existing::setUpiId, updated.getUpiId(), key);
//         updateEncryptedField(existing::setPayerEmail, updated.getPayerEmail(), key);
//         updateEncryptedField(existing::setPayerContact, updated.getPayerContact(), key);
//         updateEncryptedField(existing::setPaymentInfo, updated.getPaymentInfo(), key);

//         // Non-encrypted razorpay ids/tokens
//         if (updated.getPaymentId() != null) existing.setPaymentId(updated.getPaymentId());
//         if (updated.getOrderId() != null) existing.setOrderId(updated.getOrderId());
//         if (updated.getSignature() != null) existing.setSignature(updated.getSignature());

//         // Subscription updates
//         if (updated.getSubscriptionId() != null) existing.setSubscriptionId(updated.getSubscriptionId());
//         if (updated.getSubscriptionStatus() != null) existing.setSubscriptionStatus(updated.getSubscriptionStatus());
//         if (updated.getMandateId() != null) existing.setMandateId(updated.getMandateId());
//         if (updated.getMonthlyAmount() != null) existing.setMonthlyAmount(updated.getMonthlyAmount());
//         if (updated.getRazorpayCustomerId() != null) existing.setRazorpayCustomerId(updated.getRazorpayCustomerId());

//         // Encrypted identity fields
//         updateEncryptedField(existing::setEmail, updated.getEmail(), key);
//         updateEncryptedField(existing::setMobile, updated.getMobile(), key);
//         updateEncryptedField(existing::setUniqueId, updated.getUniqueId(), key);
//         // updateEncryptedField(existing::setBankName, updated.getBankName(), key);
//         // updateEncryptedField(existing::setIfsc, updated.getIfsc(), key);
//         // updateEncryptedField(existing::setAccountNumber, updated.getAccountNumber(), key);

//         return donationRepo.save(existing);
//     }

//     // =====================================================================================
//     // DECRYPT BY ID
//     // =====================================================================================
//     public Donourentity findByIdDecrypt(String id) {
//         Donourentity d = donationRepo.findById(id).orElse(null);
//         if (d == null) return null;

//         try {
//             SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

//             d.setEmail(safeDecrypt(d.getEmail(), key));
//             d.setMobile(safeDecrypt(d.getMobile(), key));
//             d.setUniqueId(safeDecrypt(d.getUniqueId(), key));
//             // d.setBankName(safeDecrypt(d.getBankName(), key));
//             // d.setIfsc(safeDecrypt(d.getIfsc(), key));
//             // d.setAccountNumber(safeDecrypt(d.getAccountNumber(), key));
//             d.setPaymentMethod(safeDecrypt(d.getPaymentMethod(), key));
//             d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
//             d.setUpiId(safeDecrypt(d.getUpiId(), key));
//             d.setWallet(safeDecrypt(d.getWallet(), key));
//             d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
//             d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

//         } catch (Exception ignored) {}

//         return d;
//     }
//            // Delete donor by ID (used in DonationController)
// public void deleteById(String id) {
//     donationRepo.deleteById(id);
// }
//            private String generateReceiptNumber(Donourentity donor) {
//     // Date part
//     String datePart = donor.getDonationDate()
//             .toLocalDate()
//             .toString()             // "2025-02-18"
//             .replaceAll("-", "");   // "20250218"

//     // ID last 4 chars (safe fallback)
//     String idPart = "0000";
//     if (donor.getId() != null && donor.getId().length() >= 4) {
//         idPart = donor.getId().substring(donor.getId().length() - 4).toUpperCase();
//     }

//     return "FTH-" + datePart + "-" + idPart;
// }


// }

// -----------------------------------------------------------------------------------------------------
package com.komal.template_backend.service;

import com.komal.template_backend.Util.AESUtil;
import com.komal.template_backend.Util.MaskingUtil;
import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DonationService {

    private final DonationRepo donationRepo;

    @Autowired
    private PdfReceiptServic pdfReceiptService;

    @Autowired
    private MailService mailService;

    public DonationService(DonationRepo donationRepo) {
        this.donationRepo = donationRepo;
    }

    // =====================================================================================
    // SAVE DONATION (for create-order, verify, subscription-charged, mandate-authorized)
    // =====================================================================================
    public Donourentity saveDonation(Donourentity donor) throws Exception {

        // Always save in IST
        if (donor.getDonationDate() == null) {
            donor.setDonationDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        }

        // Only search if orderId is present
        Donourentity existing = null;
        if (donor.getOrderId() != null) {
            existing = donationRepo.findByOrderId(donor.getOrderId()).orElse(null);
        }

        // ===========================================
        // UPDATE EXISTING (VERIFY / WEBHOOK)
        // ===========================================
        if (existing != null) {

            System.out.println("🟡 Updating existing donor - encryption preserved");

            SecretKeySpec key = AESUtil.deobfuscateKey(existing.getEncKey());

            existing.setPaymentId(donor.getPaymentId());
            existing.setSignature(donor.getSignature());
            existing.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");

            // Subscription / mandate extra fields
            if (donor.getSubscriptionId() != null) existing.setSubscriptionId(donor.getSubscriptionId());
            if (donor.getSubscriptionStatus() != null) existing.setSubscriptionStatus(donor.getSubscriptionStatus());
            if (donor.getRazorpayMandateId() != null) existing.setRazorpayMandateId(donor.getRazorpayMandateId());
            if (donor.getMandateStartDate() != null) existing.setMandateStartDate(donor.getMandateStartDate());
            if (donor.getMandateEndDate() != null) existing.setMandateEndDate(donor.getMandateEndDate());
            if (donor.getMandateStatus() != null) existing.setMandateStatus(donor.getMandateStatus());
            if (donor.getMandateAmount() != null) existing.setMandateAmount(donor.getMandateAmount());
            if (donor.getMandateFrequency() != null) existing.setMandateFrequency(donor.getMandateFrequency());
            if (donor.getMonthlyAmount() != null) existing.setMonthlyAmount(donor.getMonthlyAmount());
            if (donor.getDonorMandateRefId() != null) existing.setDonorMandateRefId(donor.getDonorMandateRefId());

            // Encrypted Razorpay fields
            if (donor.getPaymentMethod() != null)
                existing.setPaymentMethod(AESUtil.encryptIfNotNull(donor.getPaymentMethod(), key));

            if (donor.getWallet() != null)
                existing.setWallet(AESUtil.encryptIfNotNull(donor.getWallet(), key));

            if (donor.getUpiId() != null)
                existing.setUpiId(AESUtil.encryptIfNotNull(donor.getUpiId(), key));

            if (donor.getPayerEmail() != null)
                existing.setPayerEmail(AESUtil.encryptIfNotNull(donor.getPayerEmail(), key));

            if (donor.getPayerContact() != null)
                existing.setPayerContact(AESUtil.encryptIfNotNull(donor.getPayerContact(), key));

            if (donor.getPaymentInfo() != null)
                existing.setPaymentInfo(AESUtil.encryptIfNotNull(donor.getPaymentInfo(), key));

            return donationRepo.save(existing);
        }

        // ===========================================
        // NEW DONOR (ENCRYPT EVERYTHING)
        // ===========================================
        System.out.println("🟢 New donor — performing encryption");

        String salt = AESUtil.generateSalt();
        donor.setEncSalt(salt);
        donor.setEncMonth(donor.getDonationDate().getMonthValue());
donor.setStartDay(donor.getStartDay());

        // Generate key using identity + date + salt
        SecretKeySpec key = AESUtil.generateKey(
                donor.getMobile() != null ? donor.getMobile() : "",
                donor.getUniqueId() != null ? donor.getUniqueId() : "",
                donor.getDob() != null ? donor.getDob() : "",
                donor.getDonationDate(),
                salt
        );

        donor.setEncKey(AESUtil.obfuscateKey(key));

        // Encrypt sensitive fields
        donor.setEmail(AESUtil.encryptIfNotNull(donor.getEmail(), key));
        donor.setMobile(AESUtil.encryptIfNotNull(donor.getMobile(), key));
        donor.setUniqueId(AESUtil.encryptIfNotNull(donor.getUniqueId(), key));
        // donor.setBankName(AESUtil.encryptIfNotNull(donor.getBankName(), key));
        // donor.setIfsc(AESUtil.encryptIfNotNull(donor.getIfsc(), key));
        // donor.setAccountNumber(AESUtil.encryptIfNotNull(donor.getAccountNumber(), key));

        donor.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");
               // 🔥 Auto-generate receipt only after successful payment
if ("SUCCESS".equalsIgnoreCase(existing.getStatus())) {

    // Set receipt type
    if (existing.getSubscriptionId() != null) {
        existing.setReceiptType("SUBSCRIPTION");
    } else {
        existing.setReceiptType("ONE_TIME");
    }

    // Generate receipt number only if empty (avoid overriding)
    if (existing.getInvoiceNumber() == null || existing.getInvoiceNumber().isBlank()) {
        String receipt = generateReceiptNumber(existing);
        existing.setInvoiceNumber(receipt);
    }
}


        return donationRepo.save(donor);
    }

    // =====================================================================================
    // SAFE DECRYPT UTIL
    // =====================================================================================
    private String safeDecrypt(String data, SecretKeySpec key) {
        try { if (data != null && !data.isEmpty()) return AESUtil.decrypt(data, key); }
        catch (Exception ignored) {}
        return null;
    }

    // =====================================================================================
    // FETCH ALL DONORS (DECRYPT + MASK)
    // =====================================================================================
    public List<Donourentity> getAllDonors() {
        List<Donourentity> donors = donationRepo.findAll();

        donors.forEach(d -> {
            try {
                SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

                d.setEmail(safeDecrypt(d.getEmail(), key));
                d.setMobile(safeDecrypt(d.getMobile(), key));

                d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
                d.setUpiId(safeDecrypt(d.getUpiId(), key));
                d.setWallet(safeDecrypt(d.getWallet(), key));
                d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
                d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

            } catch (Exception ignored) {}
        });

        return donors;
    }

    // =====================================================================================
    // UPDATE DONOR (ADMIN PANEL)
    // =====================================================================================
    private void updateEncryptedField(java.util.function.Consumer<String> setter,
                                      String newValue,
                                      SecretKeySpec key) throws Exception {
        if (newValue != null && !newValue.isBlank()) {
            setter.accept(AESUtil.encrypt(newValue, key));
        }
    }

    public Donourentity updateDonor(String id, Donourentity updated) throws Exception {

        Donourentity existing = donationRepo.findById(id).orElseThrow(() -> new Exception("Donor not found"));
        SecretKeySpec key = AESUtil.deobfuscateKey(existing.getEncKey());

        // Plain editable fields
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getDob() != null) existing.setDob(updated.getDob());
        if (updated.getFrequency() != null) existing.setFrequency(updated.getFrequency());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getDeclaration() != null) existing.setDeclaration(updated.getDeclaration());
        if (updated.getCurrency() != null) existing.setCurrency(updated.getCurrency());

        // Razorpay fields (ENCRYPT THESE)
        updateEncryptedField(existing::setPaymentMethod, updated.getPaymentMethod(), key);
        updateEncryptedField(existing::setWallet, updated.getWallet(), key);
        updateEncryptedField(existing::setUpiId, updated.getUpiId(), key);
        updateEncryptedField(existing::setPayerEmail, updated.getPayerEmail(), key);
        updateEncryptedField(existing::setPayerContact, updated.getPayerContact(), key);
        updateEncryptedField(existing::setPaymentInfo, updated.getPaymentInfo(), key);

        // Non-encrypted razorpay ids/tokens
        if (updated.getPaymentId() != null) existing.setPaymentId(updated.getPaymentId());
        if (updated.getOrderId() != null) existing.setOrderId(updated.getOrderId());
        if (updated.getSignature() != null) existing.setSignature(updated.getSignature());

        // Subscription updates
        if (updated.getSubscriptionId() != null) existing.setSubscriptionId(updated.getSubscriptionId());
        if (updated.getSubscriptionStatus() != null) existing.setSubscriptionStatus(updated.getSubscriptionStatus());
        if (updated.getMandateId() != null) existing.setMandateId(updated.getMandateId());
        if (updated.getMonthlyAmount() != null) existing.setMonthlyAmount(updated.getMonthlyAmount());
        if (updated.getRazorpayCustomerId() != null) existing.setRazorpayCustomerId(updated.getRazorpayCustomerId());

        // Encrypted identity fields
        updateEncryptedField(existing::setEmail, updated.getEmail(), key);
        updateEncryptedField(existing::setMobile, updated.getMobile(), key);
        updateEncryptedField(existing::setUniqueId, updated.getUniqueId(), key);
     

        return donationRepo.save(existing);
    }

    // =====================================================================================
    // DECRYPT BY ID
    // =====================================================================================
    public Donourentity findByIdDecrypt(String id) {
        Donourentity d = donationRepo.findById(id).orElse(null);
        if (d == null) return null;

        try {
            SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

            d.setEmail(safeDecrypt(d.getEmail(), key));
            d.setMobile(safeDecrypt(d.getMobile(), key));
            d.setUniqueId(safeDecrypt(d.getUniqueId(), key));
            d.setPaymentMethod(safeDecrypt(d.getPaymentMethod(), key));
            d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
            d.setUpiId(safeDecrypt(d.getUpiId(), key));
            d.setWallet(safeDecrypt(d.getWallet(), key));
            d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
            d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

        } catch (Exception ignored) {}

        return d;
    }
           // Delete donor by ID (used in DonationController)
public void deleteById(String id) {
    donationRepo.deleteById(id);
}
           private String generateReceiptNumber(Donourentity donor) {
    // Date part
    String datePart = donor.getDonationDate()
            .toLocalDate()
            .toString()             // "2025-02-18"
            .replaceAll("-", "");   // "20250218"

    // ID last 4 chars (safe fallback)
    String idPart = "0000";
    if (donor.getId() != null && donor.getId().length() >= 4) {
        idPart = donor.getId().substring(donor.getId().length() - 4).toUpperCase();
    }

    return "FTH-" + datePart + "-" + idPart;
}


}


