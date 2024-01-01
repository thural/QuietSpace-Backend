package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.controller.NotFoundException;
import dev.thural.quietspacebackend.entity.MessageEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.MessageMapper;
import dev.thural.quietspacebackend.model.MessageDTO;
import dev.thural.quietspacebackend.repository.MessageRepository;
import dev.thural.quietspacebackend.repository.UserRepository;
import dev.thural.quietspacebackend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;

    @Override
    public Page<MessageDTO> getChat(UUID firstUserId, UUID secondUserId, Integer pageNumber, Integer pageSize) {
        return null;
    }

    @Override
    public MessageDTO addOne(MessageDTO messageDTO, String jwtToken) {
        UserEntity sender = userRepository.findById(messageDTO.getSenderId()).orElseThrow(NotFoundException::new);
        UserEntity receiver = userRepository.findById(messageDTO.getReceiverId()).orElseThrow(NotFoundException::new);

        MessageEntity newMessage = messageMapper.messageDtoToEntity(messageDTO);
        newMessage.setSender(sender);
        newMessage.setReceiver(receiver);

        return messageMapper.messageEntityToDto(messageRepository.save(newMessage));
    }

    @Override
    public Optional<MessageDTO> getById(UUID messageId) {
        return null;
    }

    @Override
    public void updateOne(UUID messageId, MessageDTO messageDTO, String jwtToken) {

    }

    @Override
    public void deleteOne(UUID id, String jwtToken) {

    }

    @Override
    public void patchOne(UUID messageId, MessageDTO messageDTO, String jwtToken) {

    }
}
