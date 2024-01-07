package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.ChatDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {

    List<ChatDTO> findChatsByOwnerId(UUID ownerId);

    List<ChatDTO> findChatsWhereUserExistsInMembers(UserEntity loggedUser);
}
