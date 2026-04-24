package it.roadies.booking_service.data.mapper;

import it.roadies.booking_service.data.dto.BookingMemberDTO;
import it.roadies.booking_service.data.dto.request.BookingMemberRequest;
import it.roadies.booking_service.data.dto.response.BookingMemberResponse;
import it.roadies.booking_service.data.entities.BookingMember;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMemberMapper {
    BookingMember toEntity(BookingMemberDTO requestDto);
    BookingMemberResponse toDto(BookingMember entity);
}
