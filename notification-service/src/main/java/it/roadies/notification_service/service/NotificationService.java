package it.roadies.notification_service.service;

import it.roadies.notification_service.data.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String mail;

    @RabbitListener(queues = "send-mail-queue")
    public void sendMail(NotificationEvent event) {
        senderMail(event.getTo(), event.getSubject(), event.getMessage());
    }

    private void senderMail(String to, String subject, String text){
        log.info("Provo ad inviare la mail");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        log.info("Email inviata");
    }
}
