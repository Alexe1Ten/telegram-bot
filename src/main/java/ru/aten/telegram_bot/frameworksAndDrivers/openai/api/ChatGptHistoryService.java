package ru.aten.telegram_bot.frameworksAndDrivers.openai.api;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.entities.ChatHistory;
import ru.aten.telegram_bot.entities.Message;
import ru.aten.telegram_bot.interfaceAdapters.repositories.ChatHistoryRepository;
import ru.aten.telegram_bot.useCases.dto.ChatHistoryDTO;
import ru.aten.telegram_bot.useCases.dto.MessageDTO;

@Service
@AllArgsConstructor
public class ChatGptHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatHistoryMapper chatHistoryMapper;

    @Transactional(readOnly=true)
    public Optional<ChatHistoryDTO> getUserHistory(Long userid) {
        return chatHistoryRepository.findByUserId(userid)
            .map(chatHistoryMapper::toDTO);
    }

    @Transactional
    public void createHistory(
            Long userid
    ) {
        var chatHistory = ChatHistory.builder()
            .userId(userid)
            .messages(new ArrayList<>())
            .build();
        chatHistoryRepository.save(chatHistory);
    }

    @Transactional
    public void clearHistory(Long userid) {
        chatHistoryRepository.findByUserId(userid)
            .ifPresent(chatHistoryRepository::delete);
    }

    @Transactional
    public ChatHistoryDTO addMessageToHistory(
            Long userid,
            MessageDTO messageDTO
    ) {
        ChatHistory chatHistory = chatHistoryRepository.findByUserId(userid)
            .orElseThrow(() -> new IllegalStateException("History not found for user id: " + userid));

        Message message = chatHistoryMapper.toEntity(messageDTO);
        chatHistory.getMessages().add(message);
        chatHistoryRepository.save(chatHistory);

        return chatHistoryMapper.toDTO(chatHistory);
    }

    public void createHistoryIfNotExist(Long userId) {
        if (!chatHistoryRepository.findByUserId(userId).isPresent()) {
            createHistory(userId);
        }
    }
}
