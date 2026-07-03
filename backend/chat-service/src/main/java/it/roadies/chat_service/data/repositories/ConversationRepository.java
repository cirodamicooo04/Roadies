package it.roadies.chat_service.data.repositories;

import it.roadies.chat_service.data.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByTravelerIdAndOrganizerId(String travelerId, String organizerId);

    List<Conversation> findByTravelerIdOrOrganizerId(String travelerId, String organizerId);
}