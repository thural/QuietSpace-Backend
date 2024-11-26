package dev.thural.quietspace.service;

import dev.thural.quietspace.model.response.PhotoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

    String uploadPhoto(MultipartFile file);

    PhotoResponse getPhotoWithMetadata(String name);

    byte[] getPhotoData(String name);

}
