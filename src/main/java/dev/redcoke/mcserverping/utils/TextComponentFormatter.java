package dev.redcoke.mcserverping.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for formatting Minecraft text components.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextComponentFormatter {
    public static final String FORMATTING_CHAR = "§"; // U+00A7 for Minecraft, "&" for Classic
    public static final String RESET = FORMATTING_CHAR + "r";
    public static final String BOLD = FORMATTING_CHAR + "l";
    public static final String OBFUSCATED = FORMATTING_CHAR + "k";
    public static final String STRIKETHROUGH = FORMATTING_CHAR + "m";
    public static final String UNDERLINE = FORMATTING_CHAR + "n";
    public static final String ITALIC = FORMATTING_CHAR + "o";
    protected static final Map<String, String> COLOR_CODES;

    static {
        Map<String, String> colorCodes = new HashMap<>();
        colorCodes.put("black", "0");
        colorCodes.put("dark_blue", "1");
        colorCodes.put("dark_green", "2");
        colorCodes.put("dark_aqua", "3");
        colorCodes.put("dark_red", "4");
        colorCodes.put("dark_purple", "5");
        colorCodes.put("gold", "6");
        colorCodes.put("gray", "7");
        colorCodes.put("dark_gray", "8");
        colorCodes.put("blue", "9");
        colorCodes.put("green", "a");
        colorCodes.put("aqua", "b");
        colorCodes.put("red", "c");
        colorCodes.put("light_purple", "d");
        colorCodes.put("yellow", "e");
        colorCodes.put("white", "f");
        COLOR_CODES = colorCodes;
    }

    public static String toLegacyText(JsonArray text) {
        StringBuilder legacyText = new StringBuilder();
        for (var component : text) {
            JsonObject componentObj = component.getAsJsonObject();
            if (componentObj.has("bold") && componentObj.get("bold").getAsBoolean()) {
                legacyText.append(BOLD);
            }
            if (componentObj.has("obfuscated") && componentObj.get("obfuscated").getAsBoolean()) {
                legacyText.append(OBFUSCATED);
            }
            if (componentObj.has("strikethrough") && componentObj.get("strikethrough").getAsBoolean()) {
                legacyText.append(STRIKETHROUGH);
            }
            if (componentObj.has("underline") && componentObj.get("underline").getAsBoolean()) {
                legacyText.append(UNDERLINE);
            }
            if (componentObj.has("italic") && componentObj.get("italic").getAsBoolean()) {
                legacyText.append(ITALIC);
            }
            if (componentObj.has("color")) {
                String color = componentObj.get("color").getAsString();
                legacyText.append(FORMATTING_CHAR).append(COLOR_CODES.get(color));
            }
            legacyText.append(componentObj.get("text").getAsString());
        }
        return legacyText.toString();
    }

}