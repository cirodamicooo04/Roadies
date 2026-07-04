package it.roadies.chat_service.data.mapper;

import it.roadies.chat_service.data.dto.response.MessageResponseDTO;
import it.roadies.chat_service.data.entities.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(target = "senderName", ignore = true)
    MessageResponseDTO toDto(Message message);
}
