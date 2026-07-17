package dev.thural.quietspace.chat;

import dev.thural.quietspace.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findAllByUsersId(UUID userId);

    List<Chat> findAllByUsersIn(Collection<User> users);
}
