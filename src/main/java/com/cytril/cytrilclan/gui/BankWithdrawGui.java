package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated read-only view of the clan bank contents (45 items per page in
 * the top rows). Clicking an item opens a per-item withdraw-confirm screen
 * (see BankWithdrawConfirmGui) rather than withdrawing instantly, so partial
 * stacks and misclicks can't drain the bank by accident.
 */
public final class BankWithdrawGui {

    private BankWithdrawGui() {
    }

    /** Returns the non-null, non-air slot indices of the clan's bank contents, in order. */
    public static List<Integer> nonEmptySlots(Clan clan) {
        List<Integer> slots = new ArrayList<>();
        ItemStack[] contents = clan.getBankContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static Inventory build(Clan clan, int page, int perPage) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BANK_WITHDRAW, clan, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, MessageUtil.color("&8Withdraw Items &7- &fPage " + (page + 1)));
        holder.setInventory(inv);

        List<Integer> bankSlots = nonEmptySlots(clan);
        int start = page * perPage;
        int end = Math.min(start + perPage, bankSlots.size());

        int displaySlot = 0;
        for (int i = start; i < end && displaySlot < 45; i++, displaySlot++) {
            ItemStack original = clan.getBankContents()[bankSlots.get(i)];
            ItemStack display = original.clone();
            inv.setItem(displaySlot, display);
        }

        boolean hasNext = end < bankSlots.size();
        GuiUtil.fillControlRow(inv, 45, true, hasNext);
        return inv;
    }
}
