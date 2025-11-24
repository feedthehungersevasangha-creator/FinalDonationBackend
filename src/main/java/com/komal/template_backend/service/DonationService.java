
// //--------------------------------------------------------------------------------------------------------
// package com.komal.template_backend.service;

// import com.komal.template_backend.Util.AESUtil;
// import com.komal.template_backend.Util.MaskingUtil;
// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.repo.DonationRepo;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import javax.crypto.spec.SecretKeySpec;
// import java.time.LocalDateTime;
// import java.util.List;
// @Service
// public class DonationService {
//     private final DonationRepo donationRepo;

//     @Autowired
//     private MailService mailService;

//     public DonationService(DonationRepo donationRepo) {
//         this.donationRepo = donationRepo;
//     }

//     public Donourentity saveDonation(Donourentity donor) throws Exception {

//         if (donor.getDonationDate() == null) {
//             donor.setDonationDate(LocalDateTime.now());
//         }


//         // 🔍 Check if this donor already exists (update case)
//         Donourentity existing = donationRepo.findByOrderId(donor.getOrderId()).orElse(null);

//         if (existing != null) {
//             System.out.println("🟡 Updating existing donor — skipping re-encryption");
//             SecretKeySpec key = AESUtil.deobfuscateKey(donor.getEncKey());

//             // just update payment info and status
//             existing.setPaymentId(donor.getPaymentId());
//             existing.setSignature(donor.getSignature());
//             existing.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");
// // ----------------------
// // Update subscription / mandate details (add these inside existing != null block)
// // ----------------------
//             if (donor.getSubscriptionId() != null) {
//                 existing.setSubscriptionId(donor.getSubscriptionId());
//             }

//             if (donor.getSubscriptionStatus() != null) {
//                 existing.setSubscriptionStatus(donor.getSubscriptionStatus());
//             }

//             if (donor.getRazorpayMandateId() != null) {
//                 existing.setRazorpayMandateId(donor.getRazorpayMandateId());
//             }

//             if (donor.getMandateStartDate() != null) {
//                 existing.setMandateStartDate(donor.getMandateStartDate());
//             }

//             if (donor.getMandateEndDate() != null) {
//                 existing.setMandateEndDate(donor.getMandateEndDate());
//             }

//             if (donor.getMandateStatus() != null) {
//                 existing.setMandateStatus(donor.getMandateStatus());
//             }

//             if (donor.getMandateAmount() != null) {
//                 existing.setMandateAmount(donor.getMandateAmount());
//             }

//             if (donor.getMandateFrequency() != null) {
//                 existing.setMandateFrequency(donor.getMandateFrequency());
//             }

// // keep monthly amount if provided
//             if (donor.getMonthlyAmount() != null) {
//                 existing.setMonthlyAmount(donor.getMonthlyAmount());
//             }

// // donor-provided reference id for mandate (if you create one)
//             if (donor.getDonorMandateRefId() != null) {
//                 existing.setDonorMandateRefId(donor.getDonorMandateRefId());
//             }

//             Donourentity updated = donationRepo.save(existing);

//             // send mail after successful payment
//             try {
//                 mailService.sendDonationReceipt(
//                         safeDecrypt(donor.getEmail(),key),
//                         existing.getFirstName() + " " + existing.getLastName(),
//                         existing.getAmount(),
//                         donor.getPaymentId()
//                 );
//             } catch (Exception e) {
//                 System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
//             }

//             return updated;
//         }

//         // 🟢 New donor (first time)
//         System.out.println("🟢 New donor — performing encryption");

//         // Generate salt
//         String salt = AESUtil.generateSalt();
//         donor.setEncSalt(salt);

//         int month = donor.getDonationDate().getMonthValue();
//         donor.setEncMonth(month);

//         // Generate AES key
//         SecretKeySpec key = AESUtil.generateKey(
//                 donor.getMobile() != null ? donor.getMobile() : "",
//                 donor.getUniqueId() != null ? donor.getUniqueId() : "",
//                 donor.getDob() != null ? donor.getDob() : "",
//                 donor.getDonationDate(),
//                 salt
//         );

//         // Store obfuscated key
//         donor.setEncKey(AESUtil.obfuscateKey(key));

