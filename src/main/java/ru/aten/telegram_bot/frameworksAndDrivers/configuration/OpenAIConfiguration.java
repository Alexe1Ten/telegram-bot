package ru.aten.telegram_bot.frameworksAndDrivers.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.aten.telegram_bot.frameworksAndDrivers.openai.api.OpenAiClient;

@Configuration
public class OpenAIConfiguration {

    @Bean
    public OpenAiClient openAiClient(
            @Value("${openai.token}") String openaiToken,
            RestTemplateBuilder restTemplateBuilder
    ) {
        return new OpenAiClient(openaiToken, restTemplateBuilder.build());
    }

}
