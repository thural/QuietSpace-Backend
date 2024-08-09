package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.utils.enums.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    @GetMapping("/user")
    List<ReactionResponse> getReactionsByUserId(@RequestParam UUID userId, @RequestParam ContentType contentType) {
        return reactionService.getReactionsByUserIdAndContentType(userId, contentType);
    }

    @GetMapping("/content")
    List<ReactionResponse> getReactionsByContentId(@RequestParam UUID contentId, @RequestParam ContentType contentType) {
        return reactionService.getReactionsByContentIdAndContentType(contentId, contentType);
    }

    @PostMapping("/toggle-reaction")
    ResponseEntity<?> toggleCommentLike(@RequestBody ReactionRequest reaction) {
        reactionService.handleReaction(reaction);
        return ResponseEntity.ok().build();
    }

}
