package it.roadies.notification_service.data;

import lombok.Data;

@Data
public class NotificationEvent {
    String to;
    String subject;
    String message;
}
