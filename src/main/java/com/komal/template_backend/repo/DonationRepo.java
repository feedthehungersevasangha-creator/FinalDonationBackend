package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DonationRepo extends MongoRepository<Donourentity, String> {

    Optional<Donourentity> findByOrderId(String orderId);
    Optional<Donourentity> findBySubscriptionId(String subscriptionId);

    // Cleanup support
    int deleteByStatusAndDonationDateBefore(String status, LocalDateTime cutoff);

    // Basic counts
    long countByStatus(String status);

    long countByStatusAndSubscriptionIdIsNull(String status);
    long countByStatusAndSubscriptionIdIsNotNull(String status);

    long countBySubscriptionStatus(String subscriptionStatus);

    // Time-window counts
    long countByStatusAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);
    long countByStatusAndSubscriptionIdIsNullAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);
    long countByStatusAndSubscriptionIdIsNotNullAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);

    // Amount retrieval
    List<Donourentity> findByStatusAndDonationDateBetween(String status, LocalDateTime start, LocalDateTime end);
}
