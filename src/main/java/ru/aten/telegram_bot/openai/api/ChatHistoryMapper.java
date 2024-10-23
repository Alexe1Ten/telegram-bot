package ru.aten.telegram_bot.openai.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ru.aten.telegram_bot.dto.ChatHistoryDTO;
import ru.aten.telegram_bot.dto.MessageDTO;
import ru.aten.telegram_bot.model.ChatHistory;
import ru.aten.telegram_bot.model.Message;

@Component
public class ChatHistoryMapper {

    public ChatHistoryDTO toDTO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }

        List<MessageDTO> messageDTOs = chatHistory.getMessages().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ChatHistoryDTO(
                chatHistory.getId(),
                chatHistory.getUserId(),
                messageDTOs
        );
    }

    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }

        return new MessageDTO(
                message.getId(),
                message.getContent(),
                message.getRole()
        );
    }

    public ChatHistory toEntity(ChatHistoryDTO chatHistoryDTO) {
        if (chatHistoryDTO == null) {
            return null;
        }

        List<Message> messages = chatHistoryDTO.getMessages().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        return new ChatHistory(
                chatHistoryDTO.getId(),
                chatHistoryDTO.getUserId(),
                messages
        );
    }

    public Message toEntity(MessageDTO messageDTO) {
        if (messageDTO == null) {
            return null;
        }

        return new Message(
            messageDTO.getId(),
            messageDTO.getContent(),
            messageDTO.getRole(),
            null
        );
    }
}
