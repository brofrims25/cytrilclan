package com.cytril.cytrilclan.command;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.model.Clan;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanCommand implements CommandExecutor {

    private final CytrilClan plugin;

    public ClanCommand(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            plugin.getGUIManager().openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage("§cGunakan: /clan create <nama>");
                    return true;
                }
                boolean created = plugin.getClanManager().createClan(player, args[1]);
                if (created) {
                    player.sendMessage("§aClan " + args[1] + " berhasil dibuat!");
                } else {
                    player.sendMessage("§cGagal membuat clan! Nama terlarang atau Anda tidak punya izin.");
                }
            }
            case "bank" -> plugin.getGUIManager().openMainMenu(player);
            case "chat" -> {
                if (args.length < 2) return true;
                Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                if (clan != null) {
                    String msg = String.join(" ", args).substring(5);
                    clan.getMembers().keySet().forEach(uuid -> {
                        Player p = plugin.getServer().getPlayer(uuid);
                        if (p != null) p.sendMessage("§8[§bClanChat§8] §f" + player.getName() + ": §7" + msg);
                    });
                }
            }
            default -> plugin.getGUIManager().openMainMenu(player);
        }
        return true;
    }
}
