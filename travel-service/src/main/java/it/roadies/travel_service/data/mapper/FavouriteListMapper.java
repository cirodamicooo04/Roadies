package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.response.FavouriteListItemResponse;
import it.roadies.travel_service.data.dto.response.FavouriteListResponse;
import it.roadies.travel_service.data.entity.FavouriteList;
import it.roadies.travel_service.data.entity.FavouriteListItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TravelMapper.class, ActivityMapper.class}
)
public interface FavouriteListMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "visibility", source = "visibility")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "items", source = "items")
    FavouriteListResponse toResponse(FavouriteList entity);

    List<FavouriteListResponse> toResponseList(List<FavouriteList> entities);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "addedAt", source = "addedAt")
    @Mapping(target = "travel", source = "travel")
    @Mapping(target = "activity", source = "activity")
    FavouriteListItemResponse toItemResponse(FavouriteListItem entity);
}