package ru.aten.telegram_bot.entities;

public enum Role {
    ADMIN("ADMIN"),
    USER("USER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Неизвестное значение роли: " + value);
    }

    public String getValue() {
        return value;
    }
}
