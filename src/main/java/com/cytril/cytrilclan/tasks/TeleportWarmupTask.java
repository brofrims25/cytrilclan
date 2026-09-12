package com.cytril.cytrilclan.tasks;

import com.cytril.cytrilclan.util.MessageUtil;
import com.cytril.cytrilclan.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Delayed teleport with a movement check so players can't just walk while
 * their teleport counts down. Cancels itself (and notifies the player) if
 * they move more than 1 block or log off before the warmup finishes.
 */
public class TeleportWarmupTask extends BukkitRunnable {

    private final Player player;
    private final Location startLocation;
    private final Location destination;
    private final SoundUtil soundUtil;

    public TeleportWarmupTask(Player player, Location destination, SoundUtil soundUtil) {
        this.player = player;
        this.startLocation = player.getLocation().clone();
        this.destination = destination;
        this.soundUtil = soundUtil;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            return;
        }
        Location current = player.getLocation();
        if (current.distanceSquared(startLocation) > 1.0) {
            MessageUtil.sendError(player, "Teleport cancelled - you moved.");
            return;
        }
        player.teleport(destination);
        soundUtil.play(player, "teleport");
        MessageUtil.sendSuccess(player, "Teleported to the base.");
    }
}
