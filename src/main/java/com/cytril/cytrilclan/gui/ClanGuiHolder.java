package com.cytril.cytrilclan.gui;

import com.cytril.cytrilclan.model.Clan;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Tags every CytrilClan-owned inventory with its GUI type, owning clan, current
 * page and optional context object (e.g. selected member UUID, selected item slot).
 * ClanGuiListener reads this to route clicks to the right handler.
 */
public class ClanGuiHolder implements InventoryHolder {

    private final GuiType type;
    private final Clan clan;
    private int page;
    private Object context;
    private Inventory inventory;

    public ClanGuiHolder(GuiType type, Clan clan) {
        this(type, clan, 0, null);
    }

    public ClanGuiHolder(GuiType type, Clan clan, int page, Object context) {
        this.type = type;
        this.clan = clan;
        this.page = page;
        this.context = context;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public GuiType getType() {
        return type;
    }

    public Clan getClan() {
        return clan;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }
}
