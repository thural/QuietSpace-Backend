package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

}
