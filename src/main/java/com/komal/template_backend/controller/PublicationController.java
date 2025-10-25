//// package com.komal.template_backend.controller;
//
//// import com.komal.template_backend.model.Publication;
//// import com.komal.template_backend.repo.PublicationRepository;
//// import org.springframework.beans.factory.annotation.Autowired;
//// import org.springframework.http.ResponseEntity;
//// import org.springframework.web.bind.annotation.*;
//// import org.springframework.web.multipart.MultipartFile;
//
//// import java.io.File;
//// import java.io.IOException;
//// import java.util.List;
//// import java.util.Optional;
//// import java.util.UUID;
//
//// @RestController
//// @RequestMapping("/api/publications")
//// //@CrossOrigin(origins = "http://localhost:5173")
//// public class PublicationController {
//
////     @Autowired
////     private PublicationRepository repository;
//
////     // Save uploads in project folder (outside JAR)
////     private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "publications";
//
////     // ✅ Get all
////     @GetMapping
////     public List<Publication> getAllPublications() {
////         return repository.findAll();
////     }
//
////     // ✅ Get by ID
////     @GetMapping("/{id}")
////     public ResponseEntity<Publication> getPublicationById(@PathVariable String id) {
////         return repository.findById(id)
////                 .map(ResponseEntity::ok)
////                 .orElse(ResponseEntity.notFound().build());
////     }
//
////     // ✅ Create new
////     @PostMapping
////     public Publication createPublication(@RequestBody Publication publication) {
////         return repository.save(publication);
////     }
//
////     // ✅ Update existing
////     @PutMapping("/{id}")
////     public ResponseEntity<Publication> updatePublication(@PathVariable String id,
////                                                          @RequestBody Publication updated) {
////         Optional<Publication> optionalPub = repository.findById(id);
////         if (optionalPub.isPresent()) {
////             Publication existing = optionalPub.get();
////             existing.setTitle(updated.getTitle());
////             existing.setDescription(updated.getDescription());
//
////             // Keep old image if none provided
////             if (updated.getImageUrl() == null || updated.getImageUrl().isEmpty()) {
////                 existing.setImageUrl(existing.getImageUrl());
////             } else {
////                 existing.setImageUrl(updated.getImageUrl());
////             }
//
////             return ResponseEntity.ok(repository.save(existing));
////         } else {
////             return ResponseEntity.notFound().build();
////         }
////     }
//
////     // ✅ Delete
////     @DeleteMapping("/{id}")
////     public ResponseEntity<Void> deletePublication(@PathVariable String id) {
////         if (repository.existsById(id)) {
////             repository.deleteById(id);
////             return ResponseEntity.ok().build();
////         }
////         return ResponseEntity.notFound().build();
////     }
//
////     // ✅ Upload/replace image
////     @PostMapping("/{id}/upload-image")
////     public ResponseEntity<?> uploadImage(@PathVariable String id,
////                                          @RequestParam("file") MultipartFile file) {
////         try {
////             Optional<Publication> optionalPub = repository.findById(id);
////             if (optionalPub.isEmpty()) {
////                 return ResponseEntity.notFound().build();
////             }
//
////             if (file.isEmpty()) {
////                 return ResponseEntity.badRequest().body("File is empty");
////             }
//
////             // Ensure upload directory exists
////             File dir = new File(uploadDir);
////             if (!dir.exists() && !dir.mkdirs()) {
////                 return ResponseEntity.status(500).body("Could not create upload directory");
////             }
//
////             // Generate safe filename
////             String originalFileName = file.getOriginalFilename();
////             if (originalFileName == null) originalFileName = "image.png";
////             String fileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");
//
////             // Save file
////             File dest = new File(dir, fileName);
////             file.transferTo(dest);
//
////             // Update publication
////             Publication pub = optionalPub.get();
////             pub.setImageUrl(fileName);
////             Publication saved = repository.save(pub);
//
////             return ResponseEntity.ok(saved);
////         } catch (IOException e) {
////             e.printStackTrace();
////             return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
////         }
////     }
//// }
//package com.komal.template_backend.controller;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import com.komal.template_backend.model.Publication;
//import com.komal.template_backend.repo.PublicationRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/publications")
////@CrossOrigin(origins = "http://localhost:5173")
//public class PublicationController {
//
//    @Autowired
//    private PublicationRepository repository;
//
//    @Autowired
//    private Cloudinary cloudinary;
//
//    // ✅ Get all
//    @GetMapping
//    public List<Publication> getAllPublications() {
//        return repository.findAll();
//    }
//
//    // ✅ Get by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Publication> getPublicationById(@PathVariable String id) {
//        return repository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // ✅ Create new
//    @PostMapping
//    public Publication createPublication(@RequestBody Publication publication) {
//        return repository.save(publication);
//    }
//
//    // ✅ Update existing
//    @PutMapping("/{id}")
//    public ResponseEntity<Publication> updatePublication(@PathVariable String id,
//                                                         @RequestBody Publication updated) {
//        Optional<Publication> optionalPub = repository.findById(id);
//        if (optionalPub.isPresent()) {
//            Publication existing = optionalPub.get();
//            existing.setTitle(updated.getTitle());
//            existing.setDescription(updated.getDescription());
//
//            // Keep old image if none provided
//            if (updated.getImageUrl() != null && !updated.getImageUrl().isEmpty()) {
//                existing.setImageUrl(updated.getImageUrl());
//            }
//
//            return ResponseEntity.ok(repository.save(existing));
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // ✅ Delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePublication(@PathVariable String id) {
//        if (repository.existsById(id)) {
//            repository.deleteById(id);
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // ✅ Upload/replace image (Cloudinary)
//    @PostMapping("/{id}/upload-image")
//    public ResponseEntity<?> uploadImage(@PathVariable String id,
//                                         @RequestParam("file") MultipartFile file) {
//        try {
//            Optional<Publication> optionalPub = repository.findById(id);
//            if (optionalPub.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest().body("File is empty");
//            }
//
//            // Upload to Cloudinary
//            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//            String imageUrl = uploadResult.get("secure_url").toString();
//
//            // Update publication
//            Publication pub = optionalPub.get();
//            pub.setImageUrl(imageUrl);
//            Publication saved = repository.save(pub);
//
//            return ResponseEntity.ok(saved);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
//        }
//    }
//}
package com.komal.template_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.komal.template_backend.model.Publication;
import com.komal.template_backend.repo.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/publications")
//@CrossOrigin(origins = "http://localhost:5173")
public class PublicationController {

