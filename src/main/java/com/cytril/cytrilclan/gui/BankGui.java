package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/** Bank landing screen: Deposit / Withdraw / History, plus the standard nav row. */
public final class BankGui {

    private BankGui() {
    }

    public static Inventory build(Clan clan) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BANK, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Clan Bank &7- &f" + clan.getName()));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        inv.setItem(11, new ItemBuilder(Material.HOPPER)
                .name("&aDeposit Items")
                .lore("&7Add items to the clan bank.")
                .build());

        inv.setItem(13, new ItemBuilder(Material.CHEST_MINECART)
                .name("&eWithdraw Items")
                .lore("&7Take items from the clan bank.")
                .build());

        inv.setItem(15, new ItemBuilder(Material.WRITTEN_BOOK)
                .name("&bTransaction History")
                .lore("&7View past deposits and withdrawals.")
                .build());

        GuiUtil.fillControlRow(inv, 18, true, false);
        return inv;
    }
}
