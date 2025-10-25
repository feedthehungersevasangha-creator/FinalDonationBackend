package com.komal.template_backend.repo;

import com.komal.template_backend.model.Declaration;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeclarationRepository  extends MongoRepository<Declaration, String> {
}
