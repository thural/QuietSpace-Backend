package dev.thural.quietspace.mapper.custom;

import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.mapper.UserMapper;
import dev.thural.quietspace.model.request.ChatRequest;
import dev.thural.quietspace.model.response.ChatResponse;
import dev.thural.quietspace.model.response.MessageResponse;
import dev.thural.quietspace.model.response.UserResponse;
import dev.thural.quietspace.service.MessageService;
import dev.thural.quietspace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMapper {
    private final UserMapper userMapper;
    private final UserService userService;
    private final MessageService messageService;

    public Chat chatRequestToEntity(ChatRequest chatRequest){
        return Chat.builder()
                .users(getUserListFromRequest(chatRequest))
                .build();
    }

    public ChatResponse chatEntityToResponse(Chat chat){
        return ChatResponse.builder()
                .id(chat.getId())
                .userIds(getUserIdsFromChat(chat))
                .members(getChatMembers(chat))
                .recentMessage(getLastMessage(chat))
                .createDate(chat.getCreateDate())
                .updateDate(chat.getUpdateDate())
                .build();
    }

    private MessageResponse getLastMessage(Chat chat){
        return messageService.getLastMessageByChat(chat).orElse(null);
    }

    private List<UUID> getUserIdsFromChat(Chat chat){
        return chat.getUsers().stream().map(User::getId).toList();
    }

    private List<User> getUserListFromRequest(ChatRequest chatRequest){
        return userService.getUsersFromIdList(chatRequest.getUserIds());
    }

    private List<UserResponse> getChatMembers(Chat chat){
        User loggedUser = userService.getLoggedUser();
        return chat.getUsers().stream()
                .filter(user -> !user.equals(loggedUser))
                .map(userMapper::userEntityToResponse).toList();
    }
}
