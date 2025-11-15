// package com.komal.template_backend.service;


// import com.komal.template_backend.model.PrivacyPolicy;
// import com.komal.template_backend.repo.PrivacyPolicyRepository;
// import org.springframework.stereotype.Service;

// @Service
// public class PrivacyPolicyService {
//     private final PrivacyPolicyRepository repository;

//     public PrivacyPolicyService(PrivacyPolicyRepository repository) {
//         this.repository = repository;
//     }

//     public PrivacyPolicy getPolicy() {
//         // If none found, return null or throw; your controller can 404 instead.
//         return repository.findAll().stream().findFirst().orElse(null);
//     }

//     public PrivacyPolicy updatePolicy(PrivacyPolicy policy) {
//         // preserve existing id if you only keep a single document
//         if (policy.getId() == null) {
//             repository.findAll().stream().findFirst().ifPresent(p -> policy.setId(p.getId()));
//         }
//         return repository.save(policy);
//     }
// }
package com.komal.template_backend.service;

import com.komal.template_backend.model.PrivacyPolicy;
import com.komal.template_backend.repo.PrivacyPolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class PrivacyPolicyService {
    private final PrivacyPolicyRepository repository;

    public PrivacyPolicyService(PrivacyPolicyRepository repository) {
        this.repository = repository;
    }

    public PrivacyPolicy getPolicy() {
        // If none found, return null or throw; your controller can 404 instead.
        return repository.findAll().stream().findFirst().orElse(null);
    }

    public PrivacyPolicy updatePolicy(PrivacyPolicy policy) {
        // preserve existing id if you only keep a single document
        if (policy.getId() == null) {
            repository.findAll().stream().findFirst().ifPresent(p -> policy.setId(p.getId()));
        }
        return repository.save(policy);
    }
}




