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

package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationRepo extends MongoRepository<Donourentity, String> {

    // ------------------------------------------------------------------
    // BASIC LOOKUPS
    // ------------------------------------------------------------------
    Optional<Donourentity> findByOrderId(String orderId);

    Optional<Donourentity> findBySubscriptionId(String subscriptionId);

    Optional<Donourentity> findTopBySubscriptionIdOrderByDonationDateAsc(String subscriptionId);

    // ------------------------------------------------------------------
    // SUBSCRIPTION SYNC JOB
    // ------------------------------------------------------------------
    @Query(value = "{ 'subscriptionId': { $ne: null }, " +
                   "  'subscriptionStatus': { $in: ['CREATED','AUTHENTICATED','ACTIVE'] } }")
    List<Donourentity> findActiveSubscriptions();

    // ------------------------------------------------------------------
    // DATE-BASED QUERIES
    // ------------------------------------------------------------------
    List<Donourentity> findByDonationDateBetween(LocalDateTime from, LocalDateTime to);

    List<Donourentity> findByStatusAndDonationDateBetween(
            String status, LocalDateTime from, LocalDateTime to
    );

    // ------------------------------------------------------------------
    // COUNTS (Dashboard)
    // ------------------------------------------------------------------
    long countByStatus(String status);

    long countByStatusAndSubscriptionIdIsNull(String status);

    long countByStatusAndSubscriptionIdIsNotNull(String status);

    long countBySubscriptionStatus(String subscriptionStatus);

    long countByStatusAndDonationDateBetween(
            String status, LocalDateTime from, LocalDateTime to
    );

    long countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(
            String status, LocalDateTime from, LocalDateTime to
    );

    long countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(
            String status, LocalDateTime from, LocalDateTime to
    );

    // ------------------------------------------------------------------
    // CLEANUP JOB
    // ------------------------------------------------------------------
    int deleteByStatusAndDonationDateBefore(
            String status, LocalDateTime cutoff
    );
}
