package com.cytril.cytrilclan.listeners;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Prepends the player's (colored) clan tag to their chat messages, controlled
 * by config.yml features.chat-tag and the chat.format-* templates. Uses the
 * legacy AsyncPlayerChatEvent (rather than Paper's newer AsyncChatEvent) on
 * purpose: Paper keeps the two events synchronized for compatibility, and the
 * legacy event's simple setFormat(String) plays more predictably alongside
 * other chat-formatting plugins that only listen for the legacy event.
 *
 * Runs at LOW priority so it applies its format before most other chat
 * plugins (which commonly listen at NORMAL/HIGH) have a chance to overwrite
 * it - if another chat-format plugin is also installed, whichever plugin's
 * handler runs last wins, since setFormat() is a single winner-takes-all field.
 */
public class ClanChatListener implements Listener {

    private final CytrilClan plugin;

    public ClanChatListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigManager().isChatTagEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        String template = clan != null
                ? plugin.getConfigManager().getChatFormatWithClan()
                : plugin.getConfigManager().getChatFormatWithoutClan();

        String tag = clan != null ? clan.getFormattedTag() : "";

        String withTag = template.replace("{tag}", tag);
        // %1$s / %2$s are the legacy chat format's placeholders for the
        // player's display name and their message - keep them literal so
        // Bukkit substitutes the real values, and only color-translate the
        // surrounding template text.
        String withPlaceholders = withTag.replace("{player}", "%1$s").replace("{message}", "%2$s");

        event.setFormat(MessageUtil.color(withPlaceholders));
    }
}
