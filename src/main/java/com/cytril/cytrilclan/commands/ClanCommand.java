package com.cytril.cytrilclan.commands;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.gui.MainMenuGui;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanBase;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Single entry point command: /clan <subcommand>. Most day-to-day interaction
 * happens through the GUIs opened by /clan (no args) or /clan bank, /clan settings
 * etc. Text subcommands cover flows that don't fit a GUI well (create, invite,
 * base set/tp by name for quick access, and admin overrides).
 */
public class ClanCommand implements CommandExecutor, TabCompleter {

    private final CytrilClan plugin;

    public ClanCommand(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendError(sender, "This command can only be used in-game.");
            return true;
        }

        if (args.length == 0) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan == null) {
                MessageUtil.send(player, "&7You're not in a clan. Use &f/clan create <name> <tag>&7 or &f/clan join <name>&7.");
                return true;
            }
            player.openInventory(MainMenuGui.build(clan, player));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> handleCreate(player, args);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player, args);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "disband" -> handleDisband(player);
            case "base" -> handleBase(player, args);
            case "bank" -> openSub(player, () -> com.cytril.cytrilclan.gui.BankGui.build(requireClan(player)));
            case "settings" -> openSub(player, () -> com.cytril.cytrilclan.gui.SettingsGui.build(requireClan(player)));
            case "info" -> handleInfo(player, args);
            case "list" -> handleList(player);
            case "promote" -> handlePromoteDemote(player, args, true);
            case "demote" -> handlePromoteDemote(player, args, false);
            case "transfer" -> handleTransfer(player, args);
            case "give" -> handleGive(player);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private Clan requireClan(Player player) {
        return plugin.getClanManager().getClanByPlayer(player.getUniqueId());
    }

    private void openSub(Player player, java.util.function.Supplier<org.bukkit.inventory.Inventory> supplier) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        player.openInventory(supplier.get());
    }

    private void handleCreate(Player player, String[] args) {
        if (plugin.getClanManager().isInClan(player.getUniqueId())) {
            MessageUtil.sendError(player, "You're already in a clan.");
            return;
        }
        if (!plugin.getLuckPermsHook().hasPermission(player, "cytrilclan.create") && !player.hasPermission("cytrilclan.admin")) {
            MessageUtil.sendError(player, "You don't have permission to create a clan.");
            return;
        }
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /clan create <name> <tag>");
            return;
        }
        String name = args[1];
        String tag = args[2];
        if (name.length() < 3 || name.length() > 16) {
            MessageUtil.sendError(player, "Clan names must be 3-16 characters.");
            return;
        }
        if (tag.length() < 2 || tag.length() > 5) {
            MessageUtil.sendError(player, "Clan tags must be 2-5 characters.");
            return;
        }
        if (!plugin.getProfanityFilter().isClean(name) || !plugin.getProfanityFilter().isClean(tag)) {
            MessageUtil.sendError(player, "That name or tag isn't allowed.");
            return;
        }
        if (plugin.getClanManager().nameTaken(name)) {
            MessageUtil.sendError(player, "That clan name is already taken.");
            return;
        }
        Clan clan = plugin.getClanManager().createClan(name, tag, player);
        MessageUtil.sendSuccess(player, "Clan " + name + " [" + tag + "] created! Use /clan to open the menu.");
    }

    private void handleInvite(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        ClanMember actor = clan.getMember(player.getUniqueId());
        if (actor == null || !actor.getRole().atLeast(ClanRole.OFFICER)) {
            MessageUtil.sendError(player, "Only officers and the leader can invite.");
            return;
        }
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /clan invite <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtil.sendError(player, "That player isn't online.");
            return;
        }
        if (plugin.getClanManager().isInClan(target.getUniqueId())) {
            MessageUtil.sendError(player, "That player is already in a clan.");
            return;
        }
        clan.getPendingInvites().put(target.getUniqueId(), System.currentTimeMillis());
        MessageUtil.sendSuccess(player, "Invited " + target.getName() + " to " + clan.getName() + ".");
        MessageUtil.send(target, "&f" + player.getName() + " invited you to join &6" + clan.getName() + "&f! Use &e/clan join " + clan.getName() + "&f to accept.");
    }

    private void handleJoin(Player player, String[] args) {
        if (plugin.getClanManager().isInClan(player.getUniqueId())) {
            MessageUtil.sendError(player, "You're already in a clan.");
            return;
        }
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /clan join <name>");
            return;
        }
        Clan clan = plugin.getClanManager().getClanByName(args[1]);
        if (clan == null) {
            MessageUtil.sendError(player, "That clan doesn't exist.");
            return;
        }
        Long invitedAt = clan.getPendingInvites().remove(player.getUniqueId());
        if (invitedAt == null) {
            MessageUtil.sendError(player, "You need an invite from an officer or the leader to join.");
            return;
        }
        plugin.getClanManager().addMember(clan, player, ClanRole.MEMBER);
        MessageUtil.sendSuccess(player, "You joined " + clan.getName() + "!");
        for (UUID uuid : clan.getMembers().keySet()) {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null && !online.equals(player)) {
                MessageUtil.send(online, "&6" + player.getName() + " &fjoined the clan.");
            }
        }
    }

    private void handleLeave(Player player) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        if (clan.getLeader().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "The leader can't leave - transfer leadership or disband instead.");
            return;
        }
        plugin.getClanManager().removeMember(clan, player.getUniqueId());
        MessageUtil.sendSuccess(player, "You left " + clan.getName() + ".");
    }

    private void handleKick(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /clan kick <player>. Tip: use the GUI (shift-click Kick) for a book reason.");
            return;
        }
        ClanMember actor = clan.getMember(player.getUniqueId());
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        UUID targetUuid = targetPlayer != null ? targetPlayer.getUniqueId() : null;
        if (targetUuid == null) {
            for (ClanMember m : clan.getMembers().values()) {
                if (m.getLastKnownName().equalsIgnoreCase(args[1])) {
                    targetUuid = m.getUuid();
                    break;
                }
            }
        }
        ClanMember target = targetUuid == null ? null : clan.getMember(targetUuid);
        if (actor == null || target == null) {
            MessageUtil.sendError(player, "That player isn't in your clan.");
            return;
        }
        if (!actor.getRole().outranks(target.getRole())) {
            MessageUtil.sendError(player, "You don't outrank that member.");
            return;
        }
        plugin.getClanManager().removeMember(clan, target.getUuid());
        MessageUtil.sendSuccess(player, target.getLastKnownName() + " was kicked.");
    }

    private void handleDisband(Player player) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        if (!clan.getLeader().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "Only the leader can disband the clan.");
            return;
        }
        plugin.getClanManager().disbandClan(clan);
        MessageUtil.sendSuccess(player, "Clan disbanded.");
    }

    private void handleBase(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        if (args.length < 2) {
            player.openInventory(com.cytril.cytrilclan.gui.BaseListGui.build(clan, plugin.getConfigManager().getMaxBases()));
            return;
        }
        ClanMember actor = clan.getMember(player.getUniqueId());
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("set")) {
            if (actor == null || !actor.getRole().atLeast(ClanRole.OFFICER)) {
                MessageUtil.sendError(player, "Only officers and the leader can set bases.");
                return;
            }
            if (args.length < 3) {
                MessageUtil.sendError(player, "Usage: /clan base set <name>");
                return;
            }
            if (clan.getBases().size() >= plugin.getConfigManager().getMaxBases()) {
                MessageUtil.sendError(player, "This clan already has the maximum number of bases.");
                return;
            }
            String baseName = args[2];
            if (!plugin.getProfanityFilter().isClean(baseName)) {
                MessageUtil.sendError(player, "That base name isn't allowed.");
                return;
            }
            clan.getBases().add(new ClanBase(baseName, player.getLocation()));
            plugin.getClanManager().save(clan);
            MessageUtil.sendSuccess(player, "Base '" + baseName + "' set at your current location.");
        } else {
            MessageUtil.sendError(player, "Unknown base action. Use /clan base or /clan base set <name>.");
        }
    }

    private void handleInfo(Player player, String[] args) {
        Clan clan = args.length >= 2 ? plugin.getClanManager().getClanByName(args[1]) : requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "That clan doesn't exist, or you're not in one.");
            return;
        }
        ClanMember leaderMember = clan.getMember(clan.getLeader());
        MessageUtil.sendRaw(player, "&8&m----&r &6" + clan.getName() + " &8[&f" + clan.getTag() + "&8] &8&m----");
        MessageUtil.sendRaw(player, "&7Leader: &f" + (leaderMember != null ? leaderMember.getLastKnownName() : "Unknown"));
        MessageUtil.sendRaw(player, "&7Members: &f" + clan.getSize());
        MessageUtil.sendRaw(player, "&7Bases: &f" + clan.getBases().size() + "/" + plugin.getConfigManager().getMaxBases());
    }

    private void handleList(Player player) {
        var clans = plugin.getClanManager().getAllClans();
        if (clans.isEmpty()) {
            MessageUtil.send(player, "&7There are no clans yet.");
            return;
        }
        MessageUtil.sendRaw(player, "&8&m----&r &6Clans &8&m----");
        for (Clan clan : clans) {
            MessageUtil.sendRaw(player, "&f" + clan.getName() + " &8[&7" + clan.getTag() + "&8] &7- " + clan.getSize() + " member(s)");
        }
    }

    private void handlePromoteDemote(Player player, String[] args, boolean promote) {
        Clan clan = requireClan(player);
        if (clan == null || args.length < 2) {
            MessageUtil.sendError(player, "Usage: /clan " + (promote ? "promote" : "demote") + " <player>");
            return;
        }
        ClanMember actor = clan.getMember(player.getUniqueId());
        ClanMember target = null;
        for (ClanMember m : clan.getMembers().values()) {
            if (m.getLastKnownName().equalsIgnoreCase(args[1])) {
                target = m;
                break;
            }
        }
        if (actor == null || target == null) {
            MessageUtil.sendError(player, "That player isn't in your clan.");
            return;
        }
        if (!actor.getRole().outranks(target.getRole())) {
            MessageUtil.sendError(player, "You don't outrank that member.");
            return;
        }
        if (promote && target.getRole() == ClanRole.MEMBER) {
            target.setRole(ClanRole.OFFICER);
            MessageUtil.sendSuccess(player, target.getLastKnownName() + " promoted to Officer.");
        } else if (!promote && target.getRole() == ClanRole.OFFICER) {
            target.setRole(ClanRole.MEMBER);
            MessageUtil.sendSuccess(player, target.getLastKnownName() + " demoted to Member.");
        } else {
            MessageUtil.sendError(player, "That member can't be " + (promote ? "promoted" : "demoted") + " further.");
            return;
        }
        plugin.getClanManager().save(clan);
    }

    private void handleTransfer(Player player, String[] args) {
        Clan clan = requireClan(player);
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "Only the leader can transfer leadership.");
            return;
        }
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /clan transfer <player>. Tip: this is easier from /clan (GUI).");
            return;
        }
        MessageUtil.send(player, "&7Use the Members GUI (/clan) to transfer leadership with the confirm/cancel flow.");
    }

    private void handleGive(Player player) {
        Clan clan = requireClan(player);
        if (clan == null) {
            MessageUtil.sendError(player, "You're not in a clan.");
            return;
        }
        player.openInventory(com.cytril.cytrilclan.gui.GiveItemGui.build(clan));
    }

    private void sendHelp(Player player) {
        MessageUtil.sendRaw(player, "&8&m----&r &6CytrilClan Help &8&m----");
        MessageUtil.sendRaw(player, "&e/clan &7- open the clan menu");
        MessageUtil.sendRaw(player, "&e/clan create <name> <tag> &7- create a clan");
        MessageUtil.sendRaw(player, "&e/clan invite <player> &7- invite a player");
        MessageUtil.sendRaw(player, "&e/clan join <name> &7- accept an invite");
        MessageUtil.sendRaw(player, "&e/clan leave &7- leave your clan");
        MessageUtil.sendRaw(player, "&e/clan kick <player> &7- kick a member");
        MessageUtil.sendRaw(player, "&e/clan disband &7- disband your clan (leader only)");
        MessageUtil.sendRaw(player, "&e/clan base [set <name>] &7- manage bases");
        MessageUtil.sendRaw(player, "&e/clan bank &7- open the clan bank");
        MessageUtil.sendRaw(player, "&e/clan settings &7- open clan settings");
        MessageUtil.sendRaw(player, "&e/clan info [name] &7- view clan info");
        MessageUtil.sendRaw(player, "&e/clan list &7- list all clans");
        MessageUtil.sendRaw(player, "&e/clan promote|demote <player> &7- change a member's rank");
        MessageUtil.sendRaw(player, "&e/clan give &7- give an item to a member");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = List.of("create", "invite", "join", "leave", "kick", "disband", "base",
                    "bank", "settings", "info", "list", "promote", "demote", "transfer", "give", "help");
            String partial = args[0].toLowerCase(Locale.ROOT);
            return subs.stream().filter(s -> s.startsWith(partial)).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("base")) {
            return List.of("set").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2 && List.of("invite", "kick", "promote", "demote", "transfer").contains(args[0].toLowerCase(Locale.ROOT))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
