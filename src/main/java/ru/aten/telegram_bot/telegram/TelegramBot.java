package ru.aten.telegram_bot.telegram;

import java.io.File;
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
import ru.aten.telegram_bot.openai.api.TranscribeVoiceToTextService;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final ChatGPTService chatGPTService;
    private final TelegramFileService telegramFileService;
    private final TelegramCommandDispatcher telegramCommandDispatcher;
    private final TranscribeVoiceToTextService transcribeVoiceToTextService;

    public TelegramBot(
            @Value(value = "${bot.token}") String botToken,
            ChatGPTService chatGPTService,
            TelegramCommandDispatcher telegramCommandDispatcher,
            TelegramFileService telegramFileService,
            TranscribeVoiceToTextService transcribeVoiceToTextService
    ) {
        super(new DefaultBotOptions(), botToken);
        this.chatGPTService = chatGPTService;
        this.telegramCommandDispatcher = telegramCommandDispatcher;
        this.telegramFileService = telegramFileService;
        this.transcribeVoiceToTextService = transcribeVoiceToTextService;
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
        var message = update.getMessage();
        var chatId = message.getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = message.getText();

            if (message.getChat().isGroupChat() || message.getChat().isSuperGroupChat()) {

                List<MessageEntity> entities = update.getMessage().getEntities();

                // Проверяем, есть ли упоминание бота
                boolean botMentioned = entities != null && entities.stream()
                        .anyMatch(entity -> entity.getType().equals("mention")
                        && text.substring(entity.getOffset(), entity.getOffset() + entity.getLength())
                                .equals("@" + getBotUsername()));

                if (botMentioned) {
                    try {
                        var gptGeneratedText = "@" + message.getFrom().getFirstName() + " " + chatGPTService.getResponseChatForUser(message.getChatId(), message.getText());
                        String escapedText = escapeMarkdownV2(gptGeneratedText);
                        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), escapedText);
                        sendMessage.setReplyToMessageId(message.getMessageId());
                        sendMessage.setParseMode("MarkdownV2");

                        return List.of(sendMessage);
                    } catch (Exception e) {
                        SendMessage errorMessage = new SendMessage(chatId.toString(), "An error occurred: " + e.getMessage());
                        return List.of(errorMessage);
                    }
                }
            }
            var gptGeneratedText = message.getFrom().getFirstName() + " " + chatGPTService.getResponseChatForUser(message.getChatId(), message.getText());
            String escapedText = escapeMarkdownV2(gptGeneratedText);
            SendMessage sendMessage = new SendMessage(message.getChatId().toString(), escapedText);
            sendMessage.setReplyToMessageId(message.getMessageId());
            sendMessage.setParseMode("MarkdownV2");
            return List.of(sendMessage);
        }

        if (update.hasMessage() && update.getMessage().hasVoice()) {
            var voice = update.getMessage().getVoice();
            var fileId = voice.getFileId();
            try {
                File file = telegramFileService.getFile(fileId);

                var text = transcribeVoiceToTextService.transcribe(file);

                if (text.trim().toLowerCase().startsWith("бот")) {
                    var gptGeneratedText = "@" + message.getFrom().getFirstName() + " " + chatGPTService.getResponseChatForUser(message.getChatId(), text);
                    String escapedText = escapeMarkdownV2(gptGeneratedText);
                    SendMessage sendMessage = new SendMessage(message.getChatId().toString(), escapedText);
                    sendMessage.setReplyToMessageId(message.getMessageId());
                    sendMessage.enableMarkdownV2(true);
                    sendMessage.setParseMode("MarkdownV2");

                    return List.of(sendMessage);

                }
            } catch (Exception e) {
                SendMessage errorMessage = new SendMessage(chatId.toString(), "An error occurred: " + e.getMessage());
                return List.of(errorMessage);
            }

        }

        return List.of();

    }


    public String escapeMarkdownV2(String text) {
        return text
                .replace("\\", "\\") // Экранируем обратный слэш
                .replace("_", "\\_") // Экранируем нижнее подчеркивание
                .replace("*", "\\*") // Экранируем звездочку
                .replace("[", "\\[") // Экранируем открывающую квадратную скобку
                .replace("]", "\\]") // Экранируем закрывающую квадратную скобку
                .replace("(", "\\(") // Экранируем открывающую скобку
                .replace(")", "\\)") // Экранируем закрывающую скобку
                .replace("~", "\\~") // Экранируем тильду
                .replace(">", "\\>") // Экранируем символ 'больше'
                .replace("#", "\\#") // Экранируем символ 'решетка'
                .replace("+", "\\+") // Экранируем плюс
                .replace("-", "\\-") // Экранируем дефис
                .replace("=", "\\=") // Экранируем равно
                .replace("|", "\\|") // Экранируем вертикальную черту
                .replace("{", "\\{") // Экранируем открывающую фигурную скобку
                .replace("}", "\\}") // Экранируем закрывающую фигурную скобку
                .replace(".", "\\.") // Экранируем точку
                .replace("!", "\\!");   // Экранируем восклицательный знак
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
