
package com.komal.template_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Donations")
public class Donourentity {
    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String dob;

    private String idType;
    private String uniqueId;  // 🔐 encrypted
    private String uniqueIdHash;   // HMAC for search

    private String frequency;
    private double amount;
    private String paymentMode;
    private String bankName;    //
    private String bankNameHash;   // HMAC
    private String ifsc;
    private String ifscHash;       // HMAC
    private String accountNumber; //
    private String accountNumberHash; // HMAC
    private String paymentId;
    private String orderId;
    private String status;
    private boolean declaration;
    private String subscriptionId;
    private String mandateId;
    private String subscriptionStatus;
    private Double monthlyAmount;

    public Double getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(Double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    private String planId;

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private String wallet;
    private String upiId;
    private String currency;
    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }

    private String PaymentMethod;
    public Boolean getDeclaration() {
        return declaration;
    }
public boolean isDeclaration() {
    return declaration;
}

    public void setDeclaration(boolean declaration) {
        this.declaration = declaration;
    }

    private LocalDateTime donationDate=LocalDateTime.now() ;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    private String signature;
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    // New fields for per-record dynamic key
    private String encSalt;   // random salt per record
    private int encMonth;// donation month used for key derivation

    public String getEncKey() {
        return encKey;
    }

    public void setEncKey(String encKey) {
        this.encKey = encKey;
    }

    private String encKey; // stores lightly obfuscated Base64 AES key

    // ===== Getters & Setters =====
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }



    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public LocalDateTime getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }

    public String getEncSalt() { return encSalt; }
    public void setEncSalt(String encSalt) { this.encSalt = encSalt; }

    public int getEncMonth() { return encMonth; }
    public void setEncMonth(int encMonth) { this.encMonth = encMonth; }


}
----------------------------------------------------------------------------------------
package com.komal.template_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Donations")
public class Donourentity {
   @Id
       private String id;

       private String firstName;
       private String lastName;
       private String email;
       private String mobile;
       private String dob;

       private String idType;
       private String uniqueId;  // 🔐 encrypted
   private String uniqueIdHash;   // HMAC for search
       private String address;

       private String frequency;
       private double amount;
       private String paymentMode;
       private String bankName;    //
   private String bankNameHash;   // HMAC
       private String ifsc;
   private String ifscHash;       // HMAC//
       private String accountNumber; //
   private String accountNumberHash; // HMAC

       private LocalDateTime donationDate = LocalDateTime.now();


   public String getId() {
       return id;
   }

   public void setId(String id) {
       this.id = id;
   }

   public String getFirstName() {
       return firstName;
   }

   public void setFirstName(String firstName) {
       this.firstName = firstName;
   }

   public String getLastName() {
       return lastName;
   }

   public void setLastName(String lastName) {
       this.lastName = lastName;
   }

   public String getEmail() {
       return email;
   }

   public void setEmail(String email) {
       this.email = email;
   }

   public String getMobile() {
       return mobile;
   }

   public void setMobile(String mobile) {
       this.mobile = mobile;
   }

   public String getDob() {
       return dob;
   }

   public void setDob(String dob) {
       this.dob = dob;
   }

   public String getIdType() {
       return idType;
   }

   public void setIdType(String idType) {
       this.idType = idType;
   }

   public String getUniqueId() {
       return uniqueId;
   }

   public void setUniqueId(String uniqueId) {
       this.uniqueId = uniqueId;
   }

   public String getAddress() {
       return address;
   }

   public void setAddress(String address) {
       this.address = address;
   }

   public String getFrequency() {
       return frequency;
   }

   public void setFrequency(String frequency) {
       this.frequency = frequency;
   }

   public double getAmount() {
       return amount;
   }

   public void setAmount(double amount) {
       this.amount = amount;
   }

   public String getPaymentMode() {
       return paymentMode;
   }

   public void setPaymentMode(String paymentMode) {
       this.paymentMode = paymentMode;
   }

   public String getIfsc() {
       return ifsc;
   }

   public void setIfsc(String ifsc) {
       this.ifsc = ifsc;
   }

   public String getBankName() {
       return bankName;
   }

   public void setBankName(String bankName) {
       this.bankName = bankName;
   }

   public String getAccountNumber() {
       return accountNumber;
   }

   public void setAccountNumber(String accountNumber) {
       this.accountNumber = accountNumber;
   }

   public LocalDateTime getDonationDate() {
       return donationDate;
   }

   public void setDonationDate(LocalDateTime donationDate) {
       this.donationDate = donationDate;
   }
}


// package com.komal.template_backend.model;

// import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.mapping.Document;

// import java.time.LocalDateTime;

// @Document(collection = "Donations")
// public class Donourentity {
//     @Id
//     private String id;

