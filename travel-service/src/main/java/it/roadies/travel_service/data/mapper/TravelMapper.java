package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.request.TravelCreateRequest;
import it.roadies.travel_service.data.dto.response.TravelDepartureResponse;
import it.roadies.travel_service.data.dto.response.TravelResponse;
import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.TravelDeparture;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ActivityMapper.class, TagMapper.class})
public interface TravelMapper {
    @Mapping(target = "ownerId", source = "ownerId") //mappo il campo ownerId che prendo dal jwt
    @Mapping(target = "tagScores", ignore = true)
    @Mapping(target = "id", ignore = true) //evito manipolazioni
    Travel toEntity(TravelCreateRequest request, String ownerId);

    TravelResponse toResponse(Travel travel);

    @Mapping(target = "travelId", source = "travel.id")
    TravelDepartureResponse toDepartureResponse(TravelDeparture departure);

    @AfterMapping
    default void linkRelations(@MappingTarget Travel travel) {
        if (travel.getDepartures() != null) {
            travel.getDepartures().forEach(d -> d.setTravel(travel));
        }
        if (travel.getActivities() != null) {
            travel.getActivities().forEach(a -> {
                a.setTravel(travel);
                a.setOwnerId(travel.getOwnerId());
            });
        }
        if (travel.getTagScores() != null) {
            travel.getTagScores().forEach(ts -> ts.setTravel(travel));
        }
    }
}
