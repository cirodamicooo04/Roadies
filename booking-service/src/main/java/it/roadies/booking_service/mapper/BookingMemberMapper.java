package it.roadies.booking_service.mapper;

import it.roadies.booking_service.data.dto.request.BookingMemberRequestDTO;
import it.roadies.booking_service.data.dto.response.BookingMemberResponseDTO;
import it.roadies.booking_service.data.entities.BookingMember;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMemberMapper {
    BookingMember toEntity(BookingMemberRequestDTO requestDto);
    BookingMemberResponseDTO toDto(BookingMember entity);
}
