package it.roadies.chat_service.data.repositories;

import it.roadies.chat_service.data.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderByTimestampDesc(UUID conversationId, Pageable pageable);

    long countByConversationIdAndReadFalseAndSenderIdNot(UUID conversationId, String senderId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.conversation.id = :conversationId AND m.read = false AND m.senderId <> :userId")
    void markAllAsRead(@Param("conversationId") UUID conversationId, @Param("userId") String userId);
}
