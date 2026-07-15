package dev.thural.quietspace.photo.controller;

import dev.thural.quietspace.photo.PhotoService;
import dev.thural.quietspace.photo.dto.PhotoResponse;
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

    @PostMapping
    public ResponseEntity<PhotoResponse> uploadPhoto(@RequestParam("image") MultipartFile file) {
        PhotoResponse response = photoService.uploadPhoto(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @GetMapping("/post/{postId}")
    public ResponseEntity<PhotoResponse> getPhotoByPostId(@PathVariable UUID postId) {
        PhotoResponse response = photoService.getPhotoByEntityId(postId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhotoById(@PathVariable UUID photoId) {
        photoService.deletePhotoById(photoId);
        return ResponseEntity.noContent().build();
    }

}
