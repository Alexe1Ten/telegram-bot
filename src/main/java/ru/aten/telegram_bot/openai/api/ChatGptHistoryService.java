package ru.aten.telegram_bot.openai.api;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatGptHistoryService {

    private final Map<Long, ChatHistory> chatHistoryMap = new ConcurrentHashMap<>();

    public Optional<ChatHistory> getUserHistory(
            Long userid
    ) {
        return Optional.ofNullable(chatHistoryMap.get(userid));
    }

    public void createHistory(
            Long userid
    ) {
        chatHistoryMap.put(userid, new ChatHistory(new ArrayList<>()));
    }

    public void clearHistory(Long userid) {
        chatHistoryMap.remove(userid);
    }

    public ChatHistory addMessageToHistory(
            Long userid,
            Message message
    ) {
        var chatHistory = chatHistoryMap.get(userid);
        if (chatHistory == null) {
            throw new IllegalStateException("History not found for user id: " + userid);
        }

        chatHistory.chatMessages().add(message);

        return chatHistory;
    }

    public void createHistoryIfNotExist(Long userId) {
        if (!chatHistoryMap.containsKey(userId)) {
            createHistory(userId);
        }
    }
}
