
// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Donourentity;
// import com.komal.template_backend.service.DonationService;
// import com.komal.template_backend.service.DonorsFilteration;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/donors")
// // @CrossOrigin(origins = "http://localhost:5173")
// public class DonationController {
//     private final DonationService donationService;
//     private final DonorsFilteration donorsFilteration;

//     public DonationController(DonationService donationService, DonorsFilteration donationFilterService, DonorsFilteration donorsFilteration) {
//         this.donationService = donationService;
//         this.donorsFilteration = donorsFilteration;
//     }

//     // Save donor record
//     @PostMapping("/save")
//     public Donourentity saveDonation(@RequestBody Donourentity donor) throws Exception {
//         // The service now automatically generates a per-record dynamic AES key
//         // based on mobile, uniqueId, dob, donationDate, and a random salt
//         return donationService.saveDonation(donor);
//     }

//     // Get all donors
//     @GetMapping("/all")
//     public List<Donourentity> getAllDonors() {
//         // Returns donors with sensitive fields decrypted and masked for display
//         return donationService.getAllDonors();
//     }

//     // Filter donors by criteria
//     @PostMapping("/filter")
//     public List<Donourentity> filterDonors(@RequestBody Map<String, String> filters) {
//         return donorsFilteration.filterDonors(filters);
//     }
//     @DeleteMapping("/delete/{id}")
//     public ResponseEntity<?> deleteDonor(@PathVariable String id) {
//         try {
//             donationService.deleteById(id);
//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "message", "Donor deleted successfully"
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body(Map.of(
//                     "success", false,
//                     "message", "Delete failed"
//             ));
//         }
//     }

//     // ------------------------- UPDATE --------------------------------
//     @PutMapping("/update/{id}")
//     public ResponseEntity<?> updateDonor(
//             @PathVariable String id,
//             @RequestBody Donourentity updated
//     ) {
//         try {
//             Donourentity result = donationService.updateDonor(id, updated);
//             return ResponseEntity.ok(Map.of(
//                     "success", true,
//                     "data", result
//             ));
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body(Map.of(
//                     "success", false,
//                     "message", e.getMessage()
//             ));
//         }
//     }
// }

//package com.komal.template_backend.controller;
//
//import com.komal.template_backend.model.Donourentity;
//import com.komal.template_backend.service.DonationService;
//import com.komal.template_backend.service.DonorsFilteration;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/donors")
//public class DonationController {
//    private final DonationService donationService;
//    private final DonorsFilteration donorsFilteration;
//    public DonationController(DonationService donationService, DonorsFilteration donationFilterService, DonorsFilteration donorsFilteration)
//    {
//        this.donationService = donationService;
//        this.donorsFilteration = donorsFilteration;
//    }
//    @PostMapping("/save")
//    public Donourentity saveDonation(@RequestBody Donourentity donor) throws Exception {
//        return donationService.saveDonation(donor);
//    }
//    @GetMapping("/all")
//    public List<Donourentity> getAllDonors() {
//        return donationService.getAllDonors();
//    }
//    @PostMapping("/filter")
//    public List<Donourentity> filterDonors(@RequestBody Map<String, String> filters) {
//        return donorsFilteration.filterDonors(filters);
//    }
//}
//
package com.komal.template_backend.controller;

import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import com.komal.template_backend.service.DonationService;
import com.komal.template_backend.service.DonorsFilteration;
import com.komal.template_backend.service.PdfReceiptServic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/donors")
// @CrossOrigin(origins = "http://localhost:5173")
public class DonationController {
    private final DonationService donationService;
    private final DonorsFilteration donorsFilteration;
    @Autowired
    private PdfReceiptServic pdfReceiptService;

    @Autowired
    private DonationRepo donationRepo;

    public DonationController(DonationService donationService, DonorsFilteration donorsFilteration) {
        this.donationService = donationService;
        this.donorsFilteration = donorsFilteration;
    }


    // Save donor record
    @PostMapping("/save")
    public Donourentity saveDonation(@RequestBody Donourentity donor) throws Exception {
        // The service now automatically generates a per-record dynamic AES key
        // based on mobile, uniqueId, dob, donationDate, and a random salt
        return donationService.saveDonation(donor);
    }

    // Get all donors
    @GetMapping("/all")
    public List<Donourentity> getAllDonors() {
        // Returns donors with sensitive fields decrypted and masked for display
        return donationService.getAllDonors();
    }

    // Filter donors by criteria
    @PostMapping("/filter")
    public List<Donourentity> filterDonors(@RequestBody Map<String, String> filters) {
        return donorsFilteration.filterDonors(filters);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDonor(@PathVariable String id) {
        try {
            donationService.deleteById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Donor deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,

                    "message", "Delete failed"
            ));
        }
    }

    // ------------------------- UPDATE --------------------------------
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDonor(
            @PathVariable String id,
            @RequestBody Donourentity updated
    ) {
        try {
            Donourentity result = donationService.updateDonor(id, updated);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadReceipt(@PathVariable String id) {
            System.out.println("📥 Receipt download request for ID: " + id);
        try {
            Donourentity d = donationService.findByIdDecrypt(id);

            if (d == null) {
                return ResponseEntity.badRequest().body("Invalid donor ID");
            }
 System.out.println("✅ Donor found:");
        System.out.println("   ID = " + d.getId());
        System.out.println("   Frequency = " + d.getFrequency());
        System.out.println("   SubscriptionId = " + d.getSubscriptionId());
        System.out.println("   PaymentId = " + d.getPaymentId());
        System.out.println("   Amount = " + d.getAmount());
            byte[] pdf;

            // ONE-TIME PAYMENT
            if (d.getFrequency().equalsIgnoreCase("onetime")) {
                pdf = pdfReceiptService.generateOneTimeDonationReceipt(
                        d,
                        d.getPaymentId(),
                        d.getAmount()
                );
            }
            // SUBSCRIPTION MANDATE (first time)
            else if (d.getSubscriptionId() != null && d.getPaymentId() == null) {
                pdf = pdfReceiptService.generateMandateConfirmation(d);
            }
            // MONTHLY CHARGE
            else {
                pdf = pdfReceiptService.generateMonthlyDebitReceipt(
                        d,
                        d.getPaymentId(),
                        d.getAmount()
                );
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Receipt_" + d.getId() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to generate receipt: " + e.getMessage());
        }
    }
//     @GetMapping("/download/{id}")
// public ResponseEntity<?> downloadReceipt(@PathVariable String id) {
//     try {
//         Donourentity d = donationService.findByIdDecrypt(id);

//         if (d == null) {
//             return ResponseEntity.badRequest().body("Invalid donor ID");
//         }

//         byte[] pdf;

//         // ✅ 1️⃣ ONE-TIME DONATION
//         if ("onetime".equalsIgnoreCase(d.getFrequency())) {

//             pdf = pdfReceiptService.generateOneTimeDonationReceipt(
//                     d,
//                     d.getPaymentId(),
//                     d.getAmount()
//             );

//         }
//         // ✅ 2️⃣ MANDATE CONFIRMATION (subscription created but no monthly debit yet)
//         else if (d.getSubscriptionId() != null
//                 && d.getRazorpayMandateId() != null
//                 && (d.getSubscriptionStatus() != null &&
//                     d.getSubscriptionStatus().equalsIgnoreCase("AUTHENTICATED"))) {

//             pdf = pdfReceiptService.generateMandateConfirmation(d);

//         }
//         // ✅ 3️⃣ MONTHLY DEBIT (subscription.charged rows)
//         else if (d.getSubscriptionId() != null
//                 && d.getPaymentId() != null
//                 && d.getAmount() > 0) {

//             pdf = pdfReceiptService.generateMonthlyDebitReceipt(
//                     d,
//                     d.getPaymentId(),
//                     d.getAmount()
//             );

//         }
//         else {
//             return ResponseEntity.badRequest()
//                     .body("Receipt not available for this donation yet");
//         }

//         return ResponseEntity.ok()
//                 .header(HttpHeaders.CONTENT_DISPOSITION,
//                         "attachment; filename=Receipt_" + d.getId() + ".pdf")
//                 .contentType(MediaType.APPLICATION_PDF)
//                 .body(pdf);

//     } catch (Exception e) {
//         e.printStackTrace();
//         return ResponseEntity.status(500)
//                 .body("Failed to generate receipt: " + e.getMessage());
//     }
// }

// @GetMapping("/donation-counts")
// public ResponseEntity<?> getDonationCounts(
//         @RequestParam(required = false) String from,
//         @RequestParam(required = false) String to
// ) {
//     try {
//         System.out.println("===== /donation-counts API HIT =====");
//         System.out.println("FROM param: " + from);
//         System.out.println("TO param  : " + to);

//         Map<String, Object> counts =
//                 donationService.getUniversalCounts(from, to);

//         System.out.println("===== /donation-counts API SUCCESS =====");

//         return ResponseEntity.ok(
//                 Map.of(
//                         "success", true,
//                         "counts", counts
//                 )
//         );

//     } catch (Exception e) {
//         System.out.println("❌ ERROR in /donation-counts");
//         e.printStackTrace();

//         return ResponseEntity.status(500).body(
//                 Map.of(
//                         "success", false,
//                         "message", e.getMessage()
//                 )
//         );
//     }
// }

//     @GetMapping("/donation-counts")
// public ResponseEntity<?> getDonationCounts(
//         @RequestParam(required = false) String from,
//         @RequestParam(required = false) String to
// ) {
//     try {
//         Map<String, Object> counts = donationService.getUniversalCounts(from, to);
//         return ResponseEntity.ok(Map.of("success", true, "counts", counts));
//     } catch (Exception e) {
//         e.printStackTrace();
//         return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
//     }
// }


}
