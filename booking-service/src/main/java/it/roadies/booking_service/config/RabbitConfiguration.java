package it.roadies.booking_service.config;

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
}
