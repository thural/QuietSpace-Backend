package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.model.ChatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
    List<ChatDTO> findAllByOwnerId(UUID ownerId);

    List<ChatDTO> findAllByMembersId(UUID memberId);

//    List<ChatDTO> findChatsWhereUserExistsInMembers(UserEntity loggedUser);

    @Query("SELECT c FROM ChatEntity c WHERE c.owner = :owner")
    List<ChatEntity> findAllByOwner(UserEntity owner);
}
