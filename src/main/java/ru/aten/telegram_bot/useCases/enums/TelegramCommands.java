package ru.aten.telegram_bot.useCases.enums;

public enum TelegramCommands {

    START_COMMAND("/start"),
    CLEAR_COMMAND("/clear"),
    EDIT_USER_COMMAND("/editUser"),
    IMPORT_FILE("/importFile"),
    EXPORT_FILE("/exportFile");

    private final String commandValue;

    
    TelegramCommands(String commandValue) {
        this.commandValue = commandValue;
    }
    
    public String getCommandValue() {
        return commandValue;
    }
    
}
