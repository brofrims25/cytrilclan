package com.cytril.cytrilclan.model;

/**
 * Hierarchical role of a member inside a clan.
 * Ordinal order matters: higher ordinal = higher authority.
 */
public enum ClanRole {
    MEMBER(0, "&7Member"),
    OFFICER(1, "&aOfficer"),
    LEADER(2, "&6Leader");

    private final int weight;
    private final String display;

    ClanRole(int weight, String display) {
        this.weight = weight;
        this.display = display;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplay() {
        return display;
    }

    public boolean outranks(ClanRole other) {
        return this.weight > other.weight;
    }

    public boolean atLeast(ClanRole other) {
        return this.weight >= other.weight;
    }
}
