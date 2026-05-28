package it.roadies.notification_service.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationEvent {
    @NotBlank
    private String to;
    private String subject;
    @NotBlank
    private String message;
}
