package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.ChatEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
    List<ChatEntity> findAllByOwnerId(UUID ownerId);

    List<ChatEntity> findAllByMembersId(UUID memberId);

    @Query("SELECT c FROM ChatEntity c WHERE c.owner = :owner")
    List<ChatEntity> findAllByOwner(UserEntity owner);
}
