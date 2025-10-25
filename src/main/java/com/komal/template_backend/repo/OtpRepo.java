package com.komal.template_backend.repo;

import com.komal.template_backend.model.Otpentity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OtpRepo extends MongoRepository <Otpentity,String> {
    Optional<Otpentity> findByEmail(String email);
    void deleteByEmail(String email);
    List<Otpentity> findAllByExpiresAtBefore(Date now);

    List<Otpentity> findAllByBlockedTrue();
}
