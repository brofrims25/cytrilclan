package com.cytril.cytrilclan.placeholder;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ClanExpansion extends PlaceholderExpansion {

    private final CytrilClan plugin;

    public ClanExpansion(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "cytrilclan"; }

    @Override
    public @NotNull String getAuthor() { return "Cytril"; }

    @Override
    public @NotNull String getVersion() { return "1.0.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (params.equalsIgnoreCase("name")) {
            return clan != null ? clan.getName() : plugin.getConfig().getString("placeholders.no-clan", "-");
        }
        return null;
    }
}
