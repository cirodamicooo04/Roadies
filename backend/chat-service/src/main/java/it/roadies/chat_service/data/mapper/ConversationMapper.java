package it.roadies.chat_service.data.mapper;

import it.roadies.chat_service.data.dto.response.ConversationResponseDTO;
import it.roadies.chat_service.data.entities.Conversation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationResponseDTO toDto(Conversation conversation);
}
