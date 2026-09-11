package com.cytril.cytrilclan.gui;

import org.bukkit.Material;

/**
 * The 14 selectable name/tag colors, each backed by a real Minecraft dye (or
 * ingot for Gold, which has no dye equivalent) so the GUI icon visually
 * matches the resulting text color. Uses only the 16 legacy Minecraft chat
 * color codes (never hex) so the result renders identically on Java and on
 * Bedrock clients connected through Geyser/Floodgate.
 */
public enum ClanColorOption {

    MERAH("&cMerah", "&c", Material.RED_DYE),
    PINK("&dPink", "&d", Material.PINK_DYE),
    PUTIH("&fPutih", "&f", Material.WHITE_DYE),
    HITAM("&8Hitam", "&0", Material.BLACK_DYE),
    UNGU("&5Ungu", "&5", Material.PURPLE_DYE),
    CYAN("&3Cyan", "&3", Material.CYAN_DYE),
    BIRU("&9Biru", "&9", Material.LIGHT_BLUE_DYE),
    BIRU_TUA("&1Biru Tua", "&1", Material.BLUE_DYE),
    HIJAU("&aHijau", "&a", Material.LIME_DYE),
    ABU_ABU("&7Abu-abu", "&7", Material.LIGHT_GRAY_DYE),
    HIJAU_TUA("&2Hijau Tua", "&2", Material.GREEN_DYE),
    KUNING("&eKuning", "&e", Material.YELLOW_DYE),
    ORANGE("&6Orange", "&6", Material.ORANGE_DYE),
    EMAS("&6Emas", "&6", Material.GOLD_INGOT);

    private final String label;
    private final String colorCode;
    private final Material icon;

    ClanColorOption(String label, String colorCode, Material icon) {
        this.label = label;
        this.colorCode = colorCode;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Material getIcon() {
        return icon;
    }
}
