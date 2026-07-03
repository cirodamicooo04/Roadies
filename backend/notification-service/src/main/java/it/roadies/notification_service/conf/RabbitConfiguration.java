package it.roadies.notification_service.conf;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange");
    }

    @Bean
    public Queue sendMailQueue() {
        return new Queue("send-mail-queue", true);
    }

    @Bean
    public Binding bindMailToNotificationExchange() {
        return BindingBuilder.bind(sendMailQueue()).to(notificationExchange()).with("notification.mail.send");
    }
}
