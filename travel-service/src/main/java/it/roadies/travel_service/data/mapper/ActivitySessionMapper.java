package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.client.ReservationResponse;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivitySessionMapper {
    ReservationResponse toDto(ActivityDeparture entity);
}
