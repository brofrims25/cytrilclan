package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.manager.ConfigManager;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * The clan's main hub GUI (3 rows / 27 slots). Bottom row acts as the control
 * row per the plugin-wide nav convention, but since this is the top-level menu
 * only the "close" button (slot 22, center of bottom row) is active.
 */
public final class MainMenuGui {

    private MainMenuGui() {
    }

    public static Inventory build(Clan clan, Player viewer, ConfigManager config) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.MAIN_MENU, clan);
        Inventory inv = Bukkit.createInventory(holder, 27, MessageUtil.color("&8Clan Menu &7- " + clan.getFormattedName()));
        holder.setInventory(inv);

        ClanMember viewerMember = clan.getMember(viewer.getUniqueId());
        ClanRole role = viewerMember == null ? ClanRole.MEMBER : viewerMember.getRole();

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, GuiUtil.FILLER);
        }

        if (config.isBankEnabled()) {
            inv.setItem(10, new ItemBuilder(Material.GOLD_INGOT)
                    .name("&6Clan Bank")
                    .lore("&7Deposit, withdraw and view", "&7transaction history.")
                    .build());
        } else {
            inv.setItem(10, disabledButton());
        }

        inv.setItem(12, new ItemBuilder(Material.PLAYER_HEAD)
                .name("&bMembers")
                .lore("&7View and manage clan members.", "&7" + clan.getSize() + " member(s)")
                .build());

        if (config.isBasesEnabled()) {
            inv.setItem(14, new ItemBuilder(Material.MAP)
                    .name("&aBases")
                    .lore("&7Teleport to or manage bases.", "&7" + clan.getBases().size() + "/3 base(s)")
                    .build());
        } else {
            inv.setItem(14, disabledButton());
        }

        if (config.isGiveItemEnabled()) {
            inv.setItem(16, new ItemBuilder(Material.CHEST)
                    .name("&dGive Item")
                    .lore("&7Give an item to a clan member.")
                    .build());
        } else {
            inv.setItem(16, disabledButton());
        }

        boolean canEditSettings = role.atLeast(ClanRole.OFFICER);
        inv.setItem(21, new ItemBuilder(canEditSettings ? Material.WRITABLE_BOOK : Material.BOOK)
                .name(canEditSettings ? "&eSettings" : "&7Settings &c(locked)")
                .lore(canEditSettings
                        ? new String[]{"&7Edit clan banner, name and bases."}
                        : new String[]{"&cOnly officers and the leader", "&ccan edit settings."})
                .build());

        boolean isLeader = clan.getLeader().equals(viewer.getUniqueId());
        inv.setItem(23, new ItemBuilder(isLeader ? Material.TNT : Material.OAK_DOOR)
                .name(isLeader ? "&c&lDisband Clan" : "&cLeave Clan")
                .lore(isLeader ? "&7Permanently delete this clan." : "&7Leave " + clan.getName() + ".")
                .build());

        inv.setItem(22, new ItemBuilder(Material.BARRIER)
                .name("&c« Close Menu")
                .lore("&7Close this menu.")
                .build());

        return inv;
    }

    private static org.bukkit.inventory.ItemStack disabledButton() {
        return new ItemBuilder(Material.BARRIER)
                .name("&7Disabled")
                .lore("&7This feature is disabled", "&7on this server.")
                .build();
    }
}
