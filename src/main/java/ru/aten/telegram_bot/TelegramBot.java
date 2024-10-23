package ru.aten.telegram_bot;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandDispatcher;
import ru.aten.telegram_bot.openai.ChatGPTService;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final ChatGPTService chatGPTService;
    private final TelegramCommandDispatcher telegramCommandDispatcher;

    public TelegramBot(
        @Value("${bot.token}") String botToken,
        ChatGPTService chatGPTService,
        TelegramCommandDispatcher telegramCommandDispatcher
    ) {
        super(new DefaultBotOptions(), botToken);
        this.chatGPTService = chatGPTService;
        this.telegramCommandDispatcher = telegramCommandDispatcher;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        try {
            var methods = processUpdate(update);
            methods.forEach(it -> {
                try {
                    sendApiMethod(it);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error("Error while processing update", e);
            sendUserErrorMessage(update.getMessage().getChatId());
        }

    }
    
    private List<BotApiMethod<?>> processUpdate(Update update) {
        if (telegramCommandDispatcher.isCommand(update)) {
            return List.of(telegramCommandDispatcher.processCommand(update));
        }
        
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
                    
                    return List.of(sendMessage);
                } catch (Exception e) {
                    SendMessage errorMessage = new SendMessage(chatId.toString(), "An error occurred: " + e.getMessage());
                    return List.of(errorMessage);
                }
            }
        }
        
        return List.of();
        
    }
    
    @SneakyThrows
    private void sendUserErrorMessage(Long userid) {
        sendApiMethod(SendMessage.builder()
            .chatId(userid)
            .text("Произошла ошибка, попробуйте позже")
            .build());
    }
    
    @Override
    public String getBotUsername() {
        return "DondonnyBot";
    }

}
