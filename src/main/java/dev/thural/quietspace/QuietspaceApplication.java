package dev.thural.quietspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class QuietspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuietspaceApplication.class, args);
    }

}