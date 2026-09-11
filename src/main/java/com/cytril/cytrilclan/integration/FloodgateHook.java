package com.cytril.cytrilclan.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Soft integration with Geyser/Floodgate to detect Bedrock players, e.g. so GUI
 * layouts or chat-input flows can be adapted for players without a full keyboard.
 */
public class FloodgateHook {

    private final boolean available;

    public FloodgateHook() {
        this.available = Bukkit.getPluginManager().getPlugin("floodgate") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isBedrockPlayer(Player player) {
        if (!available) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable t) {
            return false;
        }
    }
}
