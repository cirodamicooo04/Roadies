package it.roadies.booking_service.mapper;

import it.roadies.booking_service.data.dto.request.BookingRequestDTO;
import it.roadies.booking_service.data.dto.response.BookingResponseDTO;
import it.roadies.booking_service.data.entities.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking toEntity(BookingRequestDTO requestDto);
    BookingResponseDTO toDto(Booking entity);
}
