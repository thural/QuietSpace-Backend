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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    public ResponseEntity<String> uploadPhoto(@RequestParam("image") MultipartFile file) {
        String photoName = photoService.uploadPhoto(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoName);
    }

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> getPhotoByName(@PathVariable("name") String name) {
        PhotoResponse photoResponse = photoService.getPhotoWithMetadata(name);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photoResponse.getType()))
                .body(photoResponse.getData());
    }

}
