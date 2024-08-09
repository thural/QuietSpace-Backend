package dev.thural.quietspace.controller;

import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.service.ReactionService;
import dev.thural.quietspace.utils.enums.ContentType;
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

    @GetMapping("/user")
    Page<ReactionResponse> getReactionsByUserId(
            @RequestParam UUID userId,
            @RequestParam ContentType contentType,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return reactionService.getReactionsByUserIdAndContentType(userId, contentType, pageNumber, pageSize);
    }

    @GetMapping("/content")
    Page<ReactionResponse> getReactionsByContentId(
            @RequestParam UUID contentId,
            @RequestParam ContentType contentType,
            @RequestParam(name = "page-number", required = false) Integer pageNumber,
            @RequestParam(name = "page-size", required = false) Integer pageSize
    ) {
        return reactionService.getReactionsByContentIdAndContentType(contentId, contentType, pageNumber, pageSize);
    }

    @PostMapping("/toggle-reaction")
    ResponseEntity<?> toggleCommentLike(@RequestBody ReactionRequest reaction) {
        reactionService.handleReaction(reaction);
        return ResponseEntity.ok().build();
    }

}
