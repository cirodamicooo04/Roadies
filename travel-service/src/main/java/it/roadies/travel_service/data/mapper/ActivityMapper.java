package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureCreateRequest;
import it.roadies.travel_service.data.dto.request.ActivityDepartureUpdateRequest;
import it.roadies.travel_service.data.dto.request.ActivityUpdateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDepartureResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.dto.response.ActivitySummaryResponse;
import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "travel", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    Activity toEntity(ActivityCreateRequest activityCreateRequest);

    @Mapping(target = "travelId", source = "travel.id")
    ActivityResponse toResponse (Activity activity);

    @Mapping(target = "activityId", source = "activity.id")
    ActivityDepartureResponse toDeparturesResponse(ActivityDeparture activity);

    @Mapping(target = "activity", ignore = true)
    ActivityDeparture toDepartureEntity(ActivityDepartureCreateRequest request);

    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "maxSlots", ignore = true)
    @Mapping(target = "status", ignore = true)
    //@Mapping(target = "availableSlots", ignore = true)
    ActivityDeparture updateDepartureEntity(ActivityDepartureUpdateRequest request, @MappingTarget ActivityDeparture departure);

    @Mapping(target = "startingFromPrice", ignore = true)
    ActivitySummaryResponse toSummaryResponse(Activity activity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "travel", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    void updateActivityFromDto(ActivityUpdateRequest request, @MappingTarget Activity activity);

    @AfterMapping
    default void linkRelations(@MappingTarget Activity activity) {
        if (activity.getDepartures() != null) {
            activity.getDepartures().forEach(d -> d.setActivity(activity));
        }
    }

    @AfterMapping
    default void handleStartingPrice(Activity activity, @MappingTarget ActivitySummaryResponse response){
        if (activity.getDepartures() == null || activity.getDepartures().isEmpty()) {
            response.setStartingFromPrice(BigDecimal.ZERO);
            return;
        }

        // La logica che avevi nel Service ora è qui
        BigDecimal startingFrom = activity.getDepartures().stream()
                .map(ActivityDeparture::getPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        response.setStartingFromPrice(startingFrom);
    }

}
