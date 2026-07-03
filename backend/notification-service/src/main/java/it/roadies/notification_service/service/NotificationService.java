package it.roadies.notification_service.service;

import it.roadies.shared.contracts.NotificationEvent;

public interface NotificationService {
    void sendMail(NotificationEvent event);

}
