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

    public int getInviteExpiryMinutes() {
        return config.getInt("general.invite-expiry-minutes", 60);
    }

    // --- limits ---

    public int getMaxClans() {
        return config.getInt("limits.max-clans", 0);
    }

    public int getMaxMembersPerClan() {
        return config.getInt("limits.max-members-per-clan", 0);
    }

    // --- feature toggles ---

    public boolean isBankEnabled() {
        return config.getBoolean("features.bank", true);
    }

    public boolean isBasesEnabled() {
        return config.getBoolean("features.bases", true);
    }

    public boolean isGiveItemEnabled() {
        return config.getBoolean("features.give-item", true);
    }

    public boolean isNameStylePickerEnabled() {
        return config.getBoolean("features.name-style-picker", true);
    }

    public boolean isPvpToggleEnabled() {
        return config.getBoolean("features.pvp-toggle", true);
    }

    public boolean isChatTagEnabled() {
        return config.getBoolean("features.chat-tag", true);
    }

    public boolean isEconomyEnabled() {
        return config.getBoolean("features.economy", false);
    }

    // --- economy ---

    public double getClanCreationCost() {
        return config.getDouble("economy.clan-creation-cost", 0.0);
    }

    public boolean isRefundOnDisband() {
        return config.getBoolean("economy.refund-on-disband", false);
    }

    public int getRefundPercent() {
        return config.getInt("economy.refund-percent", 50);
    }

    // --- chat ---

    public String getChatFormatWithClan() {
        return config.getString("chat.format-with-clan", "&8[{tag}&8]&r {player}&7: &f{message}");
    }

    public String getChatFormatWithoutClan() {
        return config.getString("chat.format-without-clan", "{player}&7: &f{message}");
    }

    // --- pvp ---

    public int getPvpNotifyCooldownSeconds() {
        return config.getInt("pvp.notify-cooldown-seconds", 2);
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "&8[&bCytrilClan&8] &r");
    }

    public String getGuiTitle(String key) {
        return config.getString("gui.titles." + key, "&8Clan Menu");
    }
}
