package it.roadies.travel_service.conf;

import org.springframework.amqp.core.Queue;
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

}

