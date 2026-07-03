package it.roadies.shared.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@ConditionalOnClass(name = "org.springframework.data.jpa.repository.config.EnableJpaAuditing")
public class AuditorConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new Auditor();
    }
}
