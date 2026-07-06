package dev.thural.quietspace.service;

import dev.thural.quietspace.entity.Photo;
import dev.thural.quietspace.enums.EntityType;
import dev.thural.quietspace.model.response.PhotoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PhotoService {

    String uploadProfilePhoto(MultipartFile file);

    Photo persistPhotoEntity(MultipartFile file, UUID entityId, EntityType entityType);

    PhotoResponse getPhotoByName(String name);

    PhotoResponse getPhotoByEntityId(UUID entityId);

    PhotoResponse getPhotoById(UUID photoId);

    byte[] getPhotoData(String name);

    void deletePhotoByEntityId(UUID entityId);

    void deletePhotoById(UUID photoId);

}
