package com.komal.template_backend.repo;
    import com.komal.template_backend.model.Admin;
    import org.springframework.data.mongodb.repository.MongoRepository;

    import java.util.Optional;

    public interface AdminRepo extends MongoRepository<Admin, Long> {
        Optional<Admin> findByEmail(String email);
        Optional<Admin> findByResetToken(String token);
    }


