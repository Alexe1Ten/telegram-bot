package ru.aten.telegram_bot.useCases.command.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.entities.Displayable;
import ru.aten.telegram_bot.entities.User;
import ru.aten.telegram_bot.entities.UserInfo;
import ru.aten.telegram_bot.useCases.EmailService;
import ru.aten.telegram_bot.useCases.UserService;
import ru.aten.telegram_bot.useCases.command.TelegramCommandHandler;
import ru.aten.telegram_bot.useCases.enums.ExportType;
import ru.aten.telegram_bot.useCases.enums.TelegramCommands;
import ru.aten.telegram_bot.useCases.telegram.TelegramFileService;

@Slf4j
@Component
@AllArgsConstructor
public class ExportHandler implements TelegramCommandHandler {

    private final UserService userService;
    private final TelegramFileService telegramFileService;
    private final EmailService emailService;
    private final List<ExportType> exportTypes = Arrays.asList(ExportType.values());

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (ExportType type : exportTypes) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(type.getTypeValue())
                    .callbackData("export:" + type.name())
                    .build();
            keyboard.add(Collections.singletonList(button));
        }

        keyboardMarkup.setKeyboard(keyboard);

        return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Какие данные вы хотите экспортировать?")
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData().split(":")[1].toLowerCase();
        return switch (data) {
            case "users" ->
                exportUsers(callbackQuery);
            case "schedule" ->
                exportSchedule(callbackQuery);
            default ->
                EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId().toString())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text("Неизвестный тип экспорта.")
                .build();
        };
    }

    private BotApiMethod<?> exportUsers(CallbackQuery callbackQuery) {
        try (Workbook workbook = new XSSFWorkbook()) {
            File file = File.createTempFile("Users", ".xlsx");
            Sheet sheet = workbook.createSheet("Users");

            Row headerRow = sheet.createRow(0);

            Field[] userFields = User.class.getDeclaredFields();
            Field[] userInfoFields = UserInfo.class.getDeclaredFields();

            int columnIndex = 0;
            for (Field field : userFields) {
                Displayable displayable = field.getAnnotation(Displayable.class);
                if (displayable != null && !displayable.value()) {
                    continue;
                }
                headerRow.createCell(columnIndex++).setCellValue(field.getName());
            }
            for (Field field : userInfoFields) {
                Displayable displayable = field.getAnnotation(Displayable.class);
                if (displayable != null && !displayable.value()) {
                    continue;
                }
                headerRow.createCell(columnIndex++).setCellValue(field.getName());
            }

            List<User> users = userService.getAllUsers();
            int rowIndex = 1;

            for (User user : users) {
                Row row = sheet.createRow(rowIndex++);
                columnIndex = 0;

                for (Field field : userFields) {
                    Displayable displayable = field.getAnnotation(Displayable.class);
                    if (displayable != null && !displayable.value()) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(user);
                    row.createCell(columnIndex++).setCellValue(value != null ? value.toString() : "");
                }

                UserInfo userInfo = user.getUserInfo();
                if (userInfo != null) {
                    for (Field field : userInfoFields) {
                        Displayable displayable = field.getAnnotation(Displayable.class);
                        if (displayable != null && !displayable.value()) {
                            continue;
                        }
                        field.setAccessible(true);
                        Object value = field.get(userInfo);
                        row.createCell(columnIndex++).setCellValue(value != null ? value.toString() : "");
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            boolean res = sendFileToUser(callbackQuery, file);
            if (!res) {
                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("Ошибка при отправке файла.")
                        .build();
            }
            return SendMessage.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .text("Экспорт данных успешно завершен.")
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при экспорте пользователей в Excel", e);
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Ошибка при экспорте данных.")
                    .build();
        }
    }

    private BotApiMethod<?> exportSchedule(CallbackQuery callbackQuery) {
        return null;
    }

    private boolean sendFileToUser(CallbackQuery callbackQuery, File file) {
        if (!file.exists()) {
            log.error("Файл не существует или недоступен: " + file.getAbsolutePath());
            return false;
        }
        SendDocument sendDocument = SendDocument.builder()
                .chatId(callbackQuery.getMessage().getChatId().toString())
                .document(new InputFile(file))
                .build();
        try {
            telegramFileService.sendFile(sendDocument);
            emailService.sendEmailWithAttachmentAsync("aten9180929937@gmail.com", "subject", "Привет", file);
            
            return true;
        } catch (RuntimeException | MessagingException | IOException e) {
            log.error("Не удалось отправить файл пользователю", e);
            return false;
        }
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.EXPORT_FILE;
    }

}
