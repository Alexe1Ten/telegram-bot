package ru.aten.telegram_bot.useCases.telegram;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramFileService {

    private final DefaultAbsSender telegramSender;
    private final String botToken;

    public TelegramFileService(
            @Value("${bot.token}") String botToken,
            @Lazy DefaultAbsSender telegramSender
    ) {
        this.botToken = botToken;
        this.telegramSender = telegramSender;
    }

    public java.io.File getFile(String fileId) throws TelegramApiException, MalformedURLException {
        File file = telegramSender.execute(GetFile.builder()
                .fileId(fileId)
                .build()
        );
        var urlToDownloadFile = file.getFileUrl(botToken);

        return getFileFromUrl(urlToDownloadFile);

    }

    @SneakyThrows
    private java.io.File getFileFromUrl(String urlToDownloadFile) {
        URL url = new URI(urlToDownloadFile).toURL();
        var fileTemp = java.io.File.createTempFile("telegram", ".ogg");

        try(InputStream inputStream = url.openStream();
            FileOutputStream fileOutputStream = new FileOutputStream(fileTemp)
        ) {
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            log.error("Error while dounloading file", e);
            throw new RuntimeException("Error while dounloading file", e);
        }
        return fileTemp;
    }

    public void sendFile(SendDocument sendDocument) {
        // telegramBot.execute(sendDocument);
        telegramSender.executeAsync(sendDocument);
    }
}
