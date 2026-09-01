package com.cytril.cytrilclan.listeners;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.manager.PendingActionManager;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

import java.util.List;
import java.util.UUID;

/**
 * Captures the optional kick-reason book. Design note (see README "Known
 * Limitations"): Bukkit only fires PlayerEditBookEvent when a writable book
 * is signed or its pages are saved on inventory close, so the kick does not
 * finalize until the player closes/signs the book they were handed.
 */
public class BookEditListener implements Listener {

    private final CytrilClan plugin;

    public BookEditListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEditBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        PendingActionManager.PendingAction action = plugin.getPendingActionManager().get(player.getUniqueId());
        if (action == null || action.type != PendingActionManager.ActionType.KICK_REASON_BOOK) {
            return;
        }

        UUID targetUuid = (UUID) action.context;
        plugin.getPendingActionManager().clear(player.getUniqueId());

        List<String> pages = event.getNewBookMeta().getPages();
        String reason = String.join(" ", pages).trim();
        if (reason.isEmpty()) {
            reason = "No reason given.";
        }
        final String finalReason = reason;

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            return;
        }
        ClanMember target = clan.getMember(targetUuid);
        String targetName = target != null ? target.getLastKnownName() : "the player";

        plugin.getClanManager().removeMember(clan, targetUuid);
        MessageUtil.sendSuccess(player, targetName + " was kicked. Reason: " + finalReason);

        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        if (targetPlayer != null) {
            MessageUtil.sendError(targetPlayer, "You were kicked from " + clan.getName() + ". Reason: " + finalReason);
        }
    }
}
