
package com.komal.template_backend.service;

import com.komal.template_backend.model.Donourentity;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;


@Service
public class DonorsFilteration {
    private final DonationService donationService;

    public DonorsFilteration(DonationService donationService) {
        this.donationService = donationService;
    }
    private boolean matchesIgnoreCaseTrimmed(String fieldValue, String paramValue) {
        if (paramValue == null) return true; // no filter
        if (fieldValue == null) return false; // field missing
        return fieldValue.trim().equalsIgnoreCase(paramValue.trim());
    }
    public List<Donourentity> filterDonors(Map<String, String> params) {
        List<Donourentity> donors = donationService.getAllDonors(); // already decrypted

        return donors.stream()
                // Basic info - partial match for strings
                .filter(d -> params.get("firstName") == null ||
                        d.getFirstName().toLowerCase().contains(params.get("firstName").toLowerCase()))
                .filter(d -> params.get("lastName") == null ||
                        d.getLastName().toLowerCase().contains(params.get("lastName").toLowerCase()))
                .filter(d -> params.get("email") == null ||
                        d.getEmail().toLowerCase().contains(params.get("email").toLowerCase()))
                .filter(d -> params.get("mobile") == null ||
                        d.getMobile().contains(params.get("mobile")))
                .filter(d -> params.get("dob") == null ||
                        d.getDob().equals(params.get("dob")))

                // Identity info
                .filter(d -> {
                    if (params.get("idType") == null) return true;
                    return d.getIdType() != null &&
                            d.getIdType().equalsIgnoreCase(params.get("idType").toString());
                })


                .filter(d -> params.get("uniqueId") == null ||
                        d.getUniqueId().contains(params.get("uniqueId")))


                // Donation info
                .filter(d -> matchesIgnoreCaseTrimmed(d.getPaymentMode(), params.get("paymentMode")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getFrequency(), params.get("frequency")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getIdType(), params.get("idType")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getBankName(), params.get("bankName")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getIfsc(), params.get("ifsc")))
            .filter(d -> matchesIgnoreCaseTrimmed(d.getWallet(), params.get("wallet")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getUpiId(), params.get("upiId")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getPaymentMethod(), params.get("paymentMethod")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getCurrency(), params.get("currency")))

                // Razorpay fields
                .filter(d -> matchesIgnoreCaseTrimmed(d.getPaymentId(), params.get("paymentId")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getOrderId(), params.get("orderId")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getSignature(), params.get("signature")))
                .filter(d -> matchesIgnoreCaseTrimmed(d.getStatus(), params.get("status")))
                .filter(d -> params.get("declaration") == null 
                    || d.isDeclaration() == Boolean.parseBoolean(params.get("declaration")))
                // Amount range
                .filter(d -> params.get("minAmount") == null ||
                        d.getAmount() >= parseDoubleSafe(params.get("minAmount")))
                .filter(d -> params.get("maxAmount") == null ||
                        d.getAmount() <= parseDoubleSafe(params.get("maxAmount")))

                // Date range
                .filter(d -> {
                    if (params.get("startDate") == null) return true;
                    LocalDate start = LocalDate.parse(params.get("startDate"));
                    return !d.getDonationDate().toLocalDate().isBefore(start); // donationDate >= start
                })
                .filter(d -> {
                    if (params.get("endDate") == null) return true;
                    LocalDate end = LocalDate.parse(params.get("endDate"));
                    return !d.getDonationDate().toLocalDate().isAfter(end); // donationDate <= end
                })

                .collect(Collectors.toList());
    }
    // Safe parsing of doubles
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    }


