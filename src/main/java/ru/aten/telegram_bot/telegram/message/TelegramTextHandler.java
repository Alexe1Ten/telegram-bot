package ru.aten.telegram_bot.telegram.message;

import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.openai.ChatGPTService;

@Service
@AllArgsConstructor
public class TelegramTextHandler {

    private final ChatGPTService chatGPTService;

    public SendMessage processTextMessage(Message message) {

        var userId = message.getFrom().getId();
        var userName = message.getFrom().getFirstName();

        var gptGeneratedText = chatGPTService.getResponseChatForUser(message.getChatId(), message.getText());
        String escapedText = TextConverter.escapeMarkdownV2(gptGeneratedText);
        String messageText = String.format("[%s](tg://user?id=%d), %s", "*" + userName + "*", userId, escapedText);
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), messageText);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdownV2(true);
        sendMessage.setMessageThreadId(message.getMessageThreadId());
        sendMessage.setParseMode("MarkdownV2");

        if (message.getMessageThreadId() != null) {
            sendMessage.setMessageThreadId(message.getMessageThreadId());
        }

        return sendMessage;
    }

    public boolean isBotMentioned(Message message) {
        var text = message.getText();
        if (message.getChat().isGroupChat() || message.getChat().isSuperGroupChat()) {
            List<MessageEntity> entities = message.getEntities();
            return entities != null && entities.stream()
                    .anyMatch(entity -> entity.getType().equals("mention")
                    && text.substring(entity.getOffset(), entity.getOffset() + entity.getLength())
                            .equals("@" + "DondonnyBot"));
        }
        return false;
    }
}
