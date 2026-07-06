package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findAllByUsersId(UUID userId);

    List<Chat> findAllByUsersIn(List<User> userList);
}
