package it.roadies.booking_service.mapper;

import it.roadies.booking_service.data.dto.request.BookingRequest;
import it.roadies.booking_service.data.dto.response.BookingResponse;
import it.roadies.booking_service.data.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {
    Booking toEntity(BookingRequest requestDto);
    BookingResponse toDto(Booking entity);
}
