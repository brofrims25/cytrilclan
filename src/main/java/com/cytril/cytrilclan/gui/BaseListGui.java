package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanBase;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/** Lists up to 3 clan bases; clicking teleports (with warmup) to that base. */
public final class BaseListGui {

    private BaseListGui() {
    }

    public static Inventory build(Clan clan, int maxBases) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BASE_LIST, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Clan Bases &7- &f" + clan.getName()));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        int[] slots = {11, 13, 15};
        for (int i = 0; i < maxBases; i++) {
            if (i < clan.getBases().size()) {
                ClanBase base = clan.getBases().get(i);
                inv.setItem(slots[i], new ItemBuilder(Material.COMPASS)
                        .name("&a" + base.getName())
                        .lore(
                                "&7World: &f" + base.getWorldName(),
                                "&7X: &f" + (int) base.getX() + " &7Y: &f" + (int) base.getY() + " &7Z: &f" + (int) base.getZ(),
                                "",
                                "&eClick to teleport"
                        )
                        .build());
            } else {
                inv.setItem(slots[i], new ItemBuilder(Material.GRAY_DYE)
                        .name("&7Empty Base Slot")
                        .lore("&7Use &f/clan base set <name>", "&7at this location to claim it.")
                        .build());
            }
        }

        GuiUtil.fillControlRow(inv, 18, true, false);
        return inv;
    }
}
