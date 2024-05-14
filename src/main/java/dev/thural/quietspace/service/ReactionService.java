package dev.thural.quietspace.service;

import dev.thural.quietspace.model.request.ReactionRequest;

public interface ReactionService {
    void handleReaction(ReactionRequest reaction);
}
