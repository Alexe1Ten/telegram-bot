package ru.aten.telegram_bot.useCases.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryDTO {

    private Long id;
    private Long userId;
    private List<MessageDTO> messages;
}
