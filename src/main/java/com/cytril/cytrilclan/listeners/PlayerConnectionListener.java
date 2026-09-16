package com.cytril.cytrilclan.listeners;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Keeps each ClanMember's last-known display name fresh and clears stale pending state on logout. */
public class PlayerConnectionListener implements Listener {

    private final CytrilClan plugin;

    public PlayerConnectionListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan != null) {
            ClanMember member = clan.getMember(player.getUniqueId());
            if (member != null && !player.getName().equals(member.getLastKnownName())) {
                member.setLastKnownName(player.getName());
                plugin.getClanManager().save(clan);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPendingActionManager().clear(event.getPlayer().getUniqueId());
    }
}
