package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
    List<ChatEntity> findAllByUsersId(UUID userId);

    List<ChatEntity> findAllByUsersIn(List<UserEntity> userList);
}
