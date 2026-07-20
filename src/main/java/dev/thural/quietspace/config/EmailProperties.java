package dev.thural.quietspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.mail")
public record EmailProperties(
        String host,
        int port,
        String username,
        String password
) {}
