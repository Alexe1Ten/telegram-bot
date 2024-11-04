package ru.aten.telegram_bot.command.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.command.handler.edit.CancelHandler;
import ru.aten.telegram_bot.command.handler.edit.EditUserContext;
import ru.aten.telegram_bot.command.handler.edit.EditUserHandler;
import ru.aten.telegram_bot.command.handler.edit.FieldValueConverter;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.model.UserInfo;
import ru.aten.telegram_bot.model.enums.EditType;
import ru.aten.telegram_bot.model.enums.TelegramCommands;
import ru.aten.telegram_bot.service.UserService;
import ru.aten.telegram_bot.telegram.TelegramFileService;

@Slf4j
@Component
@AllArgsConstructor
public class ImportFileHandler implements TelegramCommandHandler {

    private final UserService userService;
    private final TelegramFileService telegramFileService;

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        Long requestFrom = message.getFrom().getId();
        EditUserContext context = new EditUserContext(true, null, EditType.FIlE, requestFrom, null, null);
        Map<Long, EditUserContext> contextMap = new HashMap<>();
        contextMap.put(requestFrom, context);
        userService.setEditUserContext(contextMap);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        EditUserHandler.addCancelButton(keyboard);
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Пожалуйста, отправьте Excel файл.")
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) throws IOException, NoSuchFieldException, SecurityException {
        String data = callbackQuery.getData();
        if (data.startsWith("cancel_edit:")) {
            return CancelHandler.cancelOperation(callbackQuery);
        } else {
            return null;
        }
    }

    public BotApiMethod<?> getFileFromUser(Message message) {
        if (message.hasDocument()) {
            String fileId = message.getDocument().getFileId();
            try {
                File file = getFilePath(fileId);
                return proccessExcelFile(message, file);
            } catch (MalformedURLException | TelegramApiException e) {
                log.error("Ошибка получения файла: ", e);
                return EditMessageText.builder()
                        .chatId(message.getChatId().toString())
                        .messageId(message.getMessageId())
                        .text("Ошибка при получении файла. ВОТ ТУТ ОШИБКА")
                        .build();
            }
        } else {
            return processCommand(message);
        }
    }

    private BotApiMethod<?> proccessExcelFile(Message message, File file) {
        String fileName = message.getDocument().getFileName();
        return switch (fileName) {
            case "EditUser.xlsx" ->
                changeUser(message, file);
            // case "EditSchedule.xlsx" ->
            //     changeSchedule(message, file);
            default ->
                errorMessage(message);
        };
    }

    private BotApiMethod<?> changeUser(Message message, File file) {
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return errorMessage(message);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return errorMessage(message);
            }

            Map<Integer, String> headers = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    headers.put(cell.getColumnIndex(), cell.getStringCellValue());
                }
            }

            Map<String, Field> userFields = getAnnotatedFields(User.class);
            Map<String, Field> userInfoFields = getAnnotatedFields(UserInfo.class);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Long telegramId = null;
                for (Cell cell : row) {
                    String headerName = headers.get(cell.getColumnIndex());
                    if (headerName.equals("telegramId")) {
                        telegramId = Long.valueOf(cell.toString());
                        break;
                    }
                }

                Optional<User> userOptional = userService.getUserByTelegramId(telegramId);
                if (userOptional.isEmpty()) return errorMessage(message);
                User user = userOptional.get();
                UserInfo userInfo = user.getUserInfo() != null ? user.getUserInfo() : new UserInfo();

                for (Cell cell : row) {
                    if (cell == null) {
                        continue;
                    }
                    String headerName = headers.get(cell.getColumnIndex());
                    if (headerName == null) {
                        continue;
                    }
                    Field field = userFields.getOrDefault(headerName, userInfoFields.get(headerName));

                    if (field != null) {
                        Object cellValue = FieldValueConverter.convertToFieldType(field, cell.toString());
                        if (cellValue != null) {
                            field.setAccessible(true);
                            try {
                                if (userFields.containsKey(headerName)) {
                                    field.set(user, cellValue);
                                } else if (userInfoFields.containsKey(headerName)) {
                                    field.set(userInfo, cellValue);
                                }
                            } catch (IllegalAccessException e) {
                                System.out.printf("Unable to set value for field: " + field.getName(), e);
                            }
                        }
                    }
                }
                user.setUserInfo(userInfo);
                userService.updateUser(user);
            }
            userService.removeContext(message.getFrom().getId());

        } catch (Exception e) {
            log.error("Ошибка при обработке фала", e);
            System.out.println(e.toString());
            return errorMessage(message);

        }

        return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Данные успешно импортированы.")
                .build();
    }

    // private BotApiMethod<?> changeSchedule(Message message, File file) {
    //     return null;
    // }

    private Map<String, Field> getAnnotatedFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
        return fields;
    }

    private File getFilePath(String fileId) throws MalformedURLException, TelegramApiException {
        return telegramFileService.getFile(fileId);
    }

    private BotApiMethod<?> errorMessage(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Ошибка при обработке файла.")
                .build();
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.IMPORT_FILE;
    }

}
