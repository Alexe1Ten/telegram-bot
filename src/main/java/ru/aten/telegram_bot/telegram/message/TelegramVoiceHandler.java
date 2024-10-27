package ru.aten.telegram_bot.telegram.message;

import java.io.File;
import java.net.MalformedURLException;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.openai.ChatGPTService;
import ru.aten.telegram_bot.openai.api.TranscribeVoiceToTextService;
import ru.aten.telegram_bot.telegram.TelegramFileService;

@Service
@AllArgsConstructor
public class TelegramVoiceHandler {

    private final TelegramFileService telegramFileService;
    private final TranscribeVoiceToTextService transcribeVoiceToTextService;
    private final ChatGPTService chatGPTService;

    public SendMessage processVoice(Message message) throws MalformedURLException, TelegramApiException {
        var fileId = message.getVoice().getFileId();
        var transcribeText = getTranscribeText(fileId);
        var gptGeneratedText = getGeneratedGptText(message, transcribeText);
        String escapedText = TelegramTextHandler.escapeMarkdownV2(gptGeneratedText);
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), escapedText);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setParseMode("MarkdownV2");

        if (message.getMessageThreadId() != null) {
            sendMessage.setMessageThreadId(message.getMessageThreadId());
        }
        return sendMessage;

    }

    private String getGeneratedGptText(Message message, String text) throws TelegramApiException, MalformedURLException {
        var gptGeneratedText = "@" + message.getFrom().getFirstName() + " " + chatGPTService.getResponseChatForUser(message.getChatId(), text);
        return gptGeneratedText;
    }

    public String getTranscribeText(String fileId) throws MalformedURLException, TelegramApiException {
        File file = telegramFileService.getFile(fileId);
        return transcribeVoiceToTextService.transcribe(file);
    }

    public boolean isBotVoiceCommand(Message message) {
        var fileId = message.getVoice().getFileId();
        String transcribeText;
        try {
            transcribeText = getTranscribeText(fileId);
            return transcribeText != null && transcribeText.trim().toLowerCase().startsWith("бот");
        } catch (MalformedURLException | TelegramApiException e) {
            throw new RuntimeException("Ошибка!");
        }
    }

}
