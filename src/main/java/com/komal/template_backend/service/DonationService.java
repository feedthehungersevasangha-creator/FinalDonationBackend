//package com.komal.template_backend.service;
//
//import com.komal.template_backend.Util.AESUtil;
//import com.komal.template_backend.Util.MaskingUtil;
//import com.komal.template_backend.model.Donourentity;
//import com.komal.template_backend.repo.DonationRepo;
//import org.springframework.stereotype.Service;
//
//import java.util.Base64;
//import java.util.List;
//
//import static com.komal.template_backend.Util.AESUtil.decrypt;
//
//@Service
//public class DonationService {
//    private final DonationRepo donationRepo;
//
//    public DonationService(DonationRepo donationRepo) {
//        this.donationRepo = donationRepo;
//    }
//
//    //  Encrypt before saving
//    public Donourentity saveDonation(Donourentity donor) throws Exception {
//        donor.setEmail(AESUtil.encrypt(donor.getEmail()));
//        donor.setMobile(AESUtil.encrypt(donor.getMobile()));
//        donor.setUniqueId(AESUtil.encrypt(donor.getUniqueId()));
//        donor.setBankName(AESUtil.encrypt(donor.getBankName()));
//        donor.setIfsc(AESUtil.encrypt(donor.getIfsc()));
//        donor.setAccountNumber(AESUtil.encrypt(donor.getAccountNumber()));
//        return donationRepo.save(donor);
//    }
//
//    // ✅ Helper: check if string looks like Base64
//    private boolean isBase64(String value) {
//        if (value == null) return false;
//        try {
//            Base64.getDecoder().decode(value);
//            return true;
//        } catch (IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    //  Decrypt only encrypted values
//    public List<Donourentity> getAllDonors() {
//        List<Donourentity> donors = donationRepo.findAll();
//
//        donors.forEach(d -> {
//            try {
//                if (isBase64(d.getEmail())) d.setEmail(decrypt(d.getEmail()));
//                if (isBase64(d.getMobile())) d.setMobile(decrypt(d.getMobile()));
//                if (isBase64(d.getUniqueId())) d.setUniqueId(
//                        MaskingUtil.maskUniqueId(decrypt(d.getUniqueId()))
//                );
//                if (isBase64(d.getBankName())) d.setBankName(
//                        MaskingUtil.maskBankName(decrypt(d.getBankName()))
//                );
//                if (isBase64(d.getIfsc())) d.setIfsc(
//                        MaskingUtil.maskIfsc(decrypt(d.getIfsc()))
//                );
//                if (isBase64(d.getAccountNumber())) d.setAccountNumber(
//                        MaskingUtil.maskAccountNumber(decrypt(d.getAccountNumber()))
//                );
//            } catch (Exception e) {
//                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
//            }
//        });
//
//        return donors;
//    }
//
//}
//------------------------------------------------------------------------------------------------------
//package com.komal.template_backend.service;
//
//import com.komal.template_backend.Util.AESUtil;
//import com.komal.template_backend.Util.MaskingUtil;
//import com.komal.template_backend.model.Donourentity;
//import com.komal.template_backend.repo.DonationRepo;
//import org.springframework.stereotype.Service;
//
//import java.util.Base64;
//import java.util.List;
//
//import static com.komal.template_backend.Util.AESUtil.decrypt;
//
//@Service
//public class DonationService {
//    private final DonationRepo donationRepo;
//
//    public DonationService(DonationRepo donationRepo) {
//        this.donationRepo = donationRepo;
//    }
//
//    // Encrypt before saving with dynamic per-record key
//    public Donourentity saveDonation(Donourentity donor) throws Exception {
//        // Generate random salt for this record
//        String salt = AESUtil.generateSalt();
//        donor.setEncSalt(salt); // You should add encSalt and encMonth fields to Donourentity
//        int month = donor.getDonationDate().getMonthValue();
//        donor.setEncMonth(month);
//
//        // Generate dynamic key per record
//        var key = AESUtil.generateKey(
//                donor.getMobile(),
//                donor.getUniqueId(),
//                donor.getDob(),
//                donor.getDonationDate(),
//                salt
//        );
//
//        // Encrypt fields using dynamic key
//        donor.setEmail(AESUtil.encrypt(donor.getEmail(), key));
//        donor.setMobile(AESUtil.encrypt(donor.getMobile(), key));
//        donor.setUniqueId(AESUtil.encrypt(donor.getUniqueId(), key));
//        donor.setBankName(AESUtil.encrypt(donor.getBankName(), key));
//        donor.setIfsc(AESUtil.encrypt(donor.getIfsc(), key));
//        donor.setAccountNumber(AESUtil.encrypt(donor.getAccountNumber(), key));
//
//        return donationRepo.save(donor);
//    }
//
//    // Helper: check if string looks like Base64
//    private boolean isBase64(String value) {
//        if (value == null) return false;
//        try {
//            Base64.getDecoder().decode(value);
//            return true;
//        } catch (IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    // Decrypt only encrypted values with dynamic key
//    public List<Donourentity> getAllDonors() {
//        List<Donourentity> donors = donationRepo.findAll();
//
//        donors.forEach(d -> {
//            try {
//                // Regenerate key from stored salt + month + donor fields
//                var key = AESUtil.generateKey(
//                        d.getMobile(),      // original stored mobile may be encrypted; consider storing raw fields for key derivation
//                        d.getUniqueId(),
//                        d.getDob(),
//                        d.getDonationDate(),
//                        d.getEncSalt()
//                );
//
//                if (isBase64(d.getEmail())) d.setEmail(AESUtil.decrypt(d.getEmail(), key));
//                if (isBase64(d.getMobile())) d.setMobile(AESUtil.decrypt(d.getMobile(), key));
//                if (isBase64(d.getUniqueId()))
//                    d.setUniqueId(MaskingUtil.maskUniqueId(AESUtil.decrypt(d.getUniqueId(), key)));
//                if (isBase64(d.getBankName()))
//                    d.setBankName(MaskingUtil.maskBankName(AESUtil.decrypt(d.getBankName(), key)));
//                if (isBase64(d.getIfsc()))
//                    d.setIfsc(MaskingUtil.maskIfsc(AESUtil.decrypt(d.getIfsc(), key)));
//                if (isBase64(d.getAccountNumber()))
//                    d.setAccountNumber(MaskingUtil.maskAccountNumber(AESUtil.decrypt(d.getAccountNumber(), key)));
//            } catch (Exception e) {
//                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
//            }
//        });
//
//        return donors;
//    }
//
//
//}
//----------------------------------------------------------------------------------------------------------
//package com.komal.template_backend.service;
//
//import com.komal.template_backend.Util.AESUtil;
//import com.komal.template_backend.Util.MaskingUtil;
//import com.komal.template_backend.model.Donourentity;
//import com.komal.template_backend.repo.DonationRepo;
//import org.springframework.stereotype.Service;
//
//import java.util.Base64;
//import java.util.List;
//
//import static com.komal.template_backend.Util.AESUtil.decrypt;
//
//@Service
//public class DonationService {
//    private final DonationRepo donationRepo;
//
//    public DonationService(DonationRepo donationRepo) {
//        this.donationRepo = donationRepo;
//    }
//
//    // Encrypt before saving with dynamic per-record key
//    public Donourentity saveDonation(Donourentity donor) throws Exception {
//        // Generate random salt for this record
//        String salt = AESUtil.generateSalt();
//        donor.setEncSalt(salt);  // Make sure encSalt and encMonth fields exist in Donourentity
//        int month = donor.getDonationDate().getMonthValue();
//        donor.setEncMonth(month);
//
//        // Generate dynamic key per record
//        var key = AESUtil.generateKey(
//                donor.getMobile(),
//                donor.getUniqueId(),
//                donor.getDob(),
//                donor.getDonationDate(),
//                salt
//        );
//
//        // Encrypt only non-null fields
//        if (donor.getEmail() != null) donor.setEmail(AESUtil.encrypt(donor.getEmail(), key));
//        if (donor.getMobile() != null) donor.setMobile(AESUtil.encrypt(donor.getMobile(), key));
//        if (donor.getUniqueId() != null) donor.setUniqueId(AESUtil.encrypt(donor.getUniqueId(), key));
//        if (donor.getBankName() != null) donor.setBankName(AESUtil.encrypt(donor.getBankName(), key));
//        if (donor.getIfsc() != null) donor.setIfsc(AESUtil.encrypt(donor.getIfsc(), key));
//        if (donor.getAccountNumber() != null) donor.setAccountNumber(AESUtil.encrypt(donor.getAccountNumber(), key));
//
//        return donationRepo.save(donor);
//    }
//
//    // Helper: check if string looks like Base64
//    private boolean isBase64(String value) {
//        if (value == null) return false;
//        try {
//            Base64.getDecoder().decode(value);
//            return true;
//        } catch (IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    // Decrypt only encrypted values using dynamic key
//    public List<Donourentity> getAllDonors() {
//        List<Donourentity> donors = donationRepo.findAll();
//
//        donors.forEach(d -> {
//            try {
//                // Regenerate key from stored salt + month + donor fields
//                var key = AESUtil.generateKey(
//                        d.getMobile(),        // raw mobile may need to be stored separately for key derivation
//                        d.getUniqueId(),
//                        d.getDob(),
//                        d.getDonationDate(),
//                        d.getEncSalt()
//                );
//
//                if (isBase64(d.getEmail())) d.setEmail(AESUtil.decrypt(d.getEmail(), key));
//                if (isBase64(d.getMobile())) d.setMobile(AESUtil.decrypt(d.getMobile(), key));
//                if (isBase64(d.getUniqueId()))
//                    d.setUniqueId(MaskingUtil.maskUniqueId(AESUtil.decrypt(d.getUniqueId(), key)));
//                if (isBase64(d.getBankName()))
//                    d.setBankName(MaskingUtil.maskBankName(AESUtil.decrypt(d.getBankName(), key)));
//                if (isBase64(d.getIfsc()))
//                    d.setIfsc(MaskingUtil.maskIfsc(AESUtil.decrypt(d.getIfsc(), key)));
//                if (isBase64(d.getAccountNumber()))
//                    d.setAccountNumber(MaskingUtil.maskAccountNumber(AESUtil.decrypt(d.getAccountNumber(), key)));
//
//            } catch (Exception e) {
//                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
//            }
//        });
//
//        return donors;
//    }
//}
//----------------------------------------------------------------------------------------------------------------------------------------------
// DonationService.java
//package com.komal.template_backend.service;
//
//import com.komal.template_backend.Util.AESUtil;
//import com.komal.template_backend.Util.MaskingUtil;
//import com.komal.template_backend.model.Donourentity;
//import com.komal.template_backend.repo.DonationRepo;
//import org.springframework.stereotype.Service;
//
//import java.util.Base64;
//import java.util.List;
//
//@Service
//public class DonationService {
//
//    private final DonationRepo donationRepo;
//
//    public DonationService(DonationRepo donationRepo) {
//        this.donationRepo = donationRepo;
//    }
//
//    // Save donor with key obfuscation
//    public Donourentity saveDonation(Donourentity donor) throws Exception {
//        String salt = AESUtil.generateSalt();
//        donor.setEncSalt(salt);
//        int month = donor.getDonationDate().getMonthValue();
//        donor.setEncMonth(month);
//
//        var key = AESUtil.generateKey(donor.getMobile(), donor.getUniqueId(),
//                donor.getDob(), donor.getDonationDate(), salt);
//
//        // Store obfuscated key in DB
//        donor.setEncKey(AESUtil.obfuscateKey(key));
//
//        // Encrypt fields
//        if (donor.getEmail() != null) donor.setEmail(AESUtil.encrypt(donor.getEmail(), key));
//        if (donor.getMobile() != null) donor.setMobile(AESUtil.encrypt(donor.getMobile(), key));
//        if (donor.getUniqueId() != null) donor.setUniqueId(AESUtil.encrypt(donor.getUniqueId(), key));
//        if (donor.getBankName() != null) donor.setBankName(AESUtil.encrypt(donor.getBankName(), key));
//        if (donor.getIfsc() != null) donor.setIfsc(AESUtil.encrypt(donor.getIfsc(), key));
//        if (donor.getAccountNumber() != null) donor.setAccountNumber(AESUtil.encrypt(donor.getAccountNumber(), key));
//
//        return donationRepo.save(donor);
//    }
//
//    private boolean isBase64(String value) {
//        if (value == null) return false;
//        try {
//            Base64.getDecoder().decode(value);
//            return true;
//        } catch (IllegalArgumentException e) {
//            return false;
//        }
//    }
//    // Decrypt with stored obfuscated key
//    public List<Donourentity> getAllDonors() {
//        List<Donourentity> donors = donationRepo.findAll();
//
//        donors.forEach(d -> {
//            try {
//                var key = AESUtil.deobfuscateKey(d.getEncKey());
//
//                try { if (d.getEmail() != null && !d.getEmail().isEmpty() && isBase64(d.getEmail()))
//                    d.setEmail(AESUtil.decrypt(d.getEmail(), key));
//                } catch(Exception ignored){}
//
//                try { if (d.getMobile() != null && !d.getMobile().isEmpty() && isBase64(d.getMobile()))
//                    d.setMobile(AESUtil.decrypt(d.getMobile(), key));
//                } catch(Exception ignored){}
//
//                try { if (d.getUniqueId() != null && !d.getUniqueId().isEmpty() && isBase64(d.getUniqueId()))
//                    d.setUniqueId(MaskingUtil.maskUniqueId(AESUtil.decrypt(d.getUniqueId(), key)));
//                } catch(Exception ignored){}
//
//                try { if (d.getBankName() != null && !d.getBankName().isEmpty() && isBase64(d.getBankName()))
//                    d.setBankName(MaskingUtil.maskBankName(AESUtil.decrypt(d.getBankName(), key)));
//                } catch(Exception ignored){}
//
//                try { if (d.getIfsc() != null && !d.getIfsc().isEmpty() && isBase64(d.getIfsc()))
//                    d.setIfsc(MaskingUtil.maskIfsc(AESUtil.decrypt(d.getIfsc(), key)));
//                } catch(Exception ignored){}
//
//                try { if (d.getAccountNumber() != null && !d.getAccountNumber().isEmpty() && isBase64(d.getAccountNumber()))
//                    d.setAccountNumber(MaskingUtil.maskAccountNumber(AESUtil.decrypt(d.getAccountNumber(), key)));
//                } catch(Exception ignored){}
//
//            } catch (Exception e) {
//                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
//            }
//        });
//
////        List<Donourentity> donors = donationRepo.findAll();
////
////        donors.forEach(d -> {
////            try {
////                var key = AESUtil.deobfuscateKey(d.getEncKey());
////
////                if (isBase64(d.getEmail())) d.setEmail(AESUtil.decrypt(d.getEmail(), key));
////                if (isBase64(d.getMobile())) d.setMobile(AESUtil.decrypt(d.getMobile(), key));
////                if (isBase64(d.getUniqueId()))
////                    d.setUniqueId(MaskingUtil.maskUniqueId(AESUtil.decrypt(d.getUniqueId(), key)));
////                if (isBase64(d.getBankName()))
////                    d.setBankName(MaskingUtil.maskBankName(AESUtil.decrypt(d.getBankName(), key)));
////                if (isBase64(d.getIfsc()))
////                    d.setIfsc(MaskingUtil.maskIfsc(AESUtil.decrypt(d.getIfsc(), key)));
////                if (isBase64(d.getAccountNumber()))
////                    d.setAccountNumber(MaskingUtil.maskAccountNumber(AESUtil.decrypt(d.getAccountNumber(), key)));
////
////            } catch (Exception e) {
////                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
////            }
////        });
////
//        return donors;
//  }
//}
//--------------------------------------------------------------------------------------------------------
package com.komal.template_backend.service;