//     private String firstName;
//     private String lastName;
//     private String email;
//     private String mobile;
//     private String dob;
//     private String idType;
//     private String uniqueId;  // 🔐 encrypted
//     private String uniqueIdHash;   // HMAC for search
//     private String frequency;
//     private double amount;
//     private String paymentMode;
//     private String bankName;    //
//     private String bankNameHash;   // HMAC
//     private String ifsc;
//     private String ifscHash;       // HMAC
//     private String accountNumber; //
//     private String accountNumberHash; // HMAC
//     private String paymentId;
//     private String orderId;
//     private String status;
//     private boolean declaration;
//     private String subscriptionId;
//     private String mandateId;
//     private String subscriptionStatus;
//     private Double monthlyAmount;
//     private String invoiceNumber;     // generated by server
//     private String receiptType;       // "ONE_TIME" | "SUBSCRIPTION"
//     private String paymentInfo;       // vpa or "VISA ****1234" or bank name/ref
//     private String payerContact;      // phone/email from payment (masked)
//     private String payerEmail;
//     private String planId;
//     private String wallet;
//     private String upiId;
//     private String currency;
//     private String PaymentMethod;
//     private LocalDateTime donationDate=LocalDateTime.now() ;
//     private String signature;
//     // New fields for per-record dynamic key
//     private String encSalt;   // random salt per record
//     private int encMonth;// donation month used for key derivation
//     private String encKey; // stores lightly obfuscated Base64 AES key

//     // Mandate / subscription info (Razorpay)
//     private String razorpayCustomerId;      // the donor/customer id from Razorpay
//     private String razorpayPlanId;          // plan id (if any)
//     private String razorpayMandateId;       // mandate id for netbanking/eMandate
//     private LocalDateTime mandateStartDate; // parsed from webhook payload (if available)
//     private LocalDateTime mandateEndDate;   // optional (if available)
//     private String mandateFrequency;        // "MONTHLY", "QUARTERLY", etc or original period
//     private Double mandateAmount;           // amount per debit for the mandate
//     private String mandateStatus;           // "PENDING", "ACTIVE", "CANCELLED", etc
//     private String donorMandateRefId;       // your internal donor-mandate ref (if you generate one)
//     // phone from payment gateway (encrypt)
//     private String paymentMethod;           // e.g., "CARD", "UPI", "NETBANKING" (encrypt or plain masked)
//     // unique invoice number generated by server (not null)
//     // Hash fields for search (store HMAC-SHA256 hex/base64)
//     private String upiIdHash;
//     private String payerEmailHash;
//     private String payerContactHash;
//     // populate

//     // Small helpers
//     private boolean storedFromWebhook;      // mark if record was enriched from webhook (true/false)

//     public String getRazorpayCustomerId() {
//         return razorpayCustomerId;
//     }

//     public void setRazorpayCustomerId(String razorpayCustomerId) {
//         this.razorpayCustomerId = razorpayCustomerId;
//     }

//     public boolean isStoredFromWebhook() {
//         return storedFromWebhook;
//     }

//     public void setStoredFromWebhook(boolean storedFromWebhook) {
//         this.storedFromWebhook = storedFromWebhook;
//     }

//     public String getPayerContactHash() {
//         return payerContactHash;
//     }

//     public void setPayerContactHash(String payerContactHash) {
//         this.payerContactHash = payerContactHash;
//     }

//     public String getPayerEmailHash() {
//         return payerEmailHash;
//     }

//     public void setPayerEmailHash(String payerEmailHash) {
//         this.payerEmailHash = payerEmailHash;
//     }

//     public String getUpiIdHash() {
//         return upiIdHash;
//     }

//     public void setUpiIdHash(String upiIdHash) {
//         this.upiIdHash = upiIdHash;
//     }

//     public String getDonorMandateRefId() {
//         return donorMandateRefId;
//     }

//     public void setDonorMandateRefId(String donorMandateRefId) {
//         this.donorMandateRefId = donorMandateRefId;
//     }

//     public String getMandateStatus() {
//         return mandateStatus;
//     }

//     public void setMandateStatus(String mandateStatus) {
//         this.mandateStatus = mandateStatus;
//     }

//     public Double getMandateAmount() {
//         return mandateAmount;
//     }

//     public void setMandateAmount(Double mandateAmount) {
//         this.mandateAmount = mandateAmount;
//     }

//     public String getMandateFrequency() {
//         return mandateFrequency;
//     }

//     public void setMandateFrequency(String mandateFrequency) {
//         this.mandateFrequency = mandateFrequency;
//     }

//     public LocalDateTime getMandateEndDate() {
//         return mandateEndDate;
//     }

//     public void setMandateEndDate(LocalDateTime mandateEndDate) {
//         this.mandateEndDate = mandateEndDate;
//     }

//     public String getRazorpayMandateId() {
//         return razorpayMandateId;
//     }

//     public void setRazorpayMandateId(String razorpayMandateId) {
//         this.razorpayMandateId = razorpayMandateId;
//     }

//     public LocalDateTime getMandateStartDate() {
//         return mandateStartDate;
//     }

//     public void setMandateStartDate(LocalDateTime mandateStartDate) {
//         this.mandateStartDate = mandateStartDate;
//     }

//     public String getRazorpayPlanId() {
//         return razorpayPlanId;
//     }

//     public void setRazorpayPlanId(String razorpayPlanId) {
//         this.razorpayPlanId = razorpayPlanId;
//     }

//     public String getReceiptType() {
//         return receiptType;
//     }

