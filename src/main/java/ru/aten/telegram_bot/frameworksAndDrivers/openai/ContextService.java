package ru.aten.telegram_bot.frameworksAndDrivers.openai;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.frameworksAndDrivers.openai.api.ChatGptHistoryService;
import ru.aten.telegram_bot.useCases.dto.MessageDTO;

@Service
@AllArgsConstructor
public class ContextService {

    private final ChatGptHistoryService gptHistoryService;

    public List<MessageDTO> getContextForUser(Long userId) {
        
        return gptHistoryService.getUserHistory(userId).get().getMessages();
    }

    public void addContextMessage(Long userId, String content) {
        MessageDTO contextMessage = new MessageDTO();
        contextMessage.setContent(content);
        contextMessage.setRole("system");

        gptHistoryService.addMessageToHistory(userId, contextMessage);
    }
}
