package ru.aten.telegram_bot.frameworksAndDrivers.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.SneakyThrows;
import ru.aten.telegram_bot.frameworksAndDrivers.telegram.TelegramBot;

@Configuration
public class TelegramBotConfiguration {

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotApi(TelegramBot bot) {
        var telegramBotApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotApi.registerBot(bot);
        return telegramBotApi;
    }
}
