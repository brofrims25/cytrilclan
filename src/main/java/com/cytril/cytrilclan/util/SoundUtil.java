package com.cytril.cytrilclan.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Plays config-driven sounds so server owners can customise or disable audio feedback.
 */
public class SoundUtil {

    private final ConfigurationSection soundsSection;

    public SoundUtil(ConfigurationSection soundsSection) {
        this.soundsSection = soundsSection;
    }

    public void play(Player player, String key) {
        if (soundsSection == null) {
            return;
        }
        String raw = soundsSection.getString(key);
        if (raw == null || raw.isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(raw.toUpperCase());
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // invalid sound name in config - fail silently to avoid spamming console
        }
    }
}
