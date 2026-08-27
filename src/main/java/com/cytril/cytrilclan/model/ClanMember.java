package com.cytril.cytrilclan.model;

import java.util.UUID;

public class ClanMember {
    private final UUID uuid;
    private final String name;
    private Rank rank;
    private int kills;
    private int deaths;
    private int joinCount;

    public enum Rank {
        MEMBER, WAKIL, LEADER
    }

    public ClanMember(UUID uuid, String name, Rank rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
        this.kills = 0;
        this.deaths = 0;
        this.joinCount = 1;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public Rank getRank() { return rank; }
    public void setRank(Rank rank) { this.rank = rank; }
    public int getKills() { return kills; }
    public void addKill() { this.kills++; }
    public int getDeaths() { return deaths; }
    public void addDeath() { this.deaths++; }
    public int getJoinCount() { return joinCount; }
    public void incrementJoinCount() { this.joinCount++; }
}
