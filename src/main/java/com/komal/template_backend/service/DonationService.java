
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
    private MailService mailService;

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
        // ✅ Payment details
        donor.setOrderId(donor.getOrderId());
        donor.setPaymentId(donor.getPaymentId());
        donor.setSignature(donor.getSignature());
        donor.setDeclaration(donor.getDeclaration());

        donor.setStatus(
                donor.getPaymentId() != null ? "SUCCESS" : "PENDING"
        );


        // Save to MongoDB
       Donourentity savedDonor =donationRepo.save(donor);
        try {
            mailService.sendDonationReceipt(
                    donor.getEmail(),           // send to encrypted email before encryption if needed
                    donor.getFirstName() + " " + donor.getLastName(),
                    donor.getAmount(),
                    donor.getPaymentId()
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send donation receipt: " + e.getMessage());
        } return savedDonor;

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

