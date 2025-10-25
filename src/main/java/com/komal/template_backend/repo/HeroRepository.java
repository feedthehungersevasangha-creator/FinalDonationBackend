package com.komal.template_backend.repo;
import com.komal.template_backend.model.Hero;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface HeroRepository extends MongoRepository<Hero, String>{
}
