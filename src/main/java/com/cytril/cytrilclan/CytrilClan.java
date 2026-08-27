package com.cytril.cytrilclan;

import com.cytril.cytrilclan.command.ClanCommand;
import com.cytril.cytrilclan.listener.GUIListener;
import com.cytril.cytrilclan.manager.ClanManager;
import com.cytril.cytrilclan.manager.GUIManager;
import com.cytril.cytrilclan.placeholder.ClanExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CytrilClan extends JavaPlugin {

    private static CytrilClan instance;
    private ClanManager clanManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.clanManager = new ClanManager(this);
        this.guiManager = new GUIManager(this);

        getCommand("clan").setExecutor(new ClanCommand(this));
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanExpansion(this).register();
        }

        getLogger().info("CytrilClan 1.21.x - 1.26.x Berhasil Dijalankan!");
    }

    @Override
    public void onDisable() {
        if (clanManager != null) {
            clanManager.saveAll();
        }
    }

    public static CytrilClan getInstance() { return instance; }
    public ClanManager getClanManager() { return clanManager; }
    public GUIManager getGUIManager() { return guiManager; }
}
