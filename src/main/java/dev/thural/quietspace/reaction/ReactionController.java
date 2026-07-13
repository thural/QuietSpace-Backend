package dev.thural.quietspace.reaction;

import dev.thural.quietspace.shared.enums.EntityType;
import dev.thural.quietspace.shared.enums.ReactionType;
import dev.thural.quietspace.reaction.dto.ReactionRequest;
import dev.thural.quietspace.reaction.dto.ReactionResponse;
import dev.thural.quietspace.service.NotificationService;
import dev.thural.quietspace.reaction.ReactionService;
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
            @RequestParam EntityType contentType,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return reactionService.getReactionsByUserIdAndContentType(userId, contentType, pageNumber, pageSize);
    }

    @GetMapping("/content")
    Page<ReactionResponse> getReactionsByContent(
            @RequestParam UUID contentId,
            @RequestParam EntityType contentType,
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
