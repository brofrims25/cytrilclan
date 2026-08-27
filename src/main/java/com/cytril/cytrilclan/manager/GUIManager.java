package com.cytril.cytrilclan.manager;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final CytrilClan plugin;

    public GUIManager(CytrilClan plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(null, 54, "Clan Menu - " + (clan != null ? clan.getName() : "No Clan"));

        // Layout Navigation Controls
        gui.setItem(45, createItem(Material.RED_DYE, "&cBatal / Back"));
        gui.setItem(49, createItem(Material.BARRIER, "&cKeluar Menu"));
        gui.setItem(53, createItem(Material.LIME_DYE, "&aLanjut / Next"));

        if (clan != null) {
            gui.setItem(4, createItem(Material.RED_BANNER, "&eClan: &b" + clan.getName()));
            gui.setItem(20, createItem(Material.COMPASS, "&aBase Clan"));
            gui.setItem(22, createItem(Material.CHEST, "&aBank Clan"));
            gui.setItem(24, createItem(Material.PLAYER_HEAD, "&aDaftar Member"));
            gui.setItem(31, createItem(Material.ANVIL, "&aSetting Clan"));
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    public void openMemberActionMenu(Player leader, ClanMember target) {
        Inventory gui = Bukkit.createInventory(null, 27, "Kelola: " + target.getName());

        // Navigation
        gui.setItem(18, createItem(Material.RED_DYE, "&cBack"));
        gui.setItem(22, createItem(Material.BARRIER, "&cLeave"));

        // Actions
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(target.getUuid()));
        meta.setDisplayName("§e" + target.getName());
        head.setItemMeta(meta);

        gui.setItem(13, head);
        gui.setItem(11, createItem(Material.CHEST, "&aGive Item (10 Slot)"));
        gui.setItem(12, createItem(Material.BOOK, "&aStatistik Player"));
        gui.setItem(14, createItem(Material.OAK_DOOR, "&cKick Member"));
        gui.setItem(15, createItem(Material.GOLD_INGOT, "&ePromosi / Demosi Rank"));

        leader.openInventory(gui);
        leader.playSound(leader.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    public void openConfirmMenu(Player player, String title, Runnable onConfirm, Runnable onCancel) {
        Inventory gui = Bukkit.createInventory(null, 27, title);
        gui.setItem(11, createItem(Material.RED_STAINED_GLASS_PANE, "&cBatal"));
        gui.setItem(15, createItem(Material.LIME_STAINED_GLASS_PANE, "&aKonfirmasi"));
        player.openInventory(gui);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name.replace("&", "§"));
        if (lore.length > 0) {
            meta.setLore(Arrays.stream(lore).map(l -> l.replace("&", "§")).toList());
        }
        item.setItemMeta(meta);
        return item;
    }
}
