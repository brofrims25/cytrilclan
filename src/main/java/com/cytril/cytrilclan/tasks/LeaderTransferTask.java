package com.cytril.cytrilclan.tasks;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Delayed leadership transfer. Scheduled 1 hour (config-driven) after the
 * leader requests the transfer, and can be cancelled any time before it runs
 * via ClanManager#cancelPendingLeaderTransfer.
 */
public class LeaderTransferTask extends BukkitRunnable {

    private final CytrilClan plugin;
    private final Clan clan;
    private final UUID newLeaderUuid;

    public LeaderTransferTask(CytrilClan plugin, Clan clan, UUID newLeaderUuid) {
        this.plugin = plugin;
        // BUGFIX: this used to store clan.getName() and look the clan back up by
        // that name when the task fired. If the leader renamed the clan while the
        // transfer was pending, the lookup by the now-stale old name returned null
        // and the transfer silently never happened - no message to anyone, and
        // ClanManager kept reporting a "pending transfer" that would never run.
        // Holding the Clan object itself sidesteps that: its identity doesn't
        // change on rename, only its name field does.
        this.clan = clan;
        this.newLeaderUuid = newLeaderUuid;
    }

    @Override
    public void run() {
        if (plugin.getClanManager().getClanByName(clan.getName()) != clan) {
            return; // clan was disbanded (or replaced) since this was scheduled
        }
        ClanMember newLeaderMember = clan.getMember(newLeaderUuid);
        if (newLeaderMember == null) {
            return; // target left the clan before the transfer completed
        }

        ClanMember oldLeaderMember = clan.getMember(clan.getLeader());
        if (oldLeaderMember != null) {
            oldLeaderMember.setRole(ClanRole.OFFICER);
        }
        newLeaderMember.setRole(ClanRole.LEADER);
        clan.setLeader(newLeaderUuid);
        plugin.getClanManager().save(clan);
        plugin.getClanManager().cancelPendingLeaderTransfer(clan);

        for (UUID uuid : clan.getMembers().keySet()) {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                MessageUtil.send(online, "&6" + newLeaderMember.getLastKnownName() + " &fis now the leader of &6" + clan.getName() + "&f.");
            }
        }
    }
}