//         // Encrypt only non-null fields
//         donor.setEmail(AESUtil.encryptIfNotNull(donor.getEmail(), key));
//         donor.setMobile(AESUtil.encryptIfNotNull(donor.getMobile(), key));
//         donor.setUniqueId(AESUtil.encryptIfNotNull(donor.getUniqueId(), key));
//         donor.setBankName(AESUtil.encryptIfNotNull(donor.getBankName(), key));
//         donor.setIfsc(AESUtil.encryptIfNotNull(donor.getIfsc(), key));
//         donor.setAccountNumber(AESUtil.encryptIfNotNull(donor.getAccountNumber(), key));
//         donor.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");
//         Donourentity savedDonor = donationRepo.save(donor);
//         // send receipt if payment already successful
//         SecretKeySpec key2 = AESUtil.deobfuscateKey(donor.getEncKey());

//         if (donor.getPaymentId() != null) {
//             try {
//                 mailService.sendDonationReceipt(
//                         safeDecrypt(donor.getEmail(),key2),
//                         donor.getFirstName() + " " + donor.getLastName(),
//                         donor.getAmount(),
//                         donor.getPaymentId()
//                 );
//             } catch (Exception e) {
//                 System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
//             }
//         }
//         return savedDonor;
//     }
//     // Safe decryption helper
//     private String safeDecrypt(String data, SecretKeySpec key) {
//         try {
//             if (data != null && !data.isEmpty()) {
//                 return AESUtil.decrypt(data, key);
//             }
//         } catch (Exception e) {
//             System.err.println("Decryption failed: " + e.getMessage());
//         }
//         return null;
//     }

//     // Fetch all donors with decryption & masking
//     public List<Donourentity> getAllDonors() {
//         List<Donourentity> donors = donationRepo.findAll();

//         donors.forEach(d -> {
//             try {
//                 SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

//                 d.setEmail(safeDecrypt(d.getEmail(), key));
//                 d.setMobile(safeDecrypt(d.getMobile(), key));
//                 d.setUniqueId(safeDecrypt(d.getUniqueId(), key));
//                 d.setBankName(safeDecrypt(d.getBankName(), key));
//                 d.setIfsc(MaskingUtil.maskIfsc(safeDecrypt(d.getIfsc(), key)));
//                 d.setAccountNumber(MaskingUtil.maskAccountNumber(safeDecrypt(d.getAccountNumber(), key)));
//                 d.setPaymentMethod(safeDecrypt(d.getPaymentMethod(), key));
//                 d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
//                 d.setUpiId(safeDecrypt(d.getUpiId(), key));
//                 d.setWallet(safeDecrypt(d.getWallet(), key));
//                 d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
//                 d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

//             } catch (Exception e) {
//                 System.err.println("Skipping decryption/masking for record: " + e.getMessage());
//             }
//         });

//         return donors;
//     }
//     public void deleteById(String id) {
//         donationRepo.deleteById(id);
//     }
//     // helper method to update encrypted data
//     private void updateEncryptedField(
//             java.util.function.Consumer<String> setter,
//             String newValue,
//             SecretKeySpec key
//     ) {
//         try {
//             if (newValue != null && !newValue.isBlank()) {
//                 setter.accept(AESUtil.encrypt(newValue, key));
//             }
//         } catch (Exception e) {
//             throw new RuntimeException("Encryption update failed: " + e.getMessage());
//         }
//     }

//     public Donourentity updateDonor(String id, Donourentity updated) throws Exception {

//         Donourentity existing = donationRepo.findById(id).orElse(null);
//         if (existing == null) throw new Exception("Donor not found");

//         // decrypt key for editing encrypted fields
//         SecretKeySpec key = AESUtil.deobfuscateKey(existing.getEncKey());

//         // ------------------ PLAIN FIELDS --------------------
//         if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
//         if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
//         if (updated.getDob() != null) existing.setDob(updated.getDob());
//         if (updated.getFrequency() != null) existing.setFrequency(updated.getFrequency());
//         if (updated.getPaymentMode() != null) existing.setPaymentMode(updated.getPaymentMode());
//         if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
//         if (updated.getDeclaration() != null) existing.setDeclaration(updated.getDeclaration());

