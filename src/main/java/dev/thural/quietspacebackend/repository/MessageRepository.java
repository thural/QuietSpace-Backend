package dev.thural.quietspacebackend.repository;

import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.model.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    Page<MessageEntity> findAllByChatId(UUID chatId, Pageable pageable);
}
