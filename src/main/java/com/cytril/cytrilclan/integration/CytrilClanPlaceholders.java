package com.cytril.cytrilclan.integration;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes %cytrilclan_name%, %cytrilclan_tag%, %cytrilclan_role% and
 * %cytrilclan_members% placeholders for other plugins (scoreboards, chat, tab lists).
 */
public class CytrilClanPlaceholders extends PlaceholderExpansion {

    private final CytrilClan plugin;

    public CytrilClanPlaceholders(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cytrilclan";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CytrilClan Team";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }
        Clan clan = plugin.getClanManager().getClanByPlayer(offlinePlayer.getUniqueId());
        if (clan == null) {
            return switch (params) {
                case "name" -> "None";
                case "tag" -> "";
                case "role" -> "";
                case "members" -> "0";
                default -> "";
            };
        }
        ClanMember member = clan.getMember(offlinePlayer.getUniqueId());
        return switch (params) {
            case "name" -> clan.getName();
            case "tag" -> clan.getTag() == null ? "" : clan.getTag();
            case "role" -> member == null ? "" : member.getRole().name();
            case "members" -> String.valueOf(clan.getSize());
            default -> "";
        };
    }
}
