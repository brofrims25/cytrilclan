package com.cytril.cytrilclan.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Wraps the native LuckPerms API. Bukkit's Player#hasPermission can be inaccurate
 * with some permission plugin setups (context-dependent nodes, negated defaults),
 * so clan-creation checks specifically go through LuckPerms when it is present.
 */
public class LuckPermsHook {

    private LuckPerms luckPerms;
    private final boolean available;

    public LuckPermsHook() {
        boolean found = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
        if (found) {
            try {
                this.luckPerms = LuckPermsProvider.get();
            } catch (IllegalStateException ex) {
                found = false;
            }
        }
        this.available = found;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Checks a permission node via the native LuckPerms API when available,
     * falling back to Bukkit's permission check otherwise.
     */
    public boolean hasPermission(Player player, String node) {
        if (!available || luckPerms == null) {
            return player.hasPermission(node);
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return player.hasPermission(node);
        }
        CachedPermissionData data = user.getCachedData().getPermissionData();
        net.luckperms.api.util.Tristate result = data.checkPermission(node);
        if (result == net.luckperms.api.util.Tristate.UNDEFINED) {
            return player.hasPermission(node);
        }
        return result.asBoolean();
    }
}
