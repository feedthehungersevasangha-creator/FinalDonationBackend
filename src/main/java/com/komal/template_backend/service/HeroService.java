package com.komal.template_backend.service;
import com.komal.template_backend.model.Hero;
import com.komal.template_backend.repo.HeroRepository;
import org.springframework.stereotype.Service;


@Service
public class HeroService {

    private final HeroRepository heroRepository;

    public HeroService(HeroRepository heroRepository) {
        this.heroRepository = heroRepository;
    }

    // Fetch hero data
    public Hero getHero() {
        return heroRepository.findAll().stream().findFirst().orElse(null);
    }
}
