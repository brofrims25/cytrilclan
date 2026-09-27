package com.cytril.cytrilclan.manager;

import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks short-lived, per-player "waiting for input" state that spans multiple
 * events (chat message capture for renames, book edit capture for kick reasons).
 * Entries are removed as soon as they're consumed or the player disconnects.
 *
 * BUGFIX: entries used to never expire on their own. If a player opened the
 * rename/kick-book flow and then just abandoned it (no "cancel", no valid
 * input, no disconnect), ChatInputListener would keep silently swallowing
 * every single chat message they sent afterwards - forever - trying (and
 * failing) to parse it as a new name. Every pending action now carries a
 * timestamp and lazily expires after {@link #timeoutMs}, so normal chat
 * comes back on its own. get()/has() are the only places that need to know
 * about this - every existing caller gets the fix for free.
 */
public class PendingActionManager {

    private static final long DEFAULT_TIMEOUT_MS = 120_000L; // 2 minutes

    public enum ActionType {
        RENAME_CLAN,
        RENAME_BASE,
        KICK_REASON_BOOK
    }

    public static class PendingAction {
        public final ActionType type;
        public final Object context; // e.g. base name being renamed, or target UUID for kicks
        public final long timestamp;

        public PendingAction(ActionType type, Object context, long timestamp) {
            this.type = type;
            this.context = context;
            this.timestamp = timestamp;
        }
    }

    private final Map<UUID, PendingAction> pending = new HashMap<>();
    private final long timeoutMs;

    public PendingActionManager() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public PendingActionManager(long timeoutMs) {
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT_MS;
    }

    public void set(UUID player, ActionType type, Object context) {
        pending.put(player, new PendingAction(type, context, System.currentTimeMillis()));
    }

    public PendingAction get(UUID player) {
        PendingAction action = pending.get(player);
        if (action == null) {
            return null;
        }
        if (System.currentTimeMillis() - action.timestamp > timeoutMs) {
            pending.remove(player);
            notifyExpired(player, action.type);
            return null;
        }
        return action;
    }

    public boolean has(UUID player) {
        return get(player) != null;
    }

    public void clear(UUID player) {
        pending.remove(player);
    }

    private void notifyExpired(UUID uuid, ActionType type) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }
        String what = switch (type) {
            case RENAME_CLAN -> "clan rename";
            case RENAME_BASE -> "base rename";
            case KICK_REASON_BOOK -> "kick-reason book";
        };
        MessageUtil.send(player, "&7Your pending " + what + " request timed out. Chat is back to normal.");
    }
}
