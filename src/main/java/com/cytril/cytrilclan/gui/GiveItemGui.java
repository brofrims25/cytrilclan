package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/**
 * Step 1 of the give-item flow: player places the item to give in a single
 * slot, then clicks confirm to move on to picking a recipient (GiveItemTargetGui).
 */
public final class GiveItemGui {

    public static final int ITEM_SLOT = 13;
    public static final int CONFIRM_SLOT = 22;

    private GiveItemGui() {
    }

    public static Inventory build(Clan clan) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.GIVE_ITEM, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Give Item &7- Step 1"));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) {
            if (i != ITEM_SLOT) inv.setItem(i, GuiUtil.FILLER);
        }

        inv.setItem(CONFIRM_SLOT, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&lNext: Choose Recipient")
                .lore("&7Place the item in the slot", "&7above, then click here.")
                .build());

        return inv;
    }
}
