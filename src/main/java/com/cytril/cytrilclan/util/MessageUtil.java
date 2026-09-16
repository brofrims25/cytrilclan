package com.cytril.cytrilclan.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralised message colorization/formatting helper. Supports classic '&' codes
 * plus '&#RRGGBB' hex codes for Paper's extended color support.
 */
public final class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static String prefix = "&8[&bCytrilClan&8] &r";

    private MessageUtil() {
    }

    public static void setPrefix(String rawPrefix) {
        prefix = rawPrefix;
    }

    public static String color(String input) {
        if (input == null) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String withPrefix(String message) {
        return color(prefix + message);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(withPrefix(message));
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(withPrefix("&c" + message));
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(withPrefix("&a" + message));
    }
}
