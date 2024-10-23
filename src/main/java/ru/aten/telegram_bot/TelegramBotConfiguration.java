package ru.aten.telegram_bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.SneakyThrows;
import ru.aten.telegram_bot.openai.ChatGPTService;

@Configuration
public class TelegramBotConfiguration {

    @Bean
    @SneakyThrows
    public TelegramBot telegramBot(
            @Value("${bot.token}") String botToken,
            TelegramBotsApi telegramBotsApi,
            ChatGPTService chatGPTService
    ) {
        var botOptions = new DefaultBotOptions();
        var bot = new TelegramBot(botOptions, botToken, chatGPTService);
        telegramBotsApi.registerBot(bot);
        return bot;
    }

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotApi() {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}
