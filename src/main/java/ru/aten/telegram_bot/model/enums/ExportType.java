package ru.aten.telegram_bot.model.enums;

public enum ExportType {
    USERS("Пользователи"),
    SCHEDULE("График");

    private final String typeValue;

    ExportType(String typeValue) {
        this.typeValue = typeValue;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public static ExportType getType(String type) {
        if (USERS.typeValue.equals(type)) return ExportType.USERS;
        if (SCHEDULE.typeValue.equals(type)) return ExportType.SCHEDULE;
        return null;
    }
}
