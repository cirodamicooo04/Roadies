package it.roadies.booking_service.data.dto.event;

import lombok.Data;

@Data
public class NotificationEvent {
    String to;
    String subject;
    String message;
}
