package ru.aten.telegram_bot.command.handler.edit;

import java.lang.reflect.Field;

import ru.aten.telegram_bot.model.enums.Position;
import ru.aten.telegram_bot.model.enums.Role;

public class FieldValueConverter {

    public static Object convertToFieldType(Field field, String newValue) {
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
        } else {
            return newValue; // Если тип - строка или другой объект
        }
    }
}
