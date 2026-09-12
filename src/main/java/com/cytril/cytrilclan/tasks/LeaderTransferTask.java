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
    private final String clanName;
    private final UUID newLeaderUuid;

    public LeaderTransferTask(CytrilClan plugin, Clan clan, UUID newLeaderUuid) {
        this.plugin = plugin;
        this.clanName = clan.getName();
        this.newLeaderUuid = newLeaderUuid;
    }

    @Override
    public void run() {
        Clan clan = plugin.getClanManager().getClanByName(clanName);
        if (clan == null) {
            return;
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
