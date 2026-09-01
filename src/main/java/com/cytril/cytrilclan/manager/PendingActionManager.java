package com.cytril.cytrilclan.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks short-lived, per-player "waiting for input" state that spans multiple
 * events (chat message capture for renames, book edit capture for kick reasons).
 * Entries are removed as soon as they're consumed or the player disconnects.
 */
public class PendingActionManager {

    public enum ActionType {
        RENAME_CLAN,
        RENAME_BASE,
        KICK_REASON_BOOK
    }

    public static class PendingAction {
        public final ActionType type;
        public final Object context; // e.g. base name being renamed, or target UUID for kicks

        public PendingAction(ActionType type, Object context) {
            this.type = type;
            this.context = context;
        }
    }

    private final Map<UUID, PendingAction> pending = new HashMap<>();

    public void set(UUID player, ActionType type, Object context) {
        pending.put(player, new PendingAction(type, context));
    }

    public PendingAction get(UUID player) {
        return pending.get(player);
    }

    public boolean has(UUID player) {
        return pending.containsKey(player);
    }

    public void clear(UUID player) {
        pending.remove(player);
    }
}
