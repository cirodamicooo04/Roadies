package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.response.TravelTagResponse;
import it.roadies.travel_service.data.entity.TravelTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {
    @Mapping(target = "tagId", source = "tag.id")
    @Mapping(target = "tagName", source = "tag.name")
    TravelTagResponse toResponse (TravelTag travelTag);
}
