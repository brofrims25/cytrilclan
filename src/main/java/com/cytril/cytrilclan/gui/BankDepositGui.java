package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/**
 * Deposit screen: top 4 rows (slots 0-35) are a free drop area for items the
 * player wants to deposit. The bottom row is a custom control row for this
 * screen only (Back / Cancel / Confirm) since "confirm deposit" doesn't fit
 * the generic Back-Leave-Next convention used elsewhere.
 */
public final class BankDepositGui {

    public static final int DEPOSIT_AREA_SIZE = 36; // slots 0-35
    public static final int BACK_SLOT = 36;
    public static final int CANCEL_SLOT = 40;
    public static final int CONFIRM_SLOT = 44;

    private BankDepositGui() {
    }

    public static Inventory build(Clan clan) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BANK_DEPOSIT, clan);
        Inventory inv = Bukkit.createInventory(holder, 45, MessageUtil.color("&8Deposit Items &7- &f" + clan.getName()));
        holder.setInventory(inv);

        for (int i = 36; i < 45; i++) inv.setItem(i, GuiUtil.FILLER);

        inv.setItem(BACK_SLOT, new ItemBuilder(Material.ARROW)
                .name("&e« Back")
                .lore("&7Return to the bank menu.", "&cItems placed above will", "&cbe returned to your inventory.")
                .build());

        inv.setItem(CANCEL_SLOT, new ItemBuilder(Material.BARRIER)
                .name("&c« Close Menu")
                .lore("&7Close without depositing.", "&7Items will be returned to you.")
                .build());

        inv.setItem(CONFIRM_SLOT, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&lConfirm Deposit")
                .lore("&7Move all items placed above", "&7into the clan bank.")
                .build());

        return inv;
    }
}
