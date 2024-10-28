package ru.aten.telegram_bot.telegram.message;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        var gptGeneratedText = "[" + "*" + userName + "*" + "](tg://user?id=" + userId + "), " + chatGPTService.getResponseChatForUser(message.getChatId(), message.getText());
        String escapedText = escapeMarkdownV2(gptGeneratedText);
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), escapedText);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdownV2(true);
        sendMessage.setMessageThreadId(message.getMessageThreadId());
        sendMessage.setParseMode("MarkdownV2");

        if (message.getMessageThreadId() != null) {
            sendMessage.setMessageThreadId(message.getMessageThreadId());
        }

        return sendMessage;
    }

    public static String escapeMarkdownV2(String text) {
        return text
                .replace("(", "\\(") // Экранируем открывающую круглую скобку
                .replace(")", "\\)") // Экранируем закрывающую круглую скобку
                .replace("#", "") // Убираем решетку (если это нужно)
                .replace("+", "\\+") // Экранируем плюс
                .replace("-", "\\-") // Экранируем дефис
                .replace("=", "\\=") // Экранируем равно
                .replace("|", "\\|") // Экранируем вертикальную черту
                .replace("{", "\\{") // Экранируем фигурную скобку открывающую
                .replace("}", "\\}") // Экранируем фигурную скобку закрывающую
                .replace(".", "\\.") // Экранируем точку
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace("!", "\\!")
                .replace("'", "\\'")
                .replace("[", "\\[") // Экранируем открывающую квадратную скобку
                .replace("]", "\\]"); // Экранируем восклицательный знак
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
