package dev.thural.quietspace.message;

import dev.thural.quietspace.chat.Chat;
import dev.thural.quietspace.message.Message;
import dev.thural.quietspace.photo.Photo;
import dev.thural.quietspace.user.User;
import dev.thural.quietspace.shared.enums.EntityType;
import dev.thural.quietspace.message.MessageMapper;
import dev.thural.quietspace.message.dto.MessageRequest;
import dev.thural.quietspace.message.dto.MessageResponse;
import dev.thural.quietspace.chat.ChatRepository;
import dev.thural.quietspace.message.MessageRepository;
import dev.thural.quietspace.message.MessageService;
import dev.thural.quietspace.photo.PhotoService;
import dev.thural.quietspace.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static dev.thural.quietspace.shared.util.PagingProvider.DEFAULT_SORT_OPTION;
import static dev.thural.quietspace.shared.util.PagingProvider.buildPageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper messageMapper;
    private final PhotoService photoService;
    private final UserService userService;

    @Override
    @Transactional
    public MessageResponse addMessage(MessageRequest messageRequest) {
        User loggedUser = userService.getSignedUser();
        Chat parentChat = chatRepository.findById(messageRequest.getChatId()).orElseThrow(EntityNotFoundException::new);
        Message newMessage = messageMapper.toEntity(messageRequest);
        newMessage.setSender(loggedUser);
        newMessage.setChat(parentChat);
        Message savedMessage = messageRepository.save(newMessage);
        if (messageRequest.getPhotoData() != null) saveMessagePhoto(messageRequest, newMessage);
        return messageMapper.toResponse(savedMessage);
    }

    @Override
    @Transactional
    public Optional<MessageResponse> deleteMessage(UUID messageId) {
        Message existingMessage = findMessageOrElseThrow(messageId);
        checkResourceAccess(existingMessage.getSender().getId());
        messageRepository.deleteById(messageId);
        photoService.deletePhotoByEntityId(messageId);
        return Optional.of(messageMapper.toResponse(existingMessage));
    }

    private Message findMessageOrElseThrow(UUID messageId) {
        return messageRepository.findById(messageId).orElseThrow(EntityNotFoundException::new);
    }

    private void checkResourceAccess(UUID userId) {
        User loggedUser = userService.getSignedUser();
        if (!userId.equals(loggedUser.getId())) throw new AccessDeniedException("message user mismatch");
    }

    @Override
    public Page<MessageResponse> getMessagesByChatId(Integer pageNumber, Integer pageSize, UUID chatId) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, DEFAULT_SORT_OPTION);
        Page<Message> messagePage = messageRepository.findAllByChatId(chatId, pageRequest);
        return messagePage.map(messageMapper::toResponse);
    }

    @Override
    public Optional<MessageResponse> getLastMessageByChat(Chat chat) {
        return messageRepository.findFirstByChatOrderByCreateDateDesc(chat).map(messageMapper::toResponse);
    }

    @Override
    public Optional<MessageResponse> setMessageSeen(UUID messageId) {
        Message existingMessage = findMessageOrElseThrow(messageId);
        existingMessage.setIsSeen(true);
        Message savedMessage = messageRepository.save(existingMessage);
        return Optional.ofNullable(messageMapper.toResponse(savedMessage));
    }

    @Override
    public MessageResponse getMessageById(UUID messageId, UUID chatId) {
        return messageRepository.findByMessageIdAndChatId(messageId, chatId)
                .map(messageMapper::toResponse)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public long getUnreadCount() {
        User loggedUser = userService.getSignedUser();
        return messageRepository.countByRecipientIdAndIsSeen(loggedUser.getId(), false);
    }

    private void saveMessagePhoto(MessageRequest request, Message message) {
        Photo savedPhoto = photoService.persistPhotoEntity(request.getPhotoData(), message.getId(), EntityType.MESSAGE);
        message.setPhotoId(savedPhoto.getId());
    }

}