//// package com.komal.template_backend.controller;
//
//// import com.komal.template_backend.model.PressRelease;
//// import com.komal.template_backend.repo.PressReleaseRepository;
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
//// @RequestMapping("/api/press-releases")
//// //@CrossOrigin(origins = "http://localhost:5173")
//// public class PressReleasesController {
//
////     @Autowired
////     private PressReleaseRepository repository;
//
////     private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "press-releases";
//
////     // ✅ Get all
////     @GetMapping
////     public List<PressRelease> getAllPressReleases() {
////         return repository.findAll();
////     }
//
////     // ✅ Get by ID
////     @GetMapping("/{id}")
////     public ResponseEntity<PressRelease> getPressReleaseById(@PathVariable String id) {
////         return repository.findById(id)
////                 .map(ResponseEntity::ok)
////                 .orElse(ResponseEntity.notFound().build());
////     }
//
////     // ✅ Create new
////     @PostMapping
////     public PressRelease createPressRelease(@RequestBody PressRelease pressRelease) {
////         return repository.save(pressRelease);
////     }
//
////     // ✅ Update text fields
////     @PutMapping("/{id}")
////     public ResponseEntity<PressRelease> updatePressRelease(@PathVariable String id,
////                                                            @RequestBody PressRelease updated) {
////         Optional<PressRelease> optional = repository.findById(id);
////         if (optional.isPresent()) {
////             PressRelease existing = optional.get();
////             existing.setTitle(updated.getTitle());
////             existing.setExcerpt(updated.getExcerpt());
////             existing.setDate(updated.getDate());
////             return ResponseEntity.ok(repository.save(existing));
////         }
////         return ResponseEntity.notFound().build();
////     }
//
////     // ✅ Delete
////     @DeleteMapping("/{id}")
////     public ResponseEntity<Void> deletePressRelease(@PathVariable String id) {
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
////             Optional<PressRelease> optional = repository.findById(id);
////             if (optional.isEmpty()) return ResponseEntity.notFound().build();
////             if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty");
//
////             File dir = new File(uploadDir);
////             if (!dir.exists() && !dir.mkdirs())
////                 return ResponseEntity.status(500).body("Could not create upload directory");
//
////             String originalFileName = file.getOriginalFilename();
////             if (originalFileName == null) originalFileName = "image.png";
////             String fileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");
//
////             File dest = new File(dir, fileName);
////             file.transferTo(dest);
//
////             PressRelease pr = optional.get();
////             pr.setImageUrl(fileName);
////             PressRelease saved = repository.save(pr);
//
////             return ResponseEntity.ok(saved);
//
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
//import com.komal.template_backend.model.PressRelease;
//import com.komal.template_backend.repo.PressReleaseRepository;
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
//@RequestMapping("/api/press-releases")
////@CrossOrigin(origins = "http://localhost:5173")
//public class PressReleasesController {
//
//    @Autowired
//    private PressReleaseRepository repository;
//
//    @Autowired
//    private Cloudinary cloudinary; // ✅ inject Cloudinary
//
//    // ✅ Get all
//    @GetMapping
//    public List<PressRelease> getAllPressReleases() {
//        return repository.findAll();
//    }
//
//    // ✅ Get by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<PressRelease> getPressReleaseById(@PathVariable String id) {
//        return repository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // ✅ Create new
//    @PostMapping
//    public PressRelease createPressRelease(@RequestBody PressRelease pressRelease) {
//        return repository.save(pressRelease);
//    }
//
//    // ✅ Update text fields
//    @PutMapping("/{id}")
//    public ResponseEntity<PressRelease> updatePressRelease(@PathVariable String id,
//                                                           @RequestBody PressRelease updated) {
//        Optional<PressRelease> optional = repository.findById(id);
//        if (optional.isPresent()) {
//            PressRelease existing = optional.get();
//            existing.setTitle(updated.getTitle());
//            existing.setExcerpt(updated.getExcerpt());
//            existing.setDate(updated.getDate());
//            return ResponseEntity.ok(repository.save(existing));
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // ✅ Delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePressRelease(@PathVariable String id) {
//        if (repository.existsById(id)) {
//            repository.deleteById(id);
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // ✅ Upload/replace image with Cloudinary
//    @PostMapping("/{id}/upload-image")
//    public ResponseEntity<?> uploadImage(@PathVariable String id,
//                                         @RequestParam("file") MultipartFile file) {
//        try {
//            Optional<PressRelease> optional = repository.findById(id);
//            if (optional.isEmpty()) return ResponseEntity.notFound().build();
//            if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty");
//
//            // ✅ Upload to Cloudinary
//            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
//                    "folder", "press-releases" // optional: organize files in a Cloudinary folder
//            ));
//            String imageUrl = uploadResult.get("secure_url").toString();
//
//            // ✅ Update entity with Cloudinary URL
//            PressRelease pr = optional.get();
//            pr.setImageUrl(imageUrl);
//            PressRelease saved = repository.save(pr);
//
//            return ResponseEntity.ok(saved);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
//        }
//    }
//}
//
package com.komal.template_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.komal.template_backend.model.PressRelease;
import com.komal.template_backend.repo.PressReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/press-releases")
//@CrossOrigin(origins = "http://localhost:5173")
public class PressReleasesController {

    @Autowired
    private PressReleaseRepository repository;

    @Autowired
    private Cloudinary cloudinary; // ✅ Cloudinary for image uploads

    // ✅ Get all
    @GetMapping
    public List<PressRelease> getAllPressReleases() {
        return repository.findAll();
    }

    // ✅ Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<PressRelease> getPressReleaseById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create new
    @PostMapping
    public PressRelease createPressRelease(@RequestBody PressRelease pressRelease) {
        return repository.save(pressRelease);
    }

    // ✅ Update text fields
    @PutMapping("/{id}")
    public ResponseEntity<PressRelease> updatePressRelease(@PathVariable String id,
                                                           @RequestBody PressRelease updated) {
        Optional<PressRelease> optional = repository.findById(id);
        if (optional.isPresent()) {
            PressRelease existing = optional.get();
            existing.setTitle(updated.getTitle());
            existing.setExcerpt(updated.getExcerpt());
            existing.setDate(updated.getDate());
            existing.setContent(updated.getContent());
            existing.setSummary(updated.getSummary());

            // Keep old image if none provided
            if (updated.getImageUrl() != null && !updated.getImageUrl().isEmpty()) {
                existing.setImageUrl(updated.getImageUrl());
            }

            return ResponseEntity.ok(repository.save(existing));
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePressRelease(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ Upload/replace image with Cloudinary
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable String id,
                                         @RequestParam("file") MultipartFile file) {
        try {
            Optional<PressRelease> optional = repository.findById(id);
            if (optional.isEmpty()) return ResponseEntity.notFound().build();
            if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty");

            // ✅ Upload to Cloudinary (inside folder "press-releases")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "press-releases"
            ));
            String imageUrl = uploadResult.get("secure_url").toString();

            // ✅ Update entity with new image
            PressRelease pr = optional.get();
            pr.setImageUrl(imageUrl);
            PressRelease saved = repository.save(pr);

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
