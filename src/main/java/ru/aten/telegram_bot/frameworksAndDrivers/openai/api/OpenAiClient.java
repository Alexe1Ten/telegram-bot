package ru.aten.telegram_bot.frameworksAndDrivers.openai.api;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OpenAiClient {

    private final String token;
    private final RestTemplate restTemplate;

    public ChatCompletionResponse createChatCompletion(
            ChatCompletionRequest chatCompletionRequest
    ) {
        String url = "https://api.proxyapi.ru/openai/v1/chat/completions";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);
        httpHeaders.set("Content-Type", "application/json");

        HttpEntity<ChatCompletionRequest> httpEntity = new HttpEntity<>(chatCompletionRequest, httpHeaders);

        try {
            ResponseEntity<ChatCompletionResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    ChatCompletionResponse.class
            );
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getResponseBodyAsString());
            throw e;
        }

    }

    public TranscriptionResponse createTranscription(
        CreateTranscriptionRequest createTranscriptionRequest
    ) {
        String url = "https://api.proxyapi.ru/openai/v1/audio/transcriptions";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);
        httpHeaders.set("Content-Type", "multipart/form-data");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(createTranscriptionRequest.audioFile()));
        body.add("model", createTranscriptionRequest.model());

        var httpEntity = new HttpEntity<>(body, httpHeaders);

        try {
            ResponseEntity<TranscriptionResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    TranscriptionResponse.class
            );
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
