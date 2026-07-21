package dev.thural.quietspace.post;

import dev.thural.quietspace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("postSecurity")
@RequiredArgsConstructor
public class PostSecurityService {

    private final PostService postService;
    private final UserRepository userRepository;

    public boolean canAccess(UUID postId, String username) {
        var user = userRepository.findUserEntityByEmail(username)
                .or(() -> userRepository.findUserByUsername(username));
        if (user.isEmpty()) return false;
        return postService.getPostById(postId)
                .map(post -> post.getUserId().equals(user.get().getId().toString()))
                .orElse(false);
    }
}
