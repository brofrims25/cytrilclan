package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

/**
 * Actions available on a single member: promote / demote, kick (with optional
 * book-reason flow), and transfer leadership (delayed 1 hour, cancellable).
 * The holder's context is the target member's UUID.
 */
public final class MemberManageGui {

    public static final int PROMOTE_SLOT = 10;
    public static final int DEMOTE_SLOT = 12;
    public static final int KICK_SLOT = 14;
    public static final int TRANSFER_LEADER_SLOT = 16;

    private MemberManageGui() {
    }

    public static Inventory build(Clan clan, UUID targetUuid, int returnPage) {
        ClanMember target = clan.getMember(targetUuid);
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.MEMBER_MANAGE, clan, returnPage, targetUuid);
        Inventory inv = Bukkit.createInventory(holder, 27,
                MessageUtil.color("&8Manage &f" + (target != null ? target.getLastKnownName() : "Unknown")));
        holder.setInventory(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiUtil.FILLER);

        if (target != null) {
            inv.setItem(PROMOTE_SLOT, new ItemBuilder(Material.LIME_DYE)
                    .name("&aPromote")
                    .lore(target.getRole() == ClanRole.OFFICER
                            ? new String[]{"&cAlready at the highest", "&cpromotable rank."}
                            : new String[]{"&7Promote to the next rank."})
                    .build());

            inv.setItem(DEMOTE_SLOT, new ItemBuilder(Material.GRAY_DYE)
                    .name("&7Demote")
                    .lore(target.getRole() == ClanRole.MEMBER
                            ? new String[]{"&cAlready at the lowest rank."}
                            : new String[]{"&7Demote to the previous rank."})
                    .build());

            inv.setItem(KICK_SLOT, new ItemBuilder(Material.IRON_BOOTS)
                    .name("&cKick Member")
                    .lore("&7Remove this player from the clan.", "&7You'll be able to write a", "&7reason in a book.")
                    .build());

            inv.setItem(TRANSFER_LEADER_SLOT, new ItemBuilder(Material.NETHER_STAR)
                    .name("&6Transfer Leadership")
                    .lore(
                            "&7Make this player the new leader.",
                            "&7Takes effect in 60 minutes and",
                            "&7can be cancelled before then."
                    )
                    .build());
        }

        GuiUtil.fillControlRow(inv, 18, true, false);
        return inv;
    }
}
