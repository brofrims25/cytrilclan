package com.cytril.cytrilclan.integration;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes clan placeholders for other plugins (scoreboards, chat, and — most
 * commonly — tab list plugins like TAB).
 *
 * %cytrilclan_name% / %cytrilclan_tag%          - COLORED (the player-chosen
 *                                                  color/bold style is applied).
 *                                                  Use these in Tab/nametag/
 *                                                  chat-prefix configs.
 * %cytrilclan_name_raw% / %cytrilclan_tag_raw%   - plain, no color codes at
 *                                                  all. Use these if another
 *                                                  plugin sorts/compares the
 *                                                  value and color codes would
 *                                                  break that.
 * %cytrilclan_name_colored% / %cytrilclan_tag_colored% - kept as aliases of
 *                                                  the colored versions for
 *                                                  backward compatibility.
 * %cytrilclan_role% / %cytrilclan_members%       - member role / clan size.
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
                case "name", "name_colored", "name_raw", "tag", "tag_colored", "tag_raw" -> "None";
                case "role" -> "";
                case "members" -> "0";
                default -> "";
            };
        }
        ClanMember member = clan.getMember(offlinePlayer.getUniqueId());
        return switch (params) {
            case "name", "name_colored" -> com.cytril.cytrilclan.util.MessageUtil.color(clan.getFormattedName());
            case "tag", "tag_colored" -> com.cytril.cytrilclan.util.MessageUtil.color(clan.getFormattedTag());
            case "name_raw" -> clan.getName();
            case "tag_raw" -> clan.getTag() == null ? "" : clan.getTag();
            case "role" -> member == null ? "" : member.getRole().name();
            case "members" -> String.valueOf(clan.getSize());
            default -> "";
        };
    }
}
