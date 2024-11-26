package dev.thural.quietspace.service.impl;

import dev.thural.quietspace.entity.Photo;
import dev.thural.quietspace.exception.ImageUploadException;
import dev.thural.quietspace.exception.UnsupportedImageTypeException;
import dev.thural.quietspace.model.response.PhotoResponse;
import dev.thural.quietspace.repository.PhotoRepository;
import dev.thural.quietspace.service.PhotoService;
import dev.thural.quietspace.utils.ImageCompressionUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final ImageCompressionUtil imageCompressionUtil;

    private static final List<String> SUPPORTED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int TARGET_IMAGE_SIZE = 100 * 1024; // 100KB

    @Override
    @Transactional
    public String uploadPhoto(MultipartFile file) {

        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("File size exceeds maximum limit of 2MB");
            throw new ImageUploadException("File size should not exceed 2MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType)) {
            log.error("Unsupported image type: {}", contentType);
            throw new UnsupportedImageTypeException(
                    "Unsupported image type. Supported types: " +
                            String.join(", ", SUPPORTED_CONTENT_TYPES)
            );
        }

        return savePhoto(file, contentType).getName();
    }

    private Photo savePhoto(MultipartFile file, String contentType) {
        try {
            String uniqueFileName = generateUniqueFileName(file);

            byte[] compressedImage = imageCompressionUtil.compressImage(
                    file.getInputStream(),
                    TARGET_IMAGE_SIZE
            );

            return photoRepository.save(Photo.builder()
                    .name(uniqueFileName)
                    .type(contentType)
                    .data(compressedImage)
                    .build());
        } catch (IOException e) {
            log.error("Error uploading photo", e);
            throw new ImageUploadException("Failed to upload photo", e);
        }
    }

    @Override
    public PhotoResponse getPhotoWithMetadata(String name) {
        Photo foundPhoto = findPhotoByName(name);
        return PhotoResponse.builder()
                .name(foundPhoto.getName())
                .type(foundPhoto.getType())
                .data(imageCompressionUtil.decompressImage(foundPhoto.getData()))
                .build();
    }

    @Override
    public byte[] getPhotoData(String name) {
        Photo foundPhoto = findPhotoByName(name);
        return imageCompressionUtil.decompressImage(foundPhoto.getData());
    }

    private Photo findPhotoByName(String name) {
        return photoRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Photo not found with name: " + name
                ));
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        return UUID.randomUUID() + fileExtension;
    }
}
