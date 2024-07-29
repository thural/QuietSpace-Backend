package dev.thural.quietspace.service.impls;

import dev.thural.quietspace.entity.Reaction;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.custom.ReactionMapper;
import dev.thural.quietspace.model.request.ReactionRequest;
import dev.thural.quietspace.model.response.ReactionResponse;
import dev.thural.quietspace.repository.ReactionRepository;
import dev.thural.quietspace.service.UserService;
import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.LikeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

    @Mock
    private ReactionRepository reactionRepository;
    @Mock
    private ReactionMapper reactionMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private ReactionServiceImpl reactionService;

    private User user;
    private UUID userId;
    private Reaction reaction;
    private ReactionRequest reactionRequest;
    private UUID contentId;

    @BeforeEach
    void setUp() {
        this.userId = UUID.randomUUID();
        this.contentId = UUID.randomUUID();

        this.user = User.builder()
                .id(userId)
                .username("user")
                .email("user@email.com")
                .password("pAsSword")
                .build();

        this.reaction = Reaction.builder()
                .id(UUID.randomUUID())
                .likeType(LikeType.LIKE)
                .contentType(ContentType.POST)
                .contentId(contentId)
                .userId(userId)
                .username(user.getUsername())
                .build();

        this.reactionRequest = ReactionRequest.builder()
                .contentId(contentId)
                .contentType(reaction.getContentType())
                .userId(userId)
                .likeType(reaction.getLikeType())
                .build();
    }

    @Test
    void testHandleReactionRemoveLike() {
        when(userService.getSignedUser()).thenReturn(user);
        when(reactionRepository.findByContentIdAndUserId(contentId, userId)).thenReturn(Optional.of(reaction));

        reactionService.handleReaction(reactionRequest);

        verify(reactionRepository, times(1)).deleteById(reaction.getId());
    }

    @Test
    void testHandleReactionAddLike() {
        when(userService.getSignedUser()).thenReturn(user);
        when(reactionMapper.reactionRequestToEntity(any(ReactionRequest.class))).thenReturn(reaction);
        when(reactionRepository.findByContentIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        reactionService.handleReaction(ReactionRequest.builder()
                .userId(UUID.randomUUID())
                .contentId(UUID.randomUUID())
                .build());

        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }


    @Test
    void getUserReactionByContentId() {
        when(userService.getSignedUser()).thenReturn(user);
        when(reactionRepository.findByContentIdAndUserId(contentId, userId)).thenReturn(Optional.of(reaction));
        when(reactionMapper.reactionEntityToResponse(any(Reaction.class))).thenReturn(ReactionResponse.builder().build());

        reactionService.getUserReactionByContentId(contentId);

        assertThat(reactionService.getUserReactionByContentId(contentId)).isNotEmpty();
    }

    @Test
    void getLikesByContentId() {
        when(reactionRepository.findAllByContentIdAndLikeType(contentId, LikeType.LIKE)).thenReturn(List.of(reaction));
        when(reactionMapper.reactionEntityToResponse(any(Reaction.class))).thenReturn(ReactionResponse.builder().build());

        reactionService.getReactionsByContentIdAndLikeType(contentId, LikeType.LIKE);
        assertThat(reactionService.getReactionsByContentIdAndLikeType(contentId, LikeType.LIKE)).size().isEqualTo(1);
    }

    @Test
    void getLikeCountByContentId() {
        when(reactionRepository.countByContentIdAndLikeType(contentId, LikeType.LIKE)).thenReturn(3);
        reactionService.getLikeCountByContentIdAndLikeType(contentId, LikeType.LIKE);
        assertThat(reactionService.getLikeCountByContentIdAndLikeType(contentId, LikeType.LIKE)).isEqualTo(3);
    }

    @Test
    void getReactionsByContentId() {
        when(reactionRepository.findAllByContentIdAndContentType(contentId, ContentType.POST)).thenReturn(List.of(reaction));
        when(reactionMapper.reactionEntityToResponse(any(Reaction.class))).thenReturn(ReactionResponse.builder().build());

        reactionService.getReactionsByContentId(contentId, ContentType.POST);
        assertThat(reactionService.getReactionsByContentId(contentId, ContentType.POST)).isNotEmpty();
    }

    @Test
    void getReactionsByUserId() {
        when(reactionRepository.findAllByUserIdAndContentType(userId, ContentType.POST)).thenReturn(List.of(reaction));
        when(reactionMapper.reactionEntityToResponse(any(Reaction.class))).thenReturn(ReactionResponse.builder().build());

        reactionService.getReactionsByUserId(userId, ContentType.POST);
        assertThat(reactionService.getReactionsByUserId(userId, ContentType.POST)).isNotEmpty();
    }
}