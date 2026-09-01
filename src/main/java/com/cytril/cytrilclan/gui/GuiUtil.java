package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Shared helpers for building the CytrilClan GUI navigation row.
 * Convention (fixed across every GUI screen in the plugin):
 *   - Back button  -> slot 0 of the control row (bottom-left)
 *   - Leave button -> slot 4 of the control row (bottom-center)
 *   - Next button  -> slot 8 of the control row (bottom-right)
 */
public final class GuiUtil {

    public static final ItemStack FILLER = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();

    private GuiUtil() {
    }

    public static void fillControlRow(Inventory inv, int rowStart, boolean showBack, boolean showNext) {
        for (int i = 0; i < 9; i++) {
            inv.setItem(rowStart + i, FILLER);
        }
        if (showBack) {
            inv.setItem(rowStart, new ItemBuilder(Material.ARROW)
                    .name("&e« Back")
                    .lore("&7Return to the previous menu.")
                    .build());
        }
        inv.setItem(rowStart + 4, new ItemBuilder(Material.BARRIER)
                .name("&c« Close Menu")
                .lore("&7Close this menu.")
                .build());
        if (showNext) {
            inv.setItem(rowStart + 8, new ItemBuilder(Material.ARROW)
                    .name("&eNext »")
                    .lore("&7Go to the next page.")
                    .build());
        }
    }

    public static void fillBorder(Inventory inv, int rows) {
        int size = rows * 9;
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, FILLER);
        }
    }

    public static int controlRowStart(int size) {
        return size - 9;
    }
}
