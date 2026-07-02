package it.roadies.user_service.conf;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange("user.exchange");
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange("booking.exchange");
    }

    @Bean
    public Queue gamificationQueue() {
        return new Queue("gamification-queue-add", true);
    }

    @Bean
    public Queue gamificationQueueRemove() {
        return new Queue("gamification-queue-remove", true);
    }

    @Bean
    public Binding bindGamification() {
        return BindingBuilder.bind(gamificationQueue()).to(bookingExchange()).with("booking.gamification.points.add");
    }

    @Bean
    public Binding bindGamificationRemove() {
        return BindingBuilder.bind(gamificationQueueRemove()).to(bookingExchange()).with("booking.gamification.points.remove");
    }
}
