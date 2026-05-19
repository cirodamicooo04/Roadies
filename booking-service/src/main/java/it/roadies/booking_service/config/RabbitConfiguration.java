package it.roadies.booking_service.config;

import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue reservedQueue() {
        return new Queue("booking.reserved.queue", true);
    }

    @Bean
    public Queue failedQueue() {
        return new Queue("booking.failed.queue", true);
    }

    @Bean
    public Queue expirationQueue() {
        return new Queue("booking-expiration-queue", true);
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable("booking-delay-queue")
                .withArgument("x-message-ttl", 900000)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "booking-expiration-queue")
                .build();
    }
}
