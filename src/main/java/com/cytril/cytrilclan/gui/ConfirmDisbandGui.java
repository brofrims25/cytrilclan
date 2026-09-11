package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/**
 * A single "are you sure?" screen shown before permanently deleting a clan.
 * Disbanding can't be undone, so it never happens from a single click anywhere
 * in the plugin - this confirm screen is always the last step.
 */
public final class ConfirmDisbandGui {

    public static final int CONFIRM_SLOT = 11;
    public static final int CANCEL_SLOT = 15;

    private ConfirmDisbandGui() {
    }

    public static Inventory build(Clan clan) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.CONFIRM_DISBAND, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&c&lConfirm Disband"));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        inv.setItem(13, new ItemBuilder(Material.TNT)
                .name("&c&lThis cannot be undone!")
                .lore(
                        "&7Disbanding &f" + clan.getName() + " &7will:",
                        "&7- Remove all " + clan.getSize() + " member(s)",
                        "&7- Delete the bank and its history",
                        "&7- Delete all saved bases"
                )
                .build());

        inv.setItem(CONFIRM_SLOT, new ItemBuilder(Material.RED_WOOL)
                .name("&c&lYes, disband the clan")
                .lore("&7This is permanent.")
                .build());

        inv.setItem(CANCEL_SLOT, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&lNo, keep the clan")
                .lore("&7Return to the clan menu.")
                .build());

        return inv;
    }
}
