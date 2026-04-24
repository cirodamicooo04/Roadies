package it.roadies.booking_service.data.mapper;

import it.roadies.booking_service.data.dto.request.BookingDraftRequest;
import it.roadies.booking_service.data.dto.response.BookingDraftResponse;
import it.roadies.booking_service.data.entities.Booking;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {
    Booking toEntity(BookingDraftRequest requestDto);

    @Mapping(source = "id", target = "bookingId")
    BookingDraftResponse toDto(Booking entity);

    @AfterMapping
    default void linkMembersAndDocuments(@MappingTarget Booking booking) {
        if (booking.getMembers() != null) {
            booking.getMembers().forEach(member -> {
                member.setBooking(booking);

                if (member.getDocuments() != null) {
                    member.getDocuments().forEach(doc -> doc.setMember(member));
                }
            });
        }
    }
}
