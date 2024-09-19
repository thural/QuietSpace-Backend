package dev.thural.quietspace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditionConfiguration {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return new ApplicationAuditAware();
    }

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return new OffsetDateTimeProvider();
    }

}
