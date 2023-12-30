package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.TokenEntity;
import dev.thural.quietspacebackend.repository.TokenRepository;
import dev.thural.quietspacebackend.service.TokenBlackList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlackListImpl implements TokenBlackList {

    private final TokenRepository tokenRepository;

    @Override
    public void addToBlacklist(String token) {
        boolean isBlacklisted = tokenRepository.existsByJwtToken(token);
        if (!isBlacklisted) tokenRepository.save(TokenEntity.builder().jwtToken(token).build());
    }

    @Override
    public boolean isBlacklisted(String token) {
        return tokenRepository.existsByJwtToken(token);
    }
}
