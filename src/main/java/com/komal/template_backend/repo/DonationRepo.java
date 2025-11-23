package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DonationRepo extends MongoRepository<Donourentity,String> {
    Optional<Donourentity> findByOrderId(String orderId);
    int deleteByStatusAndDonationDateBefore(String status, LocalDateTime cutoff);
    Optional<Donourentity> findBySubscriptionId(String subscriptionId);

}
