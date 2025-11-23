
// //--------------------------------------------------------------------------------------------------------
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

            Donourentity updated = donationRepo.save(existing);

            // send mail after successful payment
            try {
                mailService.sendDonationReceipt(
                        safeDecrypt(donor.getEmail(),key),
                        existing.getFirstName() + " " + existing.getLastName(),
                        existing.getAmount(),
                        donor.getPaymentId()
                );
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
            }

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

        if (donor.getPaymentId() != null) {
            try {
                mailService.sendDonationReceipt(
                        safeDecrypt(donor.getEmail(),key2),
                        donor.getFirstName() + " " + donor.getLastName(),
                        donor.getAmount(),
                        donor.getPaymentId()
                );
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
            }
        }
        return savedDonor;
    }


//@Service
//public class DonationService {
//    private final DonationRepo donationRepo;
//    @Autowired
//    private MailService mailService;
//
//    public DonationService(DonationRepo donationRepo) {
//        this.donationRepo = donationRepo;
//    }
//
//    // Save donor with encryption; skip null or empty fields
//    public Donourentity saveDonation(Donourentity donor) throws Exception {
//
//        if (donor.getDonationDate() == null) {
//            donor.setDonationDate(LocalDateTime.now());
//        }
//        String plainEmail = donor.getEmail(); //for sending receipt
//System.out.println("plain email:"+plainEmail);
//        // Generate salt
//        String salt = AESUtil.generateSalt();
//        donor.setEncSalt(salt);
//        // Store donation month
//        int month = donor.getDonationDate().getMonthValue();
//        donor.setEncMonth(month);
//        // Generate AES key
//        SecretKeySpec key = AESUtil.generateKey(
//                donor.getMobile() != null ? donor.getMobile() : "",
//                donor.getUniqueId() != null ? donor.getUniqueId() : "",
//                donor.getDob() != null ? donor.getDob() : "",
//                donor.getDonationDate(),
//                salt
//        );
//        // Store obfuscated key
//        donor.setEncKey(AESUtil.obfuscateKey(key));
//        // Encrypt only non-null, non-empty fields
//        if (donor.getEmail() != null && !donor.getEmail().isEmpty())
//            donor.setEmail(AESUtil.encrypt(donor.getEmail(), key));
//        else
//            donor.setEmail(null);
//        if (donor.getMobile() != null && !donor.getMobile().isEmpty())
//            donor.setMobile(AESUtil.encrypt(donor.getMobile(), key));
//        else
//            donor.setMobile(null);
//        if (donor.getUniqueId() != null && !donor.getUniqueId().isEmpty())
//            donor.setUniqueId(AESUtil.encrypt(donor.getUniqueId(), key));
//        else
//            donor.setUniqueId(null);
//        if (donor.getBankName() != null && !donor.getBankName().isEmpty())
//            donor.setBankName(AESUtil.encrypt(donor.getBankName(), key));
//        else
//            donor.setBankName(null);
//        if (donor.getIfsc() != null && !donor.getIfsc().isEmpty())
//            donor.setIfsc(AESUtil.encrypt(donor.getIfsc(), key));
//        else
//            donor.setIfsc(null);
//
//        if (donor.getAccountNumber() != null && !donor.getAccountNumber().isEmpty())
//            donor.setAccountNumber(AESUtil.encrypt(donor.getAccountNumber(), key));
//        else
//            donor.setAccountNumber(null);
//        // ✅ Payment details
//        donor.setOrderId(donor.getOrderId());
//        donor.setPaymentId(donor.getPaymentId());
//        donor.setSignature(donor.getSignature());
//        donor.setDeclaration(donor.getDeclaration());
//
//        donor.setStatus(
//                donor.getPaymentId() != null ? "SUCCESS" : "PENDING"
//        );
//
//
//        // Save to MongoDB
//       Donourentity savedDonor =donationRepo.save(donor);
//        try {
//            mailService.sendDonationReceipt(
//                    plainEmail,           // send to encrypted email before encryption if needed
//                    donor.getFirstName() + " " + donor.getLastName(),
//                    donor.getAmount(),
//                    donor.getPaymentId()
//            );
//        } catch (Exception e) {
//            System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
//        } return savedDonor;
//
//    }

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

            } catch (Exception e) {
                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
            }
        });

        return donors;
    }
}

---------------------------------------------------------------------------------------------------------------------

//--------------------------------------------------------------------------------------------------------
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
// }
