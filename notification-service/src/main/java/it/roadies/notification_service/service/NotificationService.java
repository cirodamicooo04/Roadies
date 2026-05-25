package it.roadies.notification_service.service;

import it.roadies.notification_service.data.NotificationEvent;

public interface NotificationService {
    void sendMail(NotificationEvent event);

}
