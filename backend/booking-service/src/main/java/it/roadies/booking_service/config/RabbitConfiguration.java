package it.roadies.booking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {


    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange("booking.exchange");
    }

    @Bean
    public TopicExchange travelExchange() {
        return new TopicExchange("travel.exchange");
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange");
    }

    @Bean
    public Queue bookingReservedQueue() {
        return new Queue("booking.reserved.queue", true);
    }

    @Bean
    public Queue bookingFailedQueue() {
        return new Queue("booking.failed.queue", true);
    }

    @Bean
    public Queue bookingExpirationQueue() {
        return new Queue("booking-expiration-queue", true);
    }

    @Bean
    public Queue bookingDelayPaymentQueue() {
        return QueueBuilder.durable("booking-delay-payment-queue")
                .withArgument("x-message-ttl", 300000)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "booking-expiration-queue")
                .build();
    }

    @Bean
    public Queue bookingDelayQueue() {
        return QueueBuilder.durable("booking-delay-queue")
                .withArgument("x-message-ttl", 900000)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "booking-expiration-queue")
                .build();
    }

    @Bean
    public Binding bindReservedToTravelExchange() {
        return BindingBuilder.bind(bookingReservedQueue()).to(travelExchange()).with("travel.seat.reserved");
    }

    @Bean
    public Binding bindFailedToTravelExchange() {
        return BindingBuilder.bind(bookingFailedQueue()).to(travelExchange()).with("travel.seat.failed");
    }
}
