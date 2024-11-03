package ru.aten.telegram_bot.command;

public enum TelegramCommands {

    START_COMMAND("/start"),
    CLEAR_COMMAND("/clear"),
    EDIT_USER_COMMAND("/editUser"),
    IMPORT_FILE("/importFile");

    private final String commandValue;

    
    TelegramCommands(String commandValue) {
        this.commandValue = commandValue;
    }
    
    public String getCommandValue() {
        return commandValue;
    }
    
}
