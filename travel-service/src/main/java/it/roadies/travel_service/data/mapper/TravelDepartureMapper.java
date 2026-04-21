package it.roadies.travel_service.data.mapper;

import it.roadies.travel_service.data.dto.request.TravelDepartureCreateRequest;
import it.roadies.travel_service.data.dto.response.TravelDepartureResponse;
import it.roadies.travel_service.data.entity.TravelDeparture;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TravelDepartureMapper {
    TravelDeparture toEntity(TravelDepartureCreateRequest requestDto);
    TravelDepartureResponse toDto(TravelDeparture entity);
}
