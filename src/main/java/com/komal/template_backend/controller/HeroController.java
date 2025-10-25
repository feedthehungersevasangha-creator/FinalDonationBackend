// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Hero;
// import com.komal.template_backend.repo.HeroRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.File;
// import java.io.IOException;
// import java.util.List;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/hero")
// //@CrossOrigin(origins = "http://localhost:5173")
// public class HeroController {

//     @Autowired
//     private HeroRepository heroRepository;

//     // Save uploads in project folder (outside JAR)
//     private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "hero";

//     // Get hero (first document only)
//     @GetMapping
//     public Hero getHero() {
//         List<Hero> heroes = heroRepository.findAll();
//         return heroes.isEmpty() ? null : heroes.get(0);
//     }

//     // Update hero (text, icons, backgroundImage if provided)
//     @PutMapping
//     public Hero updateHero(@RequestBody Hero updatedHero) {
//         List<Hero> heroes = heroRepository.findAll();
//         if (!heroes.isEmpty()) {
//             Hero existing = heroes.get(0);
//             updatedHero.setId(existing.getId());

//             // keep old backgroundImage if none provided
//             if (updatedHero.getBackgroundImage() == null || updatedHero.getBackgroundImage().isEmpty()) {
//                 updatedHero.setBackgroundImage(existing.getBackgroundImage());
//             }
//         }
//         return heroRepository.save(updatedHero);
//     }

//     // Upload new background image and update hero
//     @PostMapping("/upload-image")
//     public ResponseEntity<?> uploadHeroImage(@RequestParam("file") MultipartFile file) {
//         try {
//             if (file.isEmpty()) {
//                 return ResponseEntity.badRequest().body("File is empty");
//             }

//             // Ensure upload directory exists
//             File dir = new File(uploadDir);
//             if (!dir.exists() && !dir.mkdirs()) {
//                 return ResponseEntity.status(500).body("Could not create upload directory");
//             }

//             // Generate safe filename
//             String originalFileName = file.getOriginalFilename();
//             if (originalFileName == null) originalFileName = "image.png";
//             String fileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");

//             // Save file
//             File dest = new File(dir, fileName);
//             file.transferTo(dest);

//             // Fetch existing hero or create new
//             List<Hero> heroes = heroRepository.findAll();
//             Hero hero = heroes.isEmpty() ? new Hero() : heroes.get(0);

//             // Update background image
//             hero.setBackgroundImage(fileName);
//             Hero savedHero = heroRepository.save(hero);

//             return ResponseEntity.ok(savedHero);
//         } catch (IOException e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
//         }
//     }
// }
package com.komal.template_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.komal.template_backend.model.Hero;
import com.komal.template_backend.repo.HeroRepository;
import com.komal.template_backend.config.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hero")
//@CrossOrigin(origins = "http://localhost:5173")
public class HeroController {

    @Autowired
    private HeroRepository heroRepository;

    @Autowired
    private Cloudinary cloudinary;

    // Get hero (first document only)
    @GetMapping
    public Hero getHero() {
        List<Hero> heroes = heroRepository.findAll();
        if (!heroes.isEmpty()) {
            Hero hero = heroes.get(0);
        }
        return heroes.isEmpty() ? null : heroes.get(0);
    }

    // Update hero (text, icons, backgroundImage if provided)
    @PutMapping
    public Hero updateHero(@RequestBody Hero updatedHero) {
        List<Hero> heroes = heroRepository.findAll();
        if (!heroes.isEmpty()) {
            Hero existing = heroes.get(0);
            updatedHero.setId(existing.getId());

            // keep old backgroundImage if none provided
            if (updatedHero.getBackgroundImage() == null || updatedHero.getBackgroundImage().isEmpty()) {
                updatedHero.setBackgroundImage(existing.getBackgroundImage());
            }
        }
        return heroRepository.save(updatedHero);
    }

    // Upload new background image and update hero
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadHeroImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            // Fetch existing hero or create new
            List<Hero> heroes = heroRepository.findAll();
            Hero hero = heroes.isEmpty() ? new Hero() : heroes.get(0);

            // Update background image with Cloudinary URL
            hero.setBackgroundImage(imageUrl);
            Hero savedHero = heroRepository.save(hero);

            return ResponseEntity.ok(savedHero);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}

