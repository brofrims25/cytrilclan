package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Paginated grid of member heads. Clicking a head opens MemberManageGui for that member. */
public final class MemberListGui {

    private MemberListGui() {
    }

    public static Inventory build(Clan clan, int page, int perPage) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.MEMBER_LIST, clan, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, MessageUtil.color("&8Clan Members &7- &fPage " + (page + 1)));
        holder.setInventory(inv);

        List<ClanMember> members = new ArrayList<>(clan.getMembers().values());
        int start = page * perPage;
        int end = Math.min(start + perPage, members.size());

        // maps display slot -> member uuid, stored as context for the listener
        List<UUID> slotIndex = new ArrayList<>();

        int slot = 0;
        for (int i = start; i < end && slot < 45; i++, slot++) {
            ClanMember member = members.get(i);
            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .skullOwner(member.getUuid())
                    .name("&f" + member.getLastKnownName())
                    .lore(
                            "&7Role: " + member.getRole().getDisplay(),
                            "&7Kills: &f" + member.getKills(),
                            "&7Deaths: &f" + member.getDeaths(),
                            "",
                            "&eClick to manage"
                    )
                    .build();
            inv.setItem(slot, head);
            slotIndex.add(member.getUuid());
        }
        holder.setContext(slotIndex);

        boolean hasNext = end < members.size();
        GuiUtil.fillControlRow(inv, 45, true, hasNext);
        return inv;
    }
}
