package com.cytril.cytrilclan.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Soft integration with Vault's Economy service. Only actually does anything
 * when config.yml has features.economy: true AND both Vault and a real
 * economy plugin (EssentialsX, CMI, etc.) are installed - otherwise every
 * method is a safe no-op so the rest of the plugin never has to null-check.
 */
public class VaultHook {

    private Economy economy;
    private final boolean available;

    public VaultHook() {
        boolean found = false;
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                found = true;
            }
        }
        this.available = found;
    }

    public boolean isAvailable() {
        return available;
    }

    public double getBalance(OfflinePlayer player) {
        if (!available) return 0.0;
        return economy.getBalance(player);
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (!available) return true; // no economy configured - never block on cost
        return economy.has(player, amount);
    }

    /** Withdraws the amount, returning true on success. No-op success if Vault isn't hooked. */
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!available || amount <= 0) return true;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /** Deposits the amount, returning true on success. No-op success if Vault isn't hooked. */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (!available || amount <= 0) return true;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (!available) return String.valueOf(amount);
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }
}
