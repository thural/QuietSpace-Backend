package dev.thural.quietspace.bootstrap;

import dev.thural.quietspace.entity.Token;
import dev.thural.quietspace.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleaner {
    private final TokenRepository tokenRepository;

    @Scheduled(fixedRate = 86400000L)
    void tokenCleanup() {
        log.info("running token cleanup...");
        for (Token currentToken : tokenRepository.findAll()) {
            boolean isTokenExpired = currentToken.getCreateDate().isBefore(OffsetDateTime.now().minusDays(1));
            if (isTokenExpired) tokenRepository.delete(currentToken);
        }
    }
}
