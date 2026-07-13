package dev.thural.quietspace.photo;

import dev.thural.quietspace.shared.enums.EntityType;
import dev.thural.quietspace.photo.dto.PhotoResponse;
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
