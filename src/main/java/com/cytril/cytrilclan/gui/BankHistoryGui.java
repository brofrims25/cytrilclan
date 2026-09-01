package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.BankTransaction;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Paginated read-only log of IN (green) / OUT (red) bank transactions, newest first. */
public final class BankHistoryGui {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private BankHistoryGui() {
    }

    public static Inventory build(Clan clan, int page, int perPage) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BANK_HISTORY, clan, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, MessageUtil.color("&8Transaction History &7- &fPage " + (page + 1)));
        holder.setInventory(inv);

        List<BankTransaction> transactions = clan.getTransactions();
        int start = page * perPage;
        int end = Math.min(start + perPage, transactions.size());

        int slot = 0;
        for (int i = start; i < end && slot < 45; i++, slot++) {
            BankTransaction tx = transactions.get(i);
            boolean in = tx.getType() == BankTransaction.Type.IN;
            inv.setItem(slot, new ItemBuilder(in ? Material.LIME_DYE : Material.RED_DYE)
                    .name((in ? "&a+ IN" : "&c- OUT") + " &f" + tx.getAmount() + "x " + tx.getMaterial().name())
                    .lore(
                            "&7Player: &f" + tx.getPlayerName(),
                            "&7Date: &f" + FORMAT.format(new Date(tx.getTimestamp()))
                    )
                    .build());
        }

        boolean hasNext = end < transactions.size();
        GuiUtil.fillControlRow(inv, 45, true, hasNext);
        return inv;
    }
}
