package com.cytril.cytrilclan.listeners;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enforces the per-player /clan pvp on|off toggle. This only ever affects
 * combat between two members of the SAME clan - members of other clans and
 * players with no clan at all can always hit (and be hit by) anyone,
 * regardless of anyone's personal pvp setting.
 */
public class ClanPvpListener implements Listener {

    private final CytrilClan plugin;
    private final Map<UUID, Long> lastNotice = new HashMap<>();

    public ClanPvpListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isPvpToggleEnabled()) {
            return; // feature disabled server-wide - never interfere with combat
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event);
        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        Clan victimClan = plugin.getClanManager().getClanByPlayer(victim.getUniqueId());
        Clan attackerClan = plugin.getClanManager().getClanByPlayer(attacker.getUniqueId());
        if (victimClan == null || attackerClan == null) {
            return; // at least one side has no clan - never affected by this toggle
        }
        if (!victimClan.getName().equalsIgnoreCase(attackerClan.getName())) {
            return; // different clans - never affected by this toggle
        }

        ClanMember victimMember = victimClan.getMember(victim.getUniqueId());
        ClanMember attackerMember = attackerClan.getMember(attacker.getUniqueId());
        boolean victimAllows = victimMember == null || victimMember.isPvpEnabled();
        boolean attackerAllows = attackerMember == null || attackerMember.isPvpEnabled();

        if (!victimAllows || !attackerAllows) {
            event.setCancelled(true);
            notify(attacker, victim);
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private void notify(Player attacker, Player victim) {
        long cooldownMs = plugin.getConfigManager().getPvpNotifyCooldownSeconds() * 1000L;
        long now = System.currentTimeMillis();
        Long last = lastNotice.get(attacker.getUniqueId());
        if (last != null && now - last < cooldownMs) {
            return;
        }
        lastNotice.put(attacker.getUniqueId(), now);
        MessageUtil.sendError(attacker, "You can't hit " + victim.getName() + " - clan PVP is off for one of you (/clan pvp).");
    }
}
