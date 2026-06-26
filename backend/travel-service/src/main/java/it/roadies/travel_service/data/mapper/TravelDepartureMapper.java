package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.client.ReservationResponse;
import it.roadies.travel_service.data.dto.client.ReserveSeatsRequest;
import it.roadies.travel_service.data.entity.TravelDeparture;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TravelDepartureMapper {
    TravelDeparture toEntity(ReserveSeatsRequest request);
    ReservationResponse toDto(TravelDeparture entity);
}
