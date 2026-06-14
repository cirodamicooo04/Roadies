package it.roadies.travel_service.conf;

import org.springframework.amqp.core.*;
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
    public TopicExchange bookingExchange() {
        return new TopicExchange("booking.exchange");
    }

    @Bean
    public TopicExchange travelExchange() {
        return new TopicExchange("travel.exchange");
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange("user.exchange");
    }

    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange("review.exchange");
    }

    @Bean
    public Queue travelReserveQueue() {
        return new Queue("travel.reserve.queue", true);
    }

    @Bean
    public Queue activityReserveQueue() {
        return new Queue("activity.reserve.queue", true);
    }

    @Bean
    public Queue travelReleaseQueue() {
        return new Queue("travel.release.queue", true);
    }

    @Bean
    public Queue activityReleaseQueue() {
        return new Queue("activity.release.queue", true);
    }

    @Bean
    public Queue friendshipAcceptedQueue() {
        return new Queue("travel-service.friendship.accepted.queue", true);
    }

    @Bean
    public Queue friendshipDeletedQueue() {
        return new Queue("travel-service.friendship.deleted.queue", true);
    }

    @Bean
    public Queue addedTravelReviewQueue() {
        return new Queue("travel-service.added.tr.review.queue", true);
    }

    @Bean
    public Queue addedActivityReviewQueue() {
        return new Queue("travel-service.added.ac.review.queue", true);
    }

    @Bean
    public Binding bindTravelReserve() {
        return BindingBuilder.bind(travelReserveQueue()).to(bookingExchange()).with("booking.seat.reserve.travel");
    }

    @Bean
    public Binding bindActivityReserve() {
        return BindingBuilder.bind(activityReserveQueue()).to(bookingExchange()).with("booking.seat.reserve.activity");
    }

    @Bean
    public Binding bindTravelRelease() {
        return BindingBuilder.bind(travelReleaseQueue()).to(bookingExchange()).with("booking.seat.release.travel");
    }

    @Bean
    public Binding bindActivityRelease() {
        return BindingBuilder.bind(activityReleaseQueue()).to(bookingExchange()).with("booking.seat.release.activity");
    }

    @Bean
    public Binding bindFriendshipAccepted() {
        return BindingBuilder.bind(friendshipAcceptedQueue()).to(userExchange()).with("user.friendship.accepted");
    }

    @Bean
    public Binding bindFriendshipDeleted() {
        return BindingBuilder.bind(friendshipDeletedQueue()).to(userExchange()).with("user.friendship.deleted");
    }

    @Bean
    public Binding bindTravelReview() {
        return BindingBuilder.bind(addedTravelReviewQueue()).to(reviewExchange()).with("review.travel.added");
    }

    @Bean
    public Binding bindActivityReview() {
        return BindingBuilder.bind(addedActivityReviewQueue()).to(reviewExchange()).with("review.activity.added");
    }
}
