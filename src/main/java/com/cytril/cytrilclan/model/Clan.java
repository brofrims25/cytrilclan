package com.cytril.cytrilclan.model;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Clan {
    private final String name;
    private final UUID leader;
    private final Map<UUID, ClanMember> members = new HashMap<>();
    private final Map<String, Location> bases = new HashMap<>();
    private final List<ItemStack> bankItems = new ArrayList<>();
    private final List<String> bankLogs = new ArrayList<>();
    
    private UUID pendingLeaderUUID = null;
    private long leaderTransferTime = 0;

    public Clan(String name, UUID leaderUUID, String leaderName) {
        this.name = name;
        this.leader = leaderUUID;
        this.members.put(leaderUUID, new ClanMember(leaderUUID, leaderName, ClanMember.Rank.LEADER));
    }

    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public Map<UUID, ClanMember> getMembers() { return members; }
    public Map<String, Location> getBases() { return bases; }
    public List<ItemStack> getBankItems() { return bankItems; }
    public List<String> getBankLogs() { return bankLogs; }

    public UUID getPendingLeaderUUID() { return pendingLeaderUUID; }
    public void setPendingLeaderUUID(UUID pendingLeaderUUID) { this.pendingLeaderUUID = pendingLeaderUUID; }
    public long getLeaderTransferTime() { return leaderTransferTime; }
    public void setLeaderTransferTime(long leaderTransferTime) { this.leaderTransferTime = leaderTransferTime; }
}
