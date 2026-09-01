package com.cytril.cytrilclan.model;

import org.bukkit.Material;

/**
 * A single log entry of an item entering (IN) or leaving (OUT) the clan bank.
 */
public class BankTransaction {

    public enum Type {
        IN, OUT
    }

    private final Type type;
    private final String playerName;
    private final Material material;
    private final int amount;
    private final long timestamp;

    public BankTransaction(Type type, String playerName, Material material, int amount, long timestamp) {
        this.type = type;
        this.playerName = playerName;
        this.material = material;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
