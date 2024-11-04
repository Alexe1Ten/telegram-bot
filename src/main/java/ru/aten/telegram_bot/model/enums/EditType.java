package ru.aten.telegram_bot.model.enums;


public enum EditType {
    FIELD("field"),
    INFO("info"),
    FIlE("file");


    private final String typeValue;

    EditType(String typeValue) {
        this.typeValue = typeValue;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public static EditType getType(String type) {
        if (FIELD.typeValue.equals(type)) return EditType.FIELD;
        if (INFO.typeValue.equals(type)) return EditType.INFO;
        return null;
    }
}
