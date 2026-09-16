package com.cytril.cytrilclan.listeners;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.manager.PendingActionManager;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanBase;
import com.cytril.cytrilclan.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Captures the next chat line from a player who has an active
 * PendingActionManager entry (clan rename / base rename flows started from
 * SettingsGui) and cancels it from reaching public chat.
 */
public class ChatInputListener implements Listener {

    private final CytrilClan plugin;

    public ChatInputListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PendingActionManager.PendingAction action = plugin.getPendingActionManager().get(player.getUniqueId());
        if (action == null) {
            return;
        }
        if (action.type != PendingActionManager.ActionType.RENAME_CLAN
                && action.type != PendingActionManager.ActionType.RENAME_BASE) {
            return;
        }

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        plugin.getPendingActionManager().clear(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> handleInput(player, action, message));
    }

    private void handleInput(Player player, PendingActionManager.PendingAction action, String input) {
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            return;
        }

        if (input.equalsIgnoreCase("cancel")) {
            MessageUtil.send(player, "&7Cancelled.");
            return;
        }

        if (!plugin.getProfanityFilter().isClean(input)) {
            MessageUtil.sendError(player, "That name isn't allowed. Please try again (or type 'cancel').");
            plugin.getPendingActionManager().set(player.getUniqueId(), action.type, action.context);
            return;
        }

        switch (action.type) {
            case RENAME_CLAN -> {
                if (!input.matches("[A-Za-z0-9_]{3,16}")) {
                    MessageUtil.sendError(player, "Clan names must be 3-16 characters: letters, numbers, underscore only. Try again or type 'cancel'.");
                    plugin.getPendingActionManager().set(player.getUniqueId(), action.type, action.context);
                    return;
                }
                if (plugin.getClanManager().nameTaken(input) && !input.equalsIgnoreCase(clan.getName())) {
                    MessageUtil.sendError(player, "That clan name is already taken.");
                    return;
                }
                String oldName = clan.getName();
                if (action.context instanceof com.cytril.cytrilclan.manager.NameStyleSelection selection) {
                    clan.setNameColorCode(selection.getColorCode());
                    clan.setNameBold(selection.isBold());
                }
                plugin.getClanManager().renameClan(clan, input);
                MessageUtil.sendSuccess(player, "Clan renamed from " + oldName + " to " + input + ".");
                MessageUtil.sendRaw(player, "&7Preview: " + clan.getFormattedName());
            }
            case RENAME_BASE -> {
                String baseName = (String) action.context;
                if (!input.matches("[A-Za-z0-9 _-]{2,24}")) {
                    MessageUtil.sendError(player, "Base names must be 2-24 characters (letters, numbers, spaces, - or _). Try again or type 'cancel'.");
                    plugin.getPendingActionManager().set(player.getUniqueId(), action.type, action.context);
                    return;
                }
                ClanBase base = clan.getBases().stream()
                        .filter(b -> b.getName().equalsIgnoreCase(baseName))
                        .findFirst().orElse(null);
                if (base == null) {
                    MessageUtil.sendError(player, "That base no longer exists.");
                    return;
                }
                base.setName(input);
                plugin.getClanManager().save(clan);
                MessageUtil.sendSuccess(player, "Base renamed to " + input + ".");
            }
            default -> {
            }
        }
    }
}
