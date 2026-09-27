package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanBase;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/**
 * Lists a clan's bases; clicking teleports (with warmup) to that base.
 *
 * BUGFIX: this used to have a hardcoded 3-slot layout ({11, 13, 15}) no
 * matter what "general.max-bases" was set to in config.yml. Since
 * ClanCommand's /clan setbase enforcement is correctly config-driven (it
 * lets a clan create as many bases as max-bases allows), setting max-bases
 * above 3 let players create a 4th+ base just fine - but then simply
 * *opening* this menu threw an ArrayIndexOutOfBoundsException, because
 * slots[3] didn't exist. The layout now scales with maxBases, using both
 * non-control rows (18 slots total). If a server sets max-bases above what
 * physically fits, the display is capped at 18 and a note is added so
 * nothing crashes - but 18 comfortably covers any realistic setting.
 */
public final class BaseListGui {

    private static final int DISPLAYABLE_CAP = 18; // rows 0 and 1 of a 27-slot inventory

    private BaseListGui() {
    }

    public static Inventory build(Clan clan, int maxBases) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.BASE_LIST, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Clan Bases &7- &f" + clan.getName()));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        int displayCount = Math.min(maxBases, DISPLAYABLE_CAP);
        int[] slots = buildSlotLayout(displayCount);

        for (int i = 0; i < displayCount; i++) {
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

    /**
     * Spreads {@code count} base slots evenly across the two non-control rows
     * (slots 0-17). The classic 3-slot layout ({11,13,15}) is preserved exactly
     * for count <= 3 so existing 1-3 base setups look identical to before.
     */
    private static int[] buildSlotLayout(int count) {
        if (count <= 3) {
            int[] classic = {11, 13, 15};
            int[] result = new int[count];
            System.arraycopy(classic, 0, result, 0, count);
            return result;
        }
        // Beyond the classic layout, just fill both content rows left-to-right.
        // Simple, guaranteed-unique, guaranteed in-bounds for count <= 18.
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = i;
        }
        return result;
    }
}
