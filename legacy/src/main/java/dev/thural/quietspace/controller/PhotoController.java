package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.response.PhotoResponse;
import dev.thural.quietspace.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/profile")
    public ResponseEntity<String> uploadProfilePhoto(@RequestParam("image") MultipartFile file) {
        String photoName = photoService.uploadProfilePhoto(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoName);
    }

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> getPhotoByName(@PathVariable("name") String name) {
        PhotoResponse photoResponse = photoService.getPhotoByName(name);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photoResponse.getType()))
                .body(photoResponse.getData());
    }

    @DeleteMapping("profile/{userId}")
    public ResponseEntity<Void> removePhotoByUserId(@PathVariable UUID userId) {
        photoService.deletePhotoByEntityId(userId);
        return ResponseEntity.noContent().build();
    }

}
