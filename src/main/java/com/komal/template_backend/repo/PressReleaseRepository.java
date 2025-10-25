package com.komal.template_backend.repo;

import com.komal.template_backend.model.PressRelease;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PressReleaseRepository extends MongoRepository<PressRelease, String> {
}
