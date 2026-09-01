package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public final class SettingsGui {

    private SettingsGui() {
    }

    public static Inventory build(Clan clan) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.SETTINGS, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Clan Settings &7- &f" + clan.getName()));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        inv.setItem(11, new ItemBuilder(Material.SHIELD)
                .name("&bEdit Banner")
                .lore("&7Hold a banner and click to", "&7set it as the clan banner.")
                .build());

        inv.setItem(13, new ItemBuilder(Material.NAME_TAG)
                .name("&eRename Clan")
                .lore("&7Click, then type the new", "&7name in chat.")
                .build());

        inv.setItem(15, new ItemBuilder(Material.OAK_SIGN)
                .name("&aRename a Base")
                .lore("&7Click, then choose a base", "&7and type its new name.")
                .build());

        GuiUtil.fillControlRow(inv, 18, true, false);
        return inv;
    }
}
