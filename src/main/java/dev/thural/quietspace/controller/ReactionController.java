package dev.thural.quietspace.controller;

import dev.thural.quietspace.enums.ContentType;
import dev.thural.quietspace.enums.ReactionType;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;
    private final NotificationService notificationService;

    @GetMapping("/user")
    Page<ReactionResponse> getReactionsByUser(
            @RequestParam UUID userId,
            @RequestParam ContentType contentType,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return reactionService.getReactionsByUserIdAndContentType(userId, contentType, pageNumber, pageSize);
    }

    @GetMapping("/content")
    Page<ReactionResponse> getReactionsByContent(
            @RequestParam UUID contentId,
            @RequestParam ContentType contentType,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return reactionService.getReactionsByContentIdAndContentType(contentId, contentType, pageNumber, pageSize);
    }

    @PostMapping("/toggle-reaction")
    ResponseEntity<?> toggleReaction(@RequestBody ReactionRequest reaction) {
        reactionService.handleReaction(reaction);
        notificationService.processNotificationByReaction(reaction.getContentType(), reaction.getContentId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    ResponseEntity<Integer> countByContentIdAndReactionType(@RequestParam UUID contentId, @RequestParam ReactionType type) {
        return ResponseEntity.ok(reactionService.countByContentIdAndReactionType(contentId, type));
    }

}
