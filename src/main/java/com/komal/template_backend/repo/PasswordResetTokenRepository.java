package com.komal.template_backend.repo;

import com.komal.template_backend.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository  extends MongoRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}
