package com.cytril.cytrilclan.model;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core clan data object. Held in-memory by ClanManager and persisted via StorageManager.
 */
public class Clan {

    private String name;
    private String tag;
    private String bannerData; // base64 serialized ItemStack (banner) or null
    private UUID leader;
    private final Map<UUID, ClanMember> members = new LinkedHashMap<>();
    private final List<ClanBase> bases = new ArrayList<>();
    private ItemStack[] bankContents;
    private final List<BankTransaction> transactions = new ArrayList<>();
    private final long createdAt;
    private final Map<UUID, Long> pendingInvites = new LinkedHashMap<>();

    public Clan(String name, String tag, UUID leader, long createdAt) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.createdAt = createdAt;
        this.bankContents = new ItemStack[54];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBannerData() {
        return bannerData;
    }

    public void setBannerData(String bannerData) {
        this.bannerData = bannerData;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Map<UUID, ClanMember> getMembers() {
        return members;
    }

    public ClanMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public int getSize() {
        return members.size();
    }

    public List<ClanBase> getBases() {
        return bases;
    }

    public ItemStack[] getBankContents() {
        return bankContents;
    }

    public void setBankContents(ItemStack[] bankContents) {
        this.bankContents = bankContents;
    }

    public List<BankTransaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(BankTransaction transaction) {
        transactions.add(0, transaction); // newest first
        // keep log bounded to avoid unbounded file growth
        while (transactions.size() > 500) {
            transactions.remove(transactions.size() - 1);
        }
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<UUID, Long> getPendingInvites() {
        return pendingInvites;
    }

    public String getDisplayTag() {
        return "&8[" + (tag == null ? "" : tag) + "&8]";
    }
}
