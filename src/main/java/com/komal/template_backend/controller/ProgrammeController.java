// package com.komal.template_backend.controller;

// import com.komal.template_backend.model.Programme;
// import com.komal.template_backend.repo.ProgrammeRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.File;
// import java.io.IOException;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/programmes")
// //@CrossOrigin(origins = "http://localhost:5173")
// public class ProgrammeController {

//     @Autowired
//     private ProgrammeRepository repository;

//     // Save uploads in project folder (outside JAR)
//     private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "icons";

//     // ✅ Get all
//     @GetMapping
//     public List<Programme> getAllProgrammes() {
//         return repository.findAll();
//     }

//     // ✅ Get by ID
//     @GetMapping("/{id}")
//     public ResponseEntity<Programme> getProgrammeById(@PathVariable String id) {
//         return repository.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     // ✅ Create new
//     @PostMapping
//     public Programme createProgramme(@RequestBody Programme programme) {
//         return repository.save(programme);
//     }

//     // ✅ Update existing
//     @PutMapping("/{id}")
//     public ResponseEntity<Programme> updateProgramme(@PathVariable String id,
//                                                      @RequestBody Programme updated) {
//         Optional<Programme> optional = repository.findById(id);
//         if (optional.isPresent()) {
//             Programme existing = optional.get();
//             existing.setTitle(updated.getTitle());
//             existing.setDescription(updated.getDescription());
//             existing.setColor(updated.getColor());

//             // Keep old icon if none provided
//             if (updated.getIcon() != null && !updated.getIcon().isEmpty()) {
//                 existing.setIcon(updated.getIcon());
//             }

//             return ResponseEntity.ok(repository.save(existing));
//         } else {
//             return ResponseEntity.notFound().build();
//         }
//     }

//     // ✅ Delete
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteProgramme(@PathVariable String id) {
//         if (repository.existsById(id)) {
//             repository.deleteById(id);
//             return ResponseEntity.ok().build();
//         }
//         return ResponseEntity.notFound().build();
//     }

//     // ✅ Upload/replace icon
//     @PostMapping("/{id}/upload-icon")
//     public ResponseEntity<?> uploadIcon(@PathVariable String id,
//                                         @RequestParam("file") MultipartFile file) {
//         try {
//             Optional<Programme> optional = repository.findById(id);
//             if (optional.isEmpty()) {
//                 return ResponseEntity.notFound().build();
//             }

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
//             if (originalFileName == null) originalFileName = "icon.png";
//             String fileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");

//             // Save file
//             File dest = new File(dir, fileName);
//             file.transferTo(dest);

//             // Update programme
//             Programme programme = optional.get();
//             programme.setIcon(fileName);
//             Programme saved = repository.save(programme);

//             return ResponseEntity.ok(saved);
//         } catch (IOException e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
//         }
//     }
// }
package com.komal.template_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.komal.template_backend.model.Programme;
import com.komal.template_backend.repo.ProgrammeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/programmes")
//@CrossOrigin(origins = "http://localhost:5173")
public class ProgrammeController {

    @Autowired
    private ProgrammeRepository repository;

    @Autowired
    private Cloudinary cloudinary;

    // ✅ Get all
    @GetMapping
    public List<Programme> getAllProgrammes() {
        return repository.findAll();
    }

    // ✅ Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Programme> getProgrammeById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create new
    @PostMapping
    public Programme createProgramme(@RequestBody Programme programme) {
        return repository.save(programme);
    }

    // ✅ Update existing
    @PutMapping("/{id}")
    public ResponseEntity<Programme> updateProgramme(@PathVariable String id,
                                                     @RequestBody Programme updated) {
        Optional<Programme> optional = repository.findById(id);
        if (optional.isPresent()) {
            Programme existing = optional.get();
            existing.setTitle(updated.getTitle());
            existing.setDescription(updated.getDescription());
            existing.setColor(updated.getColor());

            // Keep old icon if none provided
            if (updated.getIcon() != null && !updated.getIcon().isEmpty()) {
                existing.setIcon(updated.getIcon());
            }

            return ResponseEntity.ok(repository.save(existing));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgramme(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ Upload/replace icon (Cloudinary)
    @PostMapping("/{id}/upload-icon")
    public ResponseEntity<?> uploadIcon(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file) {
        try {
            Optional<Programme> optional = repository.findById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            // Update programme with Cloudinary URL
            Programme programme = optional.get();
            programme.setIcon(imageUrl);
            Programme saved = repository.save(programme);

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}

