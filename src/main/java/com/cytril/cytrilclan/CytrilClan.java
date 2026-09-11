package com.cytril.cytrilclan;

import com.cytril.cytrilclan.commands.ClanCommand;
import com.cytril.cytrilclan.integration.CytrilClanPlaceholders;
import com.cytril.cytrilclan.integration.FloodgateHook;
import com.cytril.cytrilclan.integration.LuckPermsHook;
import com.cytril.cytrilclan.listeners.BookEditListener;
import com.cytril.cytrilclan.listeners.ChatInputListener;
import com.cytril.cytrilclan.listeners.ClanGuiListener;
import com.cytril.cytrilclan.listeners.PlayerConnectionListener;
import com.cytril.cytrilclan.manager.ClanManager;
import com.cytril.cytrilclan.manager.ConfigManager;
import com.cytril.cytrilclan.manager.PendingActionManager;
import com.cytril.cytrilclan.manager.StorageManager;
import com.cytril.cytrilclan.util.MessageUtil;
import com.cytril.cytrilclan.util.ProfanityFilter;
import com.cytril.cytrilclan.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CytrilClan main plugin bootstrap. Wires together managers, integrations,
 * listeners and the /clan command, and owns the load/save lifecycle.
 */
public class CytrilClan extends JavaPlugin {

    private ConfigManager configManager;
    private StorageManager storageManager;
    private ClanManager clanManager;
    private PendingActionManager pendingActionManager;
    private ProfanityFilter profanityFilter;
    private SoundUtil soundUtil;

    private LuckPermsHook luckPermsHook;
    private FloodgateHook floodgateHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        MessageUtil.setPrefix(configManager.getPrefix());

        this.storageManager = new StorageManager(this);
        this.clanManager = new ClanManager(storageManager);
        this.clanManager.loadAll();

        this.pendingActionManager = new PendingActionManager();
        this.profanityFilter = new ProfanityFilter(getConfig().getConfigurationSection("profanity"));
        this.soundUtil = new SoundUtil(getConfig().getConfigurationSection("sounds"));

        this.luckPermsHook = new LuckPermsHook();
        this.floodgateHook = new FloodgateHook();

        getServer().getPluginManager().registerEvents(new ClanGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new BookEditListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        var clanCommand = new ClanCommand(this);
        var command = getCommand("clan");
        if (command != null) {
            command.setExecutor(clanCommand);
            command.setTabCompleter(clanCommand);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CytrilClanPlaceholders(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }
        if (luckPermsHook.isAvailable()) {
            getLogger().info("Hooked into LuckPerms.");
        }
        if (floodgateHook.isAvailable()) {
            getLogger().info("Hooked into Floodgate.");
        }

        getLogger().info("CytrilClan enabled - " + clanManager.getAllClans().size() + " clan(s) loaded.");
    }

    @Override
    public void onDisable() {
        if (clanManager != null) {
            clanManager.saveAll();
        }
        getLogger().info("CytrilClan disabled, all clan data saved.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public PendingActionManager getPendingActionManager() {
        return pendingActionManager;
    }

    public ProfanityFilter getProfanityFilter() {
        return profanityFilter;
    }

    public SoundUtil getSoundUtil() {
        return soundUtil;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public FloodgateHook getFloodgateHook() {
        return floodgateHook;
    }
}
