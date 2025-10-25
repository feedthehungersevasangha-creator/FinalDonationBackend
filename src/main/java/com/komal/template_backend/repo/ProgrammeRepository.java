package com.komal.template_backend.repo;

import com.komal.template_backend.model.Programme;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProgrammeRepository extends MongoRepository<Programme, String> {
}
