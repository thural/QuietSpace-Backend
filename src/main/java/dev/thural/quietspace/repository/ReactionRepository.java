package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Page<Reaction> findAllByContentId(UUID contentId, PageRequest pageRequest);

    Page<Reaction> findAllByUserId(UUID userId, PageRequest pageRequest);

    boolean existsByContentIdAndUserId(UUID contentId, UUID userId);

    Optional<Reaction> findByContentIdAndUserId(UUID commentId, UUID id);

    Page<Reaction> findAllByContentTypeAndUserId(ContentType contentType, UUID userId, PageRequest pageRequest);

    Page<Reaction> findAllByContentIdAndContentType(UUID contentId, ContentType contentType, PageRequest pageRequest);

    Page<Reaction> findAllByUserIdAndContentType(UUID userId, ContentType contentType, PageRequest pageRequest);

    Integer countByContentIdAndReactionType(UUID contentId, ReactionType reactionType);

    Page<Reaction> findAllByContentIdAndReactionType(UUID contentId, ReactionType reactionType, PageRequest pageRequest);
}