package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Per-item withdraw confirmation. Context stored on the holder is the bank
 * slot index (Integer) of the item being withdrawn, so the confirm handler
 * knows exactly which stack to remove even if the bank contents shift.
 */
public final class BankWithdrawConfirmGui {

    public static final int ITEM_DISPLAY_SLOT = 13;
    public static final int CONFIRM_SLOT = 11;
    public static final int CANCEL_SLOT = 15;

    private BankWithdrawConfirmGui() {
    }

    public static Inventory build(Clan clan, int bankSlot, ItemStack item, int withdrawPage) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BANK_WITHDRAW_CONFIRM, clan, withdrawPage, bankSlot);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Confirm Withdraw"));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        ItemStack displayed = item.clone();
        inv.setItem(ITEM_DISPLAY_SLOT, displayed);

        inv.setItem(CONFIRM_SLOT, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&lConfirm Withdraw")
                .lore("&7Take this stack out of the bank.")
                .build());

        inv.setItem(CANCEL_SLOT, new ItemBuilder(Material.RED_WOOL)
                .name("&c&lCancel")
                .lore("&7Return to the withdraw list.")
                .build());

        return inv;
    }
}
