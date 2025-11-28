package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;
package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DonationRepo extends MongoRepository<Donourentity,String> {
    Optional<Donourentity> findByOrderId(String orderId);
    int deleteByStatusAndDonationDateBefore(String status, LocalDateTime cutoff);
    Optional<Donourentity> findBySubscriptionId(String subscriptionId);
    long countByStatus(String status);

    // One-time successful donations: no subscriptionId (null)
    long countByStatusAndSubscriptionIdIsNull(String status);

    // Monthly/subscription successful donations: subscriptionId is present
    long countByStatusAndSubscriptionIdIsNotNull(String status);

    // Active subscriptions (by subscriptionStatus field)
    long countBySubscriptionStatus(String subscriptionStatus);

    // ---------------------------
    // Time-window counts
    // ---------------------------

    // Count successful payments between two datetimes (inclusive start, exclusive end)
    long countByStatusAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // Count one-time successful payments in a given window
    long countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // Count subscription successful payments in a given window
    long countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // ---------------------------
    // Amount retrieval for summing (DB-agnostic fallback)
    // ---------------------------

    // Return records (useful to sum amounts in Java). Not ideal for huge datasets.
    List<Donourentity> findByStatusAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

}
import java.time.LocalDateTime;
import java.util.Optional;

public interface DonationRepo extends MongoRepository<Donourentity,String> {
    Optional<Donourentity> findByOrderId(String orderId);
    int deleteByStatusAndDonationDateBefore(String status, LocalDateTime cutoff);
    Optional<Donourentity> findBySubscriptionId(String subscriptionId);
    long countByStatus(String status);

    // One-time successful donations: no subscriptionId (null)
    long countByStatusAndSubscriptionIdIsNull(String status);

    // Monthly/subscription successful donations: subscriptionId is present
    long countByStatusAndSubscriptionIdIsNotNull(String status);

    // Active subscriptions (by subscriptionStatus field)
    long countBySubscriptionStatus(String subscriptionStatus);

    // ---------------------------
    // Time-window counts
    // ---------------------------

    // Count successful payments between two datetimes (inclusive start, exclusive end)
    long countByStatusAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // Count one-time successful payments in a given window
    long countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // Count subscription successful payments in a given window
    long countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // ---------------------------
    // Amount retrieval for summing (DB-agnostic fallback)
    // ---------------------------

    // Return records (useful to sum amounts in Java). Not ideal for huge datasets.
    List<Donourentity> findByStatusAndDonationDateBetween(String status, LocalDateTime from, LocalDateTime to);

}
