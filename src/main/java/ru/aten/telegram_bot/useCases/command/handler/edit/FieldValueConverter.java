package ru.aten.telegram_bot.useCases.command.handler.edit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ru.aten.telegram_bot.entities.Position;
import ru.aten.telegram_bot.entities.Role;

public class FieldValueConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static Object convertToFieldType(Field field, String newValue) throws IOException {
        if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            return Integer.valueOf(newValue);
        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
            return Long.valueOf(newValue);
        } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
            return Double.valueOf(newValue);
        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            return Boolean.valueOf(newValue);
        } else if (field.getType().equals(Role.class)) {
            return Role.fromValue(newValue);
        } else if (field.getType().equals(Position.class)) {
            return Position.fromValue(newValue);
        } else if(field.getType().equals(LocalDate.class)) {
            return convertToLocalDate(newValue);
        } else {
            return newValue; // Если тип - строка или другой объект
        }
    }

    private static LocalDate convertToLocalDate(String newValue) throws IOException {
        try {
            return LocalDate.parse(newValue, DATE_FORMATTER);
        } catch (Exception e) {
            throw new IOException("Неверный формат даты", e);
        }
    }
}
