package ru.aten.telegram_bot.useCases.telegram;


public class TextConverter {

    public static String escapeMarkdownV2(String text) {
        return text
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("#", "")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace("!", "\\!")
                .replace("'", "\\'")
                .replace("[", "\\[")
                .replace("]", "\\]");
    }
}
