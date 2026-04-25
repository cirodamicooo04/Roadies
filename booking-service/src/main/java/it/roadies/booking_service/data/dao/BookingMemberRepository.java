package it.roadies.booking_service.data.dao;

import it.roadies.booking_service.data.entities.BookingMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingMemberRepository extends JpaRepository<BookingMember, UUID> {
}