import com.komal.template_backend.Util.AESUtil;
import com.komal.template_backend.Util.MaskingUtil;
import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Service
public class DonationService {

    private final DonationRepo donationRepo;

    public DonationService(DonationRepo donationRepo) {
        this.donationRepo = donationRepo;
    }

    // Save donor with encryption; skip null or empty fields
    public Donourentity saveDonation(Donourentity donor) throws Exception {
        // Generate salt
        String salt = AESUtil.generateSalt();
        donor.setEncSalt(salt);

        // Store donation month
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

        // Encrypt only non-null, non-empty fields
        if (donor.getEmail() != null && !donor.getEmail().isEmpty())
            donor.setEmail(AESUtil.encrypt(donor.getEmail(), key));
        else
            donor.setEmail(null);

        if (donor.getMobile() != null && !donor.getMobile().isEmpty())
            donor.setMobile(AESUtil.encrypt(donor.getMobile(), key));
        else
            donor.setMobile(null);

        if (donor.getUniqueId() != null && !donor.getUniqueId().isEmpty())
            donor.setUniqueId(AESUtil.encrypt(donor.getUniqueId(), key));
        else
            donor.setUniqueId(null);

        if (donor.getBankName() != null && !donor.getBankName().isEmpty())
            donor.setBankName(AESUtil.encrypt(donor.getBankName(), key));
        else
            donor.setBankName(null);

        if (donor.getIfsc() != null && !donor.getIfsc().isEmpty())
            donor.setIfsc(AESUtil.encrypt(donor.getIfsc(), key));
        else
            donor.setIfsc(null);

        if (donor.getAccountNumber() != null && !donor.getAccountNumber().isEmpty())
            donor.setAccountNumber(AESUtil.encrypt(donor.getAccountNumber(), key));
        else
            donor.setAccountNumber(null);

        // Save to MongoDB
        return donationRepo.save(donor);
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
                d.setUniqueId(MaskingUtil.maskUniqueId(safeDecrypt(d.getUniqueId(), key)));
                d.setBankName(MaskingUtil.maskBankName(safeDecrypt(d.getBankName(), key)));
                d.setIfsc(MaskingUtil.maskIfsc(safeDecrypt(d.getIfsc(), key)));
                d.setAccountNumber(MaskingUtil.maskAccountNumber(safeDecrypt(d.getAccountNumber(), key)));

            } catch (Exception e) {
                System.err.println("Skipping decryption/masking for record: " + e.getMessage());
            }
        });

        return donors;
    }
}

