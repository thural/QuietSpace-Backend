package dev.thural.quietspace.config;

import dev.thural.quietspace.security.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        var cutoff = OffsetDateTime.now().minusDays(1);
        int deleted = tokenRepository.deleteByExpireDateBefore(cutoff);
        if (deleted > 0) {
            log.info("cleaned up {} expired tokens", deleted);
        }
    }
}
