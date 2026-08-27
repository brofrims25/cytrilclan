package com.cytril.cytrilclan.listener;

import com.cytril.cytrilclan.CytrilClan;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final CytrilClan plugin;

    public GUIListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.contains("Clan Menu") || title.contains("Kelola:")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            switch (event.getCurrentItem().getType()) {
                case BARRIER -> {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
                }
                case RED_DYE -> {
                    plugin.getGUIManager().openMainMenu(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                }
                default -> player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }
}
