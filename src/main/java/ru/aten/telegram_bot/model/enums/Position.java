package ru.aten.telegram_bot.model.enums;

public enum Position {
    DEPARTMENT_MANAGER("Управляющий отделом"),
    DEPUTY_DEPARTMENT_MANAGER("Заместитель управляющего отделом"),
    SALE_CONSULTANT("Продавец - Консультант"),
    SALE_CONSULTANT_COLLECTOR("Продавец - Консультант_Сборщик"),
    SALE_DESIGNER("Продавец - Дизайнер"),
    MERCHANDISER("Мерчендайзер");

    private final String value;

    Position(String value) {
        this.value = value;
    }

    public static Position fromValue(String value) {
        switch (value.toLowerCase()) {
            case "уо" -> {
                return Position.DEPARTMENT_MANAGER;
            }
            case "зуо" -> {
                return Position.DEPUTY_DEPARTMENT_MANAGER;
            }
            case "пк" -> {
                return Position.SALE_CONSULTANT;
            }
            case "пд" -> {
                return Position.SALE_DESIGNER;
            }
            case "сборка" -> {
                return Position.SALE_CONSULTANT_COLLECTOR;
            }
            case "мерч" -> {
                return Position.MERCHANDISER;
            }
            default ->
                throw new IllegalArgumentException("Неизвестное значение для позиции: " + value);
        }
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public String getValue() {
        return value;
    }
}