//         if (updated.getPaymentMethod() != null) existing.setPaymentMethod(updated.getPaymentMethod());
//         if (updated.getWallet() != null) existing.setWallet(updated.getWallet());
//         if (updated.getUpiId() != null) existing.setUpiId(updated.getUpiId());
//         if (updated.getCurrency() != null) existing.setCurrency(updated.getCurrency());

//         // Razorpay fields
//         if (updated.getPaymentId() != null) existing.setPaymentId(updated.getPaymentId());
//         if (updated.getOrderId() != null) existing.setOrderId(updated.getOrderId());
//         if (updated.getSignature() != null) existing.setSignature(updated.getSignature());

//         // Subscription fields
//         if (updated.getSubscriptionId() != null) existing.setSubscriptionId(updated.getSubscriptionId());
//         if (updated.getSubscriptionStatus() != null) existing.setSubscriptionStatus(updated.getSubscriptionStatus());
//         if (updated.getMandateId() != null) existing.setMandateId(updated.getMandateId());
//         if (updated.getMonthlyAmount() != null) existing.setMonthlyAmount(updated.getMonthlyAmount());
//         if (updated.getPayerEmail() != null) existing.setPayerEmail(updated.getPayerEmail());
//         if (updated.getPayerContact() != null) existing.setPayerContact(updated.getPayerContact());
//         if (updated.getPaymentInfo() != null) existing.setPaymentInfo(updated.getPaymentInfo());
//         if (updated.getRazorpayCustomerId() != null) existing.setRazorpayCustomerId(updated.getRazorpayCustomerId());

//         // ------------------- ENCRYPTED FIELDS -------------------
//         updateEncryptedField(existing::setEmail, updated.getEmail(), key);
//         updateEncryptedField(existing::setMobile, updated.getMobile(), key);
//         updateEncryptedField(existing::setUniqueId, updated.getUniqueId(), key);
//         updateEncryptedField(existing::setBankName, updated.getBankName(), key);
//         updateEncryptedField(existing::setIfsc, updated.getIfsc(), key);
//         updateEncryptedField(existing::setAccountNumber, updated.getAccountNumber(), key);

//         return donationRepo.save(existing);
//     }

// }

//--------------------------------------------------------------------------------------------------------
package com.komal.template_backend.service;

