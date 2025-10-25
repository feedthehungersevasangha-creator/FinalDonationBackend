package com.komal.template_backend.controller;


import com.komal.template_backend.model.Declaration;
import com.komal.template_backend.repo.DeclarationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/declaration")
public class DeclarationController {
    @Autowired
    private DeclarationRepository repository;

    // Get declaration (only one record expected)
    @GetMapping
    public Declaration getDeclaration() {
        List<Declaration> list = repository.findAll();
        return list.isEmpty() ? new Declaration("Declaration & Guidelines",
                "Donations are accepted only from Indian citizens. Please ensure details provided are correct.") : list.get(0);
    }

    // Update or create declaration (admin)
    @PutMapping
    public Declaration updateDeclaration(@RequestBody Declaration declaration) {
        return repository.save(declaration);
    }
}

