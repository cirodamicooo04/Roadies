package it.roadies.booking_service.data.dao;


import it.roadies.booking_service.data.entities.MemberDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MemberDocumentRepository extends JpaRepository<MemberDocument, UUID> {
}
