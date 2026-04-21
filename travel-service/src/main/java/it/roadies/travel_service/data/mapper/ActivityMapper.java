package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.request.ActivityCreateRequest;
import it.roadies.travel_service.data.dto.response.ActivityDeparturesResponse;
import it.roadies.travel_service.data.dto.response.ActivityResponse;
import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    @Mapping(target = "id", ignore = true)
    Activity toEntity(ActivityCreateRequest activityCreateRequest);

    @Mapping(target = "travelId", source = "travel.id")
    ActivityResponse toResponse (Activity activity);

    @Mapping(target = "activityId", source = "activity.id")
    ActivityDeparturesResponse toDeparturesResponse(ActivityDeparture activity);

    @AfterMapping
    default void linkRelations(@MappingTarget Activity activity) {
        if (activity.getDepartures() != null) {
            activity.getDepartures().forEach(d -> d.setActivity(activity));
        }
    }

}
