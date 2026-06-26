package it.roadies.review_service.conf.auditor;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditorConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider()
    {
        return new Auditor();
    }
}
