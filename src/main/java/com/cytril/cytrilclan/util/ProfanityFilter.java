package com.cytril.cytrilclan.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters clan/tag/base names against a configurable word list and regex patterns.
 */
public class ProfanityFilter {

    private boolean enabled;
    private final List<String> bannedWords = new ArrayList<>();
    private final List<Pattern> patterns = new ArrayList<>();

    public ProfanityFilter(ConfigurationSection section) {
        reload(section);
    }

    public void reload(ConfigurationSection section) {
        bannedWords.clear();
        patterns.clear();
        if (section == null) {
            enabled = false;
            return;
        }
        enabled = section.getBoolean("enabled", true);
        for (String word : section.getStringList("words")) {
            bannedWords.add(word.toLowerCase());
        }
        for (String regex : section.getStringList("regex-patterns")) {
            try {
                patterns.add(Pattern.compile(regex));
            } catch (Exception ignored) {
                // skip invalid patterns rather than crashing plugin startup
            }
        }
    }

    public boolean isClean(String input) {
        if (!enabled || input == null) {
            return true;
        }
        String lower = input.toLowerCase();
        for (String word : bannedWords) {
            if (lower.contains(word)) {
                return false;
            }
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(input).find()) {
                return false;
            }
        }
        return true;
    }
}
