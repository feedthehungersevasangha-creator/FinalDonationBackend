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
import com.komal.template_backend.service.DonationService;
import com.komal.template_backend.service.DonorsFilteration;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donors")
// @CrossOrigin(origins = "http://localhost:5173")
public class DonationController {
    private final DonationService donationService;
    private final DonorsFilteration donorsFilteration;

    public DonationController(DonationService donationService, DonorsFilteration donationFilterService, DonorsFilteration donorsFilteration) {
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
}
