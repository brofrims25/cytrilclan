package com.cytril.cytrilclan.manager;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanManager {

    private final CytrilClan plugin;
    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, String> playerClanMap = new HashMap<>();

    public ClanManager(CytrilClan plugin) {
        this.plugin = plugin;
    }

    public boolean createClan(Player player, String clanName) {
        if (!player.hasPermission("cytrilclan.create")) return false;
        
        List<String> banned = plugin.getConfig().getStringList("banned-names");
        for (String word : banned) {
            if (clanName.toLowerCase().contains(word.toLowerCase())) {
                return false;
            }
        }

        if (clans.containsKey(clanName.toLowerCase())) return false;

        Clan clan = new Clan(clanName, player.getUniqueId(), player.getName());
        clans.put(clanName.toLowerCase(), clan);
        playerClanMap.put(player.getUniqueId(), clanName.toLowerCase());
        return true;
    }

    public Clan getClanByPlayer(UUID uuid) {
        String name = playerClanMap.get(uuid);
        return name != null ? clans.get(name) : null;
    }

    public Clan getClanByName(String name) {
        return clans.get(name.toLowerCase());
    }

    public void removePlayerFromClan(UUID uuid) {
        Clan clan = getClanByPlayer(uuid);
        if (clan != null) {
            clan.getMembers().remove(uuid);
            playerClanMap.remove(uuid);
        }
    }

    public void saveAll() {
        // Logika save NBT / FlatFile / MySQL
    }
}
