package com.cytril.cytrilclan.manager;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Holds all clans in memory for fast lookup and coordinates persistence through
 * StorageManager. This is the single source of truth at runtime; GUIs and
 * commands should always go through this class rather than touching storage directly.
 */
public class ClanManager {

    private final StorageManager storageManager;
    private final ConfigManager configManager;
    private final Map<String, Clan> clansByName = new LinkedHashMap<>(); // key = lowercase name
    private final Map<UUID, String> playerClanIndex = new HashMap<>();
    private final Map<String, BukkitTask> pendingLeaderTransfers = new HashMap<>();

    public ClanManager(StorageManager storageManager, ConfigManager configManager) {
        this.storageManager = storageManager;
        this.configManager = configManager;
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

    /** Used on plugin disable - stays fully synchronous on purpose. */
    public void saveAll() {
        for (Clan clan : clansByName.values()) {
            storageManager.save(clan);
        }
    }

    public Clan createClan(String name, String tag, Player leader) {
        Clan clan = new Clan(name, tag, leader.getUniqueId(), System.currentTimeMillis());
        // BUGFIX: general.bank-rows was documented in config.yml but nothing ever
        // read it - every clan's bank was hardcoded to 54 slots regardless of what
        // an admin set. New clans now actually get a bank sized from that setting.
        int rows = configManager != null ? configManager.getBankRows() : 6;
        rows = Math.max(1, Math.min(6, rows));
        clan.setBankContents(new ItemStack[rows * 9]);
        ClanMember leaderMember = new ClanMember(leader.getUniqueId(), leader.getName(), ClanRole.LEADER, System.currentTimeMillis());
        clan.getMembers().put(leader.getUniqueId(), leaderMember);
        clansByName.put(name.toLowerCase(), clan);
        playerClanIndex.put(leader.getUniqueId(), name.toLowerCase());
        storageManager.saveAsync(clan);
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
        storageManager.saveAsync(clan);
    }

    public void removeMember(Clan clan, UUID uuid) {
        clan.getMembers().remove(uuid);
        playerClanIndex.remove(uuid);
        storageManager.saveAsync(clan);
    }

    /** Routine save used by every in-game action - non-blocking (see StorageManager). */
    public void save(Clan clan) {
        storageManager.saveAsync(clan);
    }

    /**
     * Renames a clan, re-keying the in-memory index, saving under the new
     * filename, and deleting the old YAML file so no orphaned duplicate is
     * left on disk.
     */
    public void renameClan(Clan clan, String newName) {
        String oldNameKey = clan.getName().toLowerCase();
        String newNameKey = newName.toLowerCase();
        clan.setName(newName);
        clansByName.remove(oldNameKey);
        clansByName.put(newNameKey, clan);
        for (UUID uuid : clan.getMembers().keySet()) {
            playerClanIndex.put(uuid, newNameKey);
        }
        // BUGFIX: pendingLeaderTransfers was keyed by name and never re-keyed here.
        // Renaming a clan while a /clan transfer was pending left the entry
        // orphaned under the old name - hasPendingLeaderTransfer() on the new name
        // would report "nothing pending" while the scheduled task (which also used
        // to look the clan up by its old name - see LeaderTransferTask) silently
        // failed to apply when it fired.
        if (!oldNameKey.equals(newNameKey)) {
            BukkitTask pendingTransfer = pendingLeaderTransfers.remove(oldNameKey);
            if (pendingTransfer != null) {
                pendingLeaderTransfers.put(newNameKey, pendingTransfer);
            }
        }
        storageManager.saveAsync(clan);
        if (!oldNameKey.equals(newNameKey)) {
            storageManager.deleteByName(oldNameKey);
        }
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

    /** All clans that currently have a non-expired invite pending for the given player. */
    public List<Clan> getPendingInvitesFor(UUID playerUuid, long expiryMs) {
        List<Clan> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Clan clan : clansByName.values()) {
            Long invitedAt = clan.getPendingInvites().get(playerUuid);
            if (invitedAt != null && now - invitedAt <= expiryMs) {
                result.add(clan);
            }
        }
        return result;
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
