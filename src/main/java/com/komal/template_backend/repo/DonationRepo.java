package com.komal.template_backend.repo;

import com.komal.template_backend.model.Donourentity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DonationRepo extends MongoRepository<Donourentity,String> {
}
