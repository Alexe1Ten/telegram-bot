package ru.aten.telegram_bot.telegram.message;


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