    @Autowired
    private PublicationRepository repository;

    @Autowired
    private Cloudinary cloudinary;

    // ✅ Get all
    @GetMapping
    public List<Publication> getAllPublications() {
        return repository.findAll();
    }

    // ✅ Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Publication> getPublicationById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create new
    @PostMapping
    public Publication createPublication(@RequestBody Publication publication) {
        return repository.save(publication);
    }

    // ✅ Update existing
    @PutMapping("/{id}")
    public ResponseEntity<Publication> updatePublication(@PathVariable String id,
                                                         @RequestBody Publication updated) {
        Optional<Publication> optionalPub = repository.findById(id);
        if (optionalPub.isPresent()) {
            Publication existing = optionalPub.get();
            existing.setTitle(updated.getTitle());
            existing.setDescription(updated.getDescription());
            existing.setSummary(updated.getSummary());
            existing.setContent(updated.getContent());

            // Keep old image if none provided
            if (updated.getImageUrl() != null && !updated.getImageUrl().isEmpty()) {
                existing.setImageUrl(updated.getImageUrl());
            }

            return ResponseEntity.ok(repository.save(existing));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ Upload/replace image (Cloudinary)
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable String id,
                                         @RequestParam("file") MultipartFile file) {
        try {
            Optional<Publication> optionalPub = repository.findById(id);
            if (optionalPub.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            // Update publication
            Publication pub = optionalPub.get();
            pub.setImageUrl(imageUrl);
            Publication saved = repository.save(pub);

            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}

