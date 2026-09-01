package com.cytril.cytrilclan.model;

import java.util.UUID;

/**
 * Represents a player's membership record inside a clan.
 */
public class ClanMember {

    private final UUID uuid;
    private String lastKnownName;
    private ClanRole role;
    private final long joinedAt;
    private int kills;
    private int deaths;

    public ClanMember(UUID uuid, String lastKnownName, ClanRole role, long joinedAt) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public ClanRole getRole() {
        return role;
    }

    public void setRole(ClanRole role) {
        this.role = role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addDeath() {
        this.deaths++;
    }
}
