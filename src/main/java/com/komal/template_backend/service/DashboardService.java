// @Service
// public class DashboardService {

//     private final DonationRepo donationRepo;

//     public DashboardService(DonationRepo donationRepo) {
//         this.donationRepo = donationRepo;
//     }

//     public Map<String, Object> getDashboardCounts() {

//         Map<String, Object> res = new HashMap<>();

//         // ✅ ONE-TIME
//         long oneTimeCount =
//                 donationRepo.countByRecordTypeAndStatusAndOrderIdIsNotNullAndPaymentIdIsNotNull(
//                         "ONE_TIME", "SUCCESS"
//                 );

//         // ✅ SUBSCRIPTION MANDATES
//         long mandateCount =
//                 donationRepo.countByRecordTypeAndStatusAndSubscriptionStatusInAndMandateStatusIn(
//                         "SUBSCRIPTION_PARENT",
//                         "SUCCESS",
//                         List.of("AUTHENTICATED", "ACTIVE"),
//                         List.of("ACTIVE", "APPROVED", "AUTHORIZED")
//                 );

//         // ✅ MONTHLY DEBITS
//         long monthlyCount =
//                 donationRepo.countByRecordTypeAndStatusAndPaymentIdIsNotNull(
//                         "SUBSCRIPTION_MONTHLY",
//                         "SUCCESS"
//                 );

//         double monthlyAmount =
//                 donationRepo.sumAmountByRecordTypeAndStatusAndPaymentIdIsNotNull(
//                         "SUBSCRIPTION_MONTHLY",
//                         "SUCCESS"
//                 );

//         res.put("oneTimeCount", oneTimeCount);
//         res.put("mandateCount", mandateCount);
//         res.put("monthlyDebitCount", monthlyCount);


//         res.put("monthlyAmount", monthlyAmount);

//         return res;
//     }
// }
package com.komal.template_backend.service;

import com.komal.template_backend.repo.DonationRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final DonationRepo donationRepo;

    public DashboardService(DonationRepo donationRepo) {
        this.donationRepo = donationRepo;
    }

    // ==========================================================
    // ✅ OVERALL COUNTS
    // ==========================================================
    public Map<String, Object> getOverallCounts() {

        Map<String, Object> result = new HashMap<>();

        // ✅ One-time donations (SUCCESS + orderId)
        long oneTimeSuccess =
                donationRepo.countByStatusAndSubscriptionIdIsNull("SUCCESS");

        // ✅ Active subscriptions (AUTHENTICATED / ACTIVE)
        long activeSubscriptions =
                donationRepo.countBySubscriptionStatus("AUTHENTICATED")
                + donationRepo.countBySubscriptionStatus("ACTIVE");

        // ✅ Monthly debits (CHARGED rows)
        long monthlyDebits =
                donationRepo.countBySubscriptionStatus("CHARGED");

        result.put("oneTimeSuccess", oneTimeSuccess);
        result.put("activeSubscriptions", activeSubscriptions);
        result.put("monthlyDebits", monthlyDebits);

        return result;
    }

    // ==========================================================
    // ✅ DATE RANGE COUNTS
    // ==========================================================
    public Map<String, Object> getCountsForRange(
            LocalDateTime from, LocalDateTime to) {

        Map<String, Object> result = new HashMap<>();

        long oneTime =
                donationRepo.countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(
                        "SUCCESS", from, to
                );

        long subscriptions =
                donationRepo.countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(
                        "SUCCESS", from, to
                );

        result.put("oneTimeSuccess", oneTime);
        result.put("subscriptionSuccess", subscriptions);

        return result;
    }
}
