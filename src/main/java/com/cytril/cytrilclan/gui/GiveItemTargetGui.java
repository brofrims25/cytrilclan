package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Step 2 of the give-item flow: pick which online clan member receives the
 * item held in context. Context on the holder is the ItemStack to give.
 */
public final class GiveItemTargetGui {

    private GiveItemTargetGui() {
    }

    public static Inventory build(Clan clan, ItemStack itemToGive) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.GIVE_ITEM_TARGET, clan, 0, itemToGive);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Give Item &7- Choose Recipient"));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        List<ClanMember> members = new ArrayList<>(clan.getMembers().values());
        int slot = 0;
        for (ClanMember member : members) {
            if (slot >= 18) break; // reserve bottom row for nav
            if (Bukkit.getPlayer(member.getUuid()) == null) continue; // online only
            inv.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .skullOwner(member.getUuid())
                    .name("&f" + member.getLastKnownName())
                    .lore("&eClick to give the item")
                    .build());
            slot++;
        }

        GuiUtil.fillControlRow(inv, 18, true, false);
        return inv;
    }
}
