package it.roadies.travel_service.services;


import it.roadies.travel_service.data.dto.response.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ImageService {
    ImageResponse uploadImage(MultipartFile file, String ownerId);
    void deleteImageFromMinio(String path);
}
