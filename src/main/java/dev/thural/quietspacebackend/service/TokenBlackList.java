package dev.thural.quietspacebackend.service;

public interface TokenBlackList {
    void addToBlacklist(String authHeader);
    boolean isBlacklisted(String authHeader);
}
