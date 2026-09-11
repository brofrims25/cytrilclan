package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.manager.NameStyleSelection;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.util.ItemBuilder;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/**
 * Color + bold/regular picker shown before typing a new clan name in chat.
 * The 14 color buttons occupy slots 0-13 (two rows), the bold toggle and a
 * live preview sit on row 2, and a custom control row (Back / Close / Confirm)
 * sits on row 3 - this screen needs a "confirm" action so it doesn't use the
 * generic Back-Leave-Next convention (see GiveItemGui for the same pattern).
 */
public final class NameStyleGui {

    public static final int BOLD_TOGGLE_SLOT = 20;
    public static final int PREVIEW_SLOT = 22;
    public static final int BACK_SLOT = 27;
    public static final int CLOSE_SLOT = 31;
    public static final int CONFIRM_SLOT = 35;

    private NameStyleGui() {
    }

    public static Inventory build(Clan clan, NameStyleSelection selection) {
        ClanGuiHolder holder = new ClanGuiHolder(GuiType.NAME_STYLE, clan, 0, selection);
        Inventory inv = Bukkit.createInventory(holder, 36, MessageUtil.color("&8Pilih Warna & Gaya Nama"));
        holder.setInventory(inv);

        for (int i = 0; i < 36; i++) inv.setItem(i, GuiUtil.FILLER);

        ClanColorOption[] options = ClanColorOption.values();
        for (int i = 0; i < options.length; i++) {
            ClanColorOption option = options[i];
            boolean selected = option.getColorCode().equals(selection.getColorCode());
            ItemBuilder builder = new ItemBuilder(option.getIcon())
                    .name(option.getLabel() + (selected ? " &a\u2713" : ""))
                    .lore(selected ? "&aSedang dipilih" : "&7Klik untuk memilih warna ini");
            if (selected) builder.glow();
            inv.setItem(i, builder.build());
        }

        inv.setItem(BOLD_TOGGLE_SLOT, new ItemBuilder(selection.isBold() ? Material.ANVIL : Material.IRON_INGOT)
                .name(selection.isBold() ? "&aTebal (Bold): ON" : "&7Tebal (Bold): OFF")
                .lore("&7Klik untuk mengubah antara", "&7teks tebal dan tipis.")
                .build());

        String previewText = (selection.isBold() ? "&l" : "") + selection.getColorCode() + clan.getName();
        inv.setItem(PREVIEW_SLOT, new ItemBuilder(Material.NAME_TAG)
                .name(previewText)
                .lore("&7Contoh tampilan nama clan-mu", "&7dengan warna & gaya ini.")
                .build());

        for (int i = 27; i < 36; i++) if (inv.getItem(i) == null) inv.setItem(i, GuiUtil.FILLER);

        inv.setItem(BACK_SLOT, new ItemBuilder(Material.ARROW)
                .name("&e\u00ab Back")
                .lore("&7Kembali ke menu Settings.")
                .build());
        inv.setItem(CLOSE_SLOT, new ItemBuilder(Material.BARRIER)
                .name("&c\u00ab Close Menu")
                .lore("&7Batal, tutup menu ini.")
                .build());
        inv.setItem(CONFIRM_SLOT, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&lLanjut: Ketik Nama")
                .lore("&7Gunakan warna & gaya ini,", "&7lalu ketik nama baru di chat.")
                .build());

        return inv;
    }
}
