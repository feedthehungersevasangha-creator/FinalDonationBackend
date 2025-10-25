package com.komal.template_backend.repo;

import com.komal.template_backend.model.Publication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicationRepository extends MongoRepository<Publication, String> {

}
