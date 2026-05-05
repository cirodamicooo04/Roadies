package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.response.ImageResponse;
import it.roadies.travel_service.data.entity.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageResponse toResponse(Image image);
}
