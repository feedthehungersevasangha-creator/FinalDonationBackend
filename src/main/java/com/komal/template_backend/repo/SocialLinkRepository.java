package com.komal.template_backend.repo;

import com.komal.template_backend.model.SocialLink;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialLinkRepository extends MongoRepository<SocialLink, String> {}
