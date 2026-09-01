package com.cytril.cytrilclan.manager;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds all clans in memory for fast lookup and coordinates persistence through
 * StorageManager. This is the single source of truth at runtime; GUIs and
 * commands should always go through this class rather than touching storage directly.
 */
public class ClanManager {

    private final StorageManager storageManager;
    private final Map<String, Clan> clansByName = new LinkedHashMap<>(); // key = lowercase name
    private final Map<UUID, String> playerClanIndex = new HashMap<>();
    private final Map<String, BukkitTask> pendingLeaderTransfers = new HashMap<>();

    public ClanManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public void loadAll() {
        clansByName.clear();
        playerClanIndex.clear();
        for (Clan clan : storageManager.loadAll()) {
            clansByName.put(clan.getName().toLowerCase(), clan);
            for (UUID uuid : clan.getMembers().keySet()) {
                playerClanIndex.put(uuid, clan.getName().toLowerCase());
            }
        }
    }

    public void saveAll() {
        for (Clan clan : clansByName.values()) {
            storageManager.save(clan);
        }
    }

    public Clan createClan(String name, String tag, Player leader) {
        Clan clan = new Clan(name, tag, leader.getUniqueId(), System.currentTimeMillis());
        ClanMember leaderMember = new ClanMember(leader.getUniqueId(), leader.getName(), ClanRole.LEADER, System.currentTimeMillis());
        clan.getMembers().put(leader.getUniqueId(), leaderMember);
        clansByName.put(name.toLowerCase(), clan);
        playerClanIndex.put(leader.getUniqueId(), name.toLowerCase());
        storageManager.save(clan);
        return clan;
    }

    public void disbandClan(Clan clan) {
        for (UUID uuid : clan.getMembers().keySet()) {
            playerClanIndex.remove(uuid);
        }
        clansByName.remove(clan.getName().toLowerCase());
        cancelPendingLeaderTransfer(clan);
        storageManager.delete(clan);
    }

    public void addMember(Clan clan, Player player, ClanRole role) {
        ClanMember member = new ClanMember(player.getUniqueId(), player.getName(), role, System.currentTimeMillis());
        clan.getMembers().put(player.getUniqueId(), member);
        playerClanIndex.put(player.getUniqueId(), clan.getName().toLowerCase());
        storageManager.save(clan);
    }

    public void removeMember(Clan clan, UUID uuid) {
        clan.getMembers().remove(uuid);
        playerClanIndex.remove(uuid);
        storageManager.save(clan);
    }

    public void save(Clan clan) {
        storageManager.save(clan);
    }

    public Clan getClanByName(String name) {
        if (name == null) return null;
        return clansByName.get(name.toLowerCase());
    }

    public Clan getClanByPlayer(UUID uuid) {
        String name = playerClanIndex.get(uuid);
        return name == null ? null : clansByName.get(name);
    }

    public boolean isInClan(UUID uuid) {
        return playerClanIndex.containsKey(uuid);
    }

    public boolean nameTaken(String name) {
        return clansByName.containsKey(name.toLowerCase());
    }

    public Collection<Clan> getAllClans() {
        return clansByName.values();
    }

    public void reindexPlayer(UUID uuid, String clanName) {
        playerClanIndex.put(uuid, clanName.toLowerCase());
    }

    // --- leader transfer scheduling -----------------------------------------

    public void schedulePendingLeaderTransfer(Clan clan, BukkitTask task) {
        cancelPendingLeaderTransfer(clan);
        pendingLeaderTransfers.put(clan.getName().toLowerCase(), task);
    }

    public boolean cancelPendingLeaderTransfer(Clan clan) {
        BukkitTask task = pendingLeaderTransfers.remove(clan.getName().toLowerCase());
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }

    public boolean hasPendingLeaderTransfer(Clan clan) {
        return pendingLeaderTransfers.containsKey(clan.getName().toLowerCase());
    }
}
