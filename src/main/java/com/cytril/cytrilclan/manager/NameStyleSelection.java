package com.cytril.cytrilclan.manager;

/**
 * Holds a player's in-progress color + bold choice while they're using the
 * NameStyleGui, and later as the context object carried by PendingActionManager
 * until the actual name text arrives via chat.
 */
public class NameStyleSelection {

    private String colorCode;
    private boolean bold;

    public NameStyleSelection(String colorCode, boolean bold) {
        this.colorCode = colorCode;
        this.bold = bold;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public NameStyleSelection copy() {
        return new NameStyleSelection(colorCode, bold);
    }
}
