package it.roadies.booking_service.data.mapper;

import it.roadies.booking_service.data.dto.BookingMemberDTO;
import it.roadies.booking_service.data.entities.BookingMember;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMemberMapper {
    @Mapping(target = "booking", ignore = true)
    BookingMember toEntity(BookingMemberDTO requestDto);

    @AfterMapping
    default void linkDocuments(@MappingTarget BookingMember member) {
        if (member.getDocuments() != null) {
            member.getDocuments().forEach(doc -> doc.setMember(member));
        }
    }
}
