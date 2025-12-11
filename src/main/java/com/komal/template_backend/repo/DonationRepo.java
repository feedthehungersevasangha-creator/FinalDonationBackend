// package com.komal.template_backend.repo;

// import com.komal.template_backend.model.Donourentity;
// import org.springframework.data.mongodb.repository.MongoRepository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.data.mongodb.repository.Query;

// public interface DonationRepo extends MongoRepository<Donourentity, String> {

//     @Query(value = "{ 'subscriptionId': { $ne: null }, " +
//                    "  'subscriptionStatus': { $in: ['CREATED','AUTHENTICATED','ACTIVE'] } }")
//     List<Donourentity> findActiveSubscriptions();

//     Optional<Donourentity> findBySubscriptionId(String subscriptionId);
// }


// public interface DonationRepo extends MongoRepository<Donourentity, String> {

//     Optional<Donourentity> findByOrderId(String orderId);
//     Optional<Donourentity> findBySubscriptionId(String subscriptionId);

//     // Cleanup support
//     int deleteByStatusAndDonationDateBefore(String status, LocalDateTime cutoff);

//     // Basic counts
//     long countByStatus(String status);

    

//     long countByStatusAndSubscriptionIdIsNull(String status);
//     long countByStatusAndSubscriptionIdIsNotNull(String status);

//     long countBySubscriptionStatus(String subscriptionStatus);
// Optional<Donourentity> findTopBySubscriptionIdOrderByDonationDateAsc(String subscriptionId);

//     // Time-window counts
//     long countByStatusAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);
//     long countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);
//     long countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);
// List<Donourentity> findByDonationDateBetween(LocalDateTime from, LocalDateTime to);

//     // Amount retrieval
//     List<Donourentity> findByStatusAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);

// }

// package com.komal.template_backend.repo;

// import com.komal.template_backend.model.Donourentity;
// import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.data.mongodb.repository.Query;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// public interface DonationRepo extends MongoRepository<Donourentity, String> {
//   // ✅ One-time donations
//     long countByRecordTypeAndStatus(String recordType, String status);

//     // ✅ Mandates (parent subscription row)
//     long countByRecordTypeAndMandateStatus(String recordType, String mandateStatus);

//     // ✅ Monthly debits
//     long countByRecordTypeAndStatusAndSubscriptionIdIsNotNull(
//             String recordType,
//             String status
//     );
//     // ------------------------------------------------------------------
//     // BASIC LOOKUPS
//     // ------------------------------------------------------------------
//     Optional<Donourentity> findByOrderId(String orderId);

//     Optional<Donourentity> findBySubscriptionId(String subscriptionId);

//     Optional<Donourentity> findTopBySubscriptionIdOrderByDonationDateAsc(String subscriptionId);

//     // ------------------------------------------------------------------
//     // SUBSCRIPTION SYNC JOB
//     // ------------------------------------------------------------------
//     @Query(value = "{ 'subscriptionId': { $ne: null }, " +
//                    "  'subscriptionStatus': { $in: ['CREATED','AUTHENTICATED','ACTIVE'] } }")
//     List<Donourentity> findActiveSubscriptions();

//     // ------------------------------------------------------------------
//     // DATE-BASED QUERIES
//     // ------------------------------------------------------------------
//     List<Donourentity> findByDonationDateBetween(LocalDateTime from, LocalDateTime to);

//     List<Donourentity> findByStatusAndDonationDateBetween(
//             String status, LocalDateTime from, LocalDateTime to
//     );
// // ------------------------------------------------------------------
// // SUBSCRIPTION SYNC JOB (SAFE FOR E-MANDATE)
// // ------------------------------------------------------------------
// @Query(value = "{ 'subscriptionId': { $ne: null }, " +
//                "  'subscriptionStatus': { $nin: ['CANCELLED','COMPLETED','EXPIRED'] } }")
// List<Donourentity> findSubscriptionsForSync();
//     // ------------------------------------------------------------------
//     // COUNTS (Dashboard)
//     // ------------------------------------------------------------------
//     long countByStatus(String status);

