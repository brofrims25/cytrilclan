package com.cytril.cytrilclan.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Thin accessor over config.yml so the rest of the plugin doesn't repeat
 * raw path strings everywhere.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration raw() {
        return config;
    }

    public int getMaxBases() {
        return config.getInt("general.max-bases", 3);
    }

    public int getBankRows() {
        return config.getInt("general.bank-rows", 6);
    }

    public int getBaseWarmupSeconds() {
        return config.getInt("general.base-warmup-seconds", 5);
    }

    public int getLeaderTransferDelayMinutes() {
        return config.getInt("general.leader-transfer-delay-minutes", 60);
    }

    public int getMembersPerPage() {
        return config.getInt("general.members-per-page", 45);
    }

    public int getBankItemsPerPage() {
        return config.getInt("general.bank-items-per-page", 45);
    }

    public int getHistoryEntriesPerPage() {
        return config.getInt("general.history-entries-per-page", 45);
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "&8[&bCytrilClan&8] &r");
    }

    public String getGuiTitle(String key) {
        return config.getString("gui.titles." + key, "&8Clan Menu");
    }
}