//     public void setReceiptType(String receiptType) {
//         this.receiptType = receiptType;
//     }

//     public String getPaymentInfo() {
//         return paymentInfo;
//     }

//     public void setPaymentInfo(String paymentInfo) {
//         this.paymentInfo = paymentInfo;
//     }

//     public String getPayerContact() {
//         return payerContact;
//     }

//     public void setPayerContact(String payerContact) {
//         this.payerContact = payerContact;
//     }

//     public String getPayerEmail() {
//         return payerEmail;
//     }

//     public void setPayerEmail(String payerEmail) {
//         this.payerEmail = payerEmail;
//     }
//     public String getInvoiceNumber() {
//         return invoiceNumber;
//     }

//     public void setInvoiceNumber(String invoiceNumber) {
//         this.invoiceNumber = invoiceNumber;
//     }

//        // if available
// // you already have subscriptionId, subscriptionStatus, paymentId, orderId, amount etc.

//     public Double getMonthlyAmount() {
//         return monthlyAmount;
//     }

//     public void setMonthlyAmount(Double monthlyAmount) {
//         this.monthlyAmount = monthlyAmount;
//     }

//     public String getPlanId() {
//         return planId;
//     }

//     public void setPlanId(String planId) {
//         this.planId = planId;
//     }


//     public String getSubscriptionStatus() {
//         return subscriptionStatus;
//     }

//     public void setSubscriptionStatus(String subscriptionStatus) {
//         this.subscriptionStatus = subscriptionStatus;
//     }

//     public String getMandateId() {
//         return mandateId;
//     }

//     public void setMandateId(String mandateId) {
//         this.mandateId = mandateId;
//     }

//     public String getSubscriptionId() {
//         return subscriptionId;
//     }

//     public void setSubscriptionId(String subscriptionId) {
//         this.subscriptionId = subscriptionId;
//     }

//     public String getWallet() {
//         return wallet;
//     }

//     public void setWallet(String wallet) {
//         this.wallet = wallet;
//     }

//     public String getUpiId() {
//         return upiId;
//     }

//     public void setUpiId(String upiId) {
//         this.upiId = upiId;
//     }

//     public String getCurrency() {
//         return currency;
//     }

//     public void setCurrency(String currency) {
//         this.currency = currency;
//     }

//     public String getPaymentMethod() {
//         return PaymentMethod;
//     }

//     public void setPaymentMethod(String paymentMethod) {
//         PaymentMethod = paymentMethod;
//     }

//     public Boolean getDeclaration() {
//         return declaration;
//     }
//     public boolean isDeclaration() {
//         return declaration;
//     }
//     public void setDeclaration(boolean declaration) {
//         this.declaration = declaration;
//     }


//     public String getSignature() {
//         return signature;
//     }

//     public void setSignature(String signature) {
//         this.signature = signature;
//     }

//     public String getPaymentId() {
//         return paymentId;
//     }

//     public void setPaymentId(String paymentId) {
//         this.paymentId = paymentId;
//     }

//     public String getOrderId() {
//         return orderId;
//     }

//     public void setOrderId(String orderId) {
//         this.orderId = orderId;
//     }

//     public String getStatus() {
//         return status;
//     }

//     public void setStatus(String status) {
//         this.status = status;
//     }




//     public String getEncKey() {
//         return encKey;
//     }

//     public void setEncKey(String encKey) {
//         this.encKey = encKey;
//     }


//     // ===== Getters & Setters =====
//     public String getId() { return id; }
//     public void setId(String id) { this.id = id; }

//     public String getFirstName() { return firstName; }
//     public void setFirstName(String firstName) { this.firstName = firstName; }

//     public String getLastName() { return lastName; }
//     public void setLastName(String lastName) { this.lastName = lastName; }

//     public String getEmail() { return email; }
//     public void setEmail(String email) { this.email = email; }

//     public String getMobile() { return mobile; }
//     public void setMobile(String mobile) { this.mobile = mobile; }

//     public String getDob() { return dob; }
//     public void setDob(String dob) { this.dob = dob; }

//     public String getIdType() { return idType; }
//     public void setIdType(String idType) { this.idType = idType; }

//     public String getUniqueId() { return uniqueId; }
//     public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }



//     public String getFrequency() { return frequency; }
//     public void setFrequency(String frequency) { this.frequency = frequency; }

//     public double getAmount() { return amount; }
//     public void setAmount(double amount) { this.amount = amount; }

//     public String getPaymentMode() { return paymentMode; }
//     public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

//     public String getIfsc() { return ifsc; }
//     public void setIfsc(String ifsc) { this.ifsc = ifsc; }

//     public String getBankName() { return bankName; }
//     public void setBankName(String bankName) { this.bankName = bankName; }

//     public String getAccountNumber() { return accountNumber; }
//     public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

//     public LocalDateTime getDonationDate() { return donationDate; }
//     public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }

//     public String getEncSalt() { return encSalt; }
//     public void setEncSalt(String encSalt) { this.encSalt = encSalt; }

//     public int getEncMonth() { return encMonth; }
//     public void setEncMonth(int encMonth) { this.encMonth = encMonth; }


// }