import com.komal.template_backend.Util.AESUtil;
import com.komal.template_backend.Util.MaskingUtil;
import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
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

    public Donourentity saveDonation(Donourentity donor) throws Exception {

        if (donor.getDonationDate() == null) {
            donor.setDonationDate(LocalDateTime.now());
        }


        // 🔍 Check if this donor already exists (update case)
        Donourentity existing = donationRepo.findByOrderId(donor.getOrderId()).orElse(null);

        if (existing != null) {
            System.out.println("🟡 Updating existing donor — skipping re-encryption");
            SecretKeySpec key = AESUtil.deobfuscateKey(donor.getEncKey());

            // just update payment info and status
            existing.setPaymentId(donor.getPaymentId());
            existing.setSignature(donor.getSignature());
            existing.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");
// ----------------------
// Update subscription / mandate details (add these inside existing != null block)
// ----------------------
            if (donor.getSubscriptionId() != null) {
                existing.setSubscriptionId(donor.getSubscriptionId());
            }

            if (donor.getSubscriptionStatus() != null) {
                existing.setSubscriptionStatus(donor.getSubscriptionStatus());
            }

            if (donor.getRazorpayMandateId() != null) {
                existing.setRazorpayMandateId(donor.getRazorpayMandateId());
            }

            if (donor.getMandateStartDate() != null) {
                existing.setMandateStartDate(donor.getMandateStartDate());
            }

            if (donor.getMandateEndDate() != null) {
                existing.setMandateEndDate(donor.getMandateEndDate());
            }

            if (donor.getMandateStatus() != null) {
                existing.setMandateStatus(donor.getMandateStatus());
            }

            if (donor.getMandateAmount() != null) {
                existing.setMandateAmount(donor.getMandateAmount());
            }

            if (donor.getMandateFrequency() != null) {
                existing.setMandateFrequency(donor.getMandateFrequency());
            }

// keep monthly amount if provided
            if (donor.getMonthlyAmount() != null) {
                existing.setMonthlyAmount(donor.getMonthlyAmount());
            }

// donor-provided reference id for mandate (if you create one)
            if (donor.getDonorMandateRefId() != null) {
                existing.setDonorMandateRefId(donor.getDonorMandateRefId());
            }

            Donourentity updated = donationRepo.save(existing);

            // send mail after successful payment
           

            return updated;
        }
        // 🟢 New donor (first time)
        System.out.println("🟢 New donor — performing encryption");

        // Generate salt
        String salt = AESUtil.generateSalt();
        donor.setEncSalt(salt);

        int month = donor.getDonationDate().getMonthValue();
        donor.setEncMonth(month);
        // Generate AES key
        SecretKeySpec key = AESUtil.generateKey(
                donor.getMobile() != null ? donor.getMobile() : "",
                donor.getUniqueId() != null ? donor.getUniqueId() : "",
                donor.getDob() != null ? donor.getDob() : "",
                donor.getDonationDate(),
                salt
        );
        // Store obfuscated key
        donor.setEncKey(AESUtil.obfuscateKey(key));
        // Encrypt only non-null fields
        donor.setEmail(AESUtil.encryptIfNotNull(donor.getEmail(), key));
        donor.setMobile(AESUtil.encryptIfNotNull(donor.getMobile(), key));
        donor.setUniqueId(AESUtil.encryptIfNotNull(donor.getUniqueId(), key));
        donor.setBankName(AESUtil.encryptIfNotNull(donor.getBankName(), key));
        donor.setIfsc(AESUtil.encryptIfNotNull(donor.getIfsc(), key));
        donor.setAccountNumber(AESUtil.encryptIfNotNull(donor.getAccountNumber(), key));
        donor.setStatus(donor.getPaymentId() != null ? "SUCCESS" : "PENDING");
        Donourentity savedDonor = donationRepo.save(donor);
        // send receipt if payment already successful
        SecretKeySpec key2 = AESUtil.deobfuscateKey(donor.getEncKey());

        // if (donor.getPaymentId() != null) {
        //     try {
        //         mailService.sendDonationReceipt(
        //                 safeDecrypt(donor.getEmail(),key2),
        //                 donor.getFirstName() + " " + donor.getLastName(),
        //                 donor.getAmount(),
        //                 donor.getPaymentId()
        //         );
        //     } catch (Exception e) {
        //         System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
        //     }
        // }
        return savedDonor;
    }
    // Safe decryption helper
    private String safeDecrypt(String data, SecretKeySpec key) {
        try {
            if (data != null && !data.isEmpty()) {
                return AESUtil.decrypt(data, key);
            }
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
        }
        return null;
    }

    // Fetch all donors with decryption & masking
    public List<Donourentity> getAllDonors() {
        List<Donourentity> donors = donationRepo.findAll();

        donors.forEach(d -> {
            try {
                SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

                d.setEmail(safeDecrypt(d.getEmail(), key));
                d.setMobile(safeDecrypt(d.getMobile(), key));
                d.setUniqueId(safeDecrypt(d.getUniqueId(), key));
                d.setBankName(safeDecrypt(d.getBankName(), key));
                d.setIfsc(MaskingUtil.maskIfsc(safeDecrypt(d.getIfsc(), key)));
                d.setAccountNumber(MaskingUtil.maskAccountNumber(safeDecrypt(d.getAccountNumber(), key)));
                d.setPaymentMethod(safeDecrypt(d.getPaymentMethod(), key));
                d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
                d.setUpiId(safeDecrypt(d.getUpiId(), key));
                d.setWallet(safeDecrypt(d.getWallet(), key));
                d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
                d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

            } catch (Exception e) {
                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
            }
        });

        return donors;
    }
    public void deleteById(String id) {
        donationRepo.deleteById(id);
    }
    // helper method to update encrypted data
    private void updateEncryptedField(
            java.util.function.Consumer<String> setter,
            String newValue,
            SecretKeySpec key
    ) {
        try {
            if (newValue != null && !newValue.isBlank()) {
                setter.accept(AESUtil.encrypt(newValue, key));
            }
        } catch (Exception e) {
            throw new RuntimeException("Encryption update failed: " + e.getMessage());
        }
    }

    public Donourentity updateDonor(String id, Donourentity updated) throws Exception {

        Donourentity existing = donationRepo.findById(id).orElse(null);
        if (existing == null) throw new Exception("Donor not found");

        // decrypt key for editing encrypted fields
        SecretKeySpec key = AESUtil.deobfuscateKey(existing.getEncKey());

        // ------------------ PLAIN FIELDS --------------------
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getDob() != null) existing.setDob(updated.getDob());
        if (updated.getFrequency() != null) existing.setFrequency(updated.getFrequency());
        if (updated.getPaymentMode() != null) existing.setPaymentMode(updated.getPaymentMode());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getDeclaration() != null) existing.setDeclaration(updated.getDeclaration());

        if (updated.getPaymentMethod() != null) existing.setPaymentMethod(updated.getPaymentMethod());
        if (updated.getWallet() != null) existing.setWallet(updated.getWallet());
        if (updated.getUpiId() != null) existing.setUpiId(updated.getUpiId());
        if (updated.getCurrency() != null) existing.setCurrency(updated.getCurrency());

        // Razorpay fields
        if (updated.getPaymentId() != null) existing.setPaymentId(updated.getPaymentId());
        if (updated.getOrderId() != null) existing.setOrderId(updated.getOrderId());
        if (updated.getSignature() != null) existing.setSignature(updated.getSignature());

        // Subscription fields
        if (updated.getSubscriptionId() != null) existing.setSubscriptionId(updated.getSubscriptionId());
        if (updated.getSubscriptionStatus() != null) existing.setSubscriptionStatus(updated.getSubscriptionStatus());
        if (updated.getMandateId() != null) existing.setMandateId(updated.getMandateId());
        if (updated.getMonthlyAmount() != null) existing.setMonthlyAmount(updated.getMonthlyAmount());
        if (updated.getPayerEmail() != null) existing.setPayerEmail(updated.getPayerEmail());
        if (updated.getPayerContact() != null) existing.setPayerContact(updated.getPayerContact());
        if (updated.getPaymentInfo() != null) existing.setPaymentInfo(updated.getPaymentInfo());
        if (updated.getRazorpayCustomerId() != null) existing.setRazorpayCustomerId(updated.getRazorpayCustomerId());

        // ------------------- ENCRYPTED FIELDS -------------------
        updateEncryptedField(existing::setEmail, updated.getEmail(), key);
        updateEncryptedField(existing::setMobile, updated.getMobile(), key);
        updateEncryptedField(existing::setUniqueId, updated.getUniqueId(), key);
        updateEncryptedField(existing::setBankName, updated.getBankName(), key);
        updateEncryptedField(existing::setIfsc, updated.getIfsc(), key);
        updateEncryptedField(existing::setAccountNumber, updated.getAccountNumber(), key);

        return donationRepo.save(existing);
    }
    public Donourentity findByIdDecrypt(String id) {
        Donourentity d = donationRepo.findById(id).orElse(null);
        if (d == null) return null;

        try {
            SecretKeySpec key = AESUtil.deobfuscateKey(d.getEncKey());

            d.setEmail(safeDecrypt(d.getEmail(), key));
            d.setMobile(safeDecrypt(d.getMobile(), key));
            d.setUniqueId(safeDecrypt(d.getUniqueId(), key));
            d.setBankName(safeDecrypt(d.getBankName(), key));
            d.setIfsc(safeDecrypt(d.getIfsc(), key));
            d.setAccountNumber(safeDecrypt(d.getAccountNumber(), key));
            d.setPaymentMethod(safeDecrypt(d.getPaymentMethod(), key));
            d.setPaymentInfo(safeDecrypt(d.getPaymentInfo(), key));
            d.setUpiId(safeDecrypt(d.getUpiId(), key));
            d.setWallet(safeDecrypt(d.getWallet(), key));
            d.setPayerEmail(safeDecrypt(d.getPayerEmail(), key));
            d.setPayerContact(safeDecrypt(d.getPayerContact(), key));

        } catch (Exception ex) {
            System.err.println("❌ Decrypt failed: " + ex.getMessage());
        }

        return d;
    }


}

