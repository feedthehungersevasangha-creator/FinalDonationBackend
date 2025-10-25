package com.komal.template_backend.repo;

import com.komal.template_backend.model.PrivacyPolicy;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrivacyPolicyRepository extends MongoRepository<PrivacyPolicy, String> {
}
