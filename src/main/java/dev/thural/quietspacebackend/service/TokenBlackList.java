package dev.thural.quietspacebackend.service;

public interface TokenBlackList {
    void addToBlacklist(String token);
    boolean isBlacklisted(String token);
}
