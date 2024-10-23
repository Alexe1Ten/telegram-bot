package ru.aten.telegram_bot;

import java.util.List;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.SneakyThrows;
import ru.aten.telegram_bot.openai.ChatGPTService;

public class TelegramBot extends TelegramLongPollingBot {

    private final ChatGPTService chatGPTService;

    public TelegramBot(DefaultBotOptions options, String botToken, ChatGPTService chatGPTService) {
        super(options, botToken);
        this.chatGPTService = chatGPTService;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            // Получаем список всех сущностей сообщения
            List<MessageEntity> entities = update.getMessage().getEntities();

            // Проверяем, есть ли упоминание бота
            boolean botMentioned = entities != null && entities.stream()
                    .anyMatch(entity -> entity.getType().equals("mention")
                    && update.getMessage().getText().substring(entity.getOffset(), entity.getLength())
                            .equals("@" + getBotUsername()));

            if (botMentioned) {
                try {
                    var gptGeneratedText = chatGPTService.getResponseChatForUser(chatId, text);
                    SendMessage sendMessage = new SendMessage(chatId.toString(), gptGeneratedText);
                    sendApiMethod(sendMessage);
                } catch (Exception e) {
                    SendMessage errorMessage = new SendMessage(chatId.toString(), "An error occurred: " + e.getMessage());
                    sendApiMethod(errorMessage);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "DondonnyBot";
    }

}
