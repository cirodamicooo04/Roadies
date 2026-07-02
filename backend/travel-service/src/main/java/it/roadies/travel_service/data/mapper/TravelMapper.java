package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.request.TravelCreateRequest;
import it.roadies.travel_service.data.dto.request.TravelDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.TravelDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.request.TravelUpdateRequest;
import it.roadies.travel_service.data.dto.response.TravelDepartureResponse;
import it.roadies.travel_service.data.dto.response.TravelResponse;
import it.roadies.travel_service.data.dto.response.TravelSummaryResponse;
import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.TravelDeparture;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = {ActivityMapper.class, TagMapper.class, ImageMapper.class}, nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface TravelMapper {
    @Mapping(target = "ownerId", source = "ownerId") //mappo il campo ownerId che prendo dal jwt
    @Mapping(target = "tagScores", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "numberOfRatings", ignore = true)
    @Mapping(target = "id", ignore = true) //evito manipolazioni
    Travel toEntity(TravelCreateRequest request, String ownerId);

    @Mapping(target = "type", constant = "TRAVEL")
    TravelResponse toResponse(Travel travel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "tagScores", ignore = true)
    @Mapping(target = "departures", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "numberOfRatings", ignore = true)
    void updateTravelFromDto(TravelUpdateRequest request, @MappingTarget Travel travel);

    @Mapping(target = "startingFromPrice" , ignore = true)
    @Mapping(target = "type", constant = "TRAVEL")
    TravelSummaryResponse toSummaryResponse(Travel travel);

    @Mapping(target = "travelId", source = "travel.id")
    TravelDepartureResponse toDepartureResponse(TravelDeparture departure);

    @Mapping(target = "travel", ignore = true)
    @Mapping(target = "availableSlots", ignore = true)
    TravelDeparture toDepartureEntity(TravelDepartureCreateRequest request);

    @Mapping(target = "travel", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "availableSlots", ignore = true)
    void updateDepartureEntity(TravelDepartureUpdateRequest request, @MappingTarget TravelDeparture departure);

    @AfterMapping
    default void linkRelations(@MappingTarget Travel travel) {
        if (travel.getDepartures() != null) {
            travel.getDepartures().forEach(d -> d.setTravel(travel));

        }
        if (travel.getActivities() != null) {
            travel.getActivities().forEach(a -> {
                a.setTravel(travel);
                a.setOwnerId(travel.getOwnerId());
                if (a.getDestination() == null || a.getDestination().isBlank()) {
                    a.setDestination(travel.getDestination());
                }
            });
        }
        if (travel.getTagScores() != null) {
            travel.getTagScores().forEach(ts -> ts.setTravel(travel));
        }
    }

    @AfterMapping
    default void handleStartingPrice(Travel travel, @MappingTarget TravelSummaryResponse response) {
        if (travel.getDepartures() == null || travel.getDepartures().isEmpty()) {
            response.setStartingFromPrice(BigDecimal.ZERO);
            return;
        }

        BigDecimal startingFrom = travel.getDepartures().stream()
                .map(TravelDeparture::getPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        response.setStartingFromPrice(startingFrom);
    }
}