//     long countByStatusAndSubscriptionIdIsNull(String status);

//     long countByStatusAndSubscriptionIdIsNotNull(String status);

//     long countBySubscriptionStatus(String subscriptionStatus);

//     long countByStatusAndDonationDateBetween(
//             String status, LocalDateTime from, LocalDateTime to
//     );

//     long countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(
//             String status, LocalDateTime from, LocalDateTime to
//     );

//     long countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(
//             String status, LocalDateTime from, LocalDateTime to
//     );

//     // ------------------------------------------------------------------
//     // CLEANUP JOB
//     // ------------------------------------------------------------------
//     int deleteByStatusAndDonationDateBefore(
//             String status, LocalDateTime cutoff
//     );
//      // ===============================
//     // ONE-TIME
//     // ===============================
//     long countByRecordTypeAndStatusAndOrderIdIsNotNullAndPaymentIdIsNotNull(
//             String recordType,
//             String status
//     );

//     // ===============================
//     // SUBSCRIPTION (MANDATE)
//     // ===============================
//     long countByRecordTypeAndStatusAndSubscriptionStatusInAndMandateStatusIn(
//             String recordType,
//             String status,
//             List<String> subscriptionStatuses,
//             List<String> mandateStatuses
//     );

//     // ===============================
//     // MONTHLY DEBITS
//     // ===============================
//     long countByRecordTypeAndStatusAndPaymentIdIsNotNull(
//             String recordType,
//             String status
//     );

//     double sumAmountByRecordTypeAndStatusAndPaymentIdIsNotNull(
//             String recordType,
//             String status
//     );
// }
package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationRepo extends MongoRepository<Donourentity, String> {

    // =================================================
    // BASIC LOOKUPS (USED BY WEBHOOKS & PAYMENTS)
    // =================================================
    Optional<Donourentity> findByOrderId(String orderId);

    Optional<Donourentity> findBySubscriptionId(String subscriptionId);

    Optional<Donourentity> findTopBySubscriptionIdOrderByDonationDateAsc(String subscriptionId);

    // =================================================
    // SUBSCRIPTION SYNC (SAFE FOR E-MANDATE)
    // =================================================
    @Query(value = "{ 'subscriptionId': { $ne: null }, " +
                   "  'subscriptionStatus': { $nin: ['CANCELLED','COMPLETED','EXPIRED'] } }")
    List<Donourentity> findSubscriptionsForSync();
    
@Query("SELECT d FROM Donourentity d WHERE d.createdAt BETWEEN :from AND :to")
List<Donourentity> findByDateRange(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
);

    // =================================================
    // DASHBOARD COUNTS (FINAL – USE RECORD TYPE)
    // =================================================

    // ✅ One-time donations
    long countByRecordTypeAndStatus(
            String recordType,    // ONE_TIME
            String status         // SUCCESS
    );

    // ✅ Mandate parents
    long countByRecordTypeAndMandateStatus(
            String recordType,    // SUBSCRIPTION_PARENT
            String mandateStatus  // AUTHORIZED / ACTIVE
    );

    // ✅ Monthly debits
    long countByRecordTypeAndStatusAndPaymentIdIsNotNull(
            String recordType,    // SUBSCRIPTION_MONTHLY
            String status         // SUCCESS
    );
@Query(value = "{ 'recordType': ?0, 'status': ?1, 'paymentId': { $ne: null } }",
       fields = "{ 'amount': 1 }")
List<Donourentity> findAmountsForSum(
        String recordType,
        String status
);
    // =================================================
    // DATE RANGE (REPORTS)
    // =================================================
    List<Donourentity> findByDonationDateBetween(
            LocalDateTime from,
            LocalDateTime to
    );

    List<Donourentity> findByStatusAndDonationDateBetween(
            String status,
            LocalDateTime from,
            LocalDateTime to
    );

    // =================================================
    // CLEANUP
    // =================================================
    int deleteByStatusAndDonationDateBefore(
            String status,
            LocalDateTime cutoff
    );
}

