package com.cytril.cytrilclan.listeners;

import com.cytril.cytrilclan.CytrilClan;
import com.cytril.cytrilclan.gui.*;
import com.cytril.cytrilclan.manager.PendingActionManager;
import com.cytril.cytrilclan.model.*;
import com.cytril.cytrilclan.tasks.LeaderTransferTask;
import com.cytril.cytrilclan.tasks.TeleportWarmupTask;
import com.cytril.cytrilclan.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;
import java.util.UUID;

/**
 * Routes every click across all CytrilClan GUIs based on the ClanGuiHolder
 * attached to the clicked inventory. Kept as one listener (rather than one
 * per screen) so the plugin-wide nav convention (back/leave/next) only needs
 * to be interpreted in a single place.
 */
public class ClanGuiListener implements Listener {

    private final CytrilClan plugin;

    public ClanGuiListener(CytrilClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder rawHolder = event.getInventory().getHolder();
        if (!(rawHolder instanceof ClanGuiHolder holder)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Clan clan = plugin.getClanManager().getClanByName(holder.getClan().getName());
        if (clan == null) {
            player.closeInventory();
            return;
        }

        switch (holder.getType()) {
            case MAIN_MENU -> handleMainMenu(event, player, clan);
            case BANK -> handleBank(event, player, clan);
            case BANK_DEPOSIT -> handleBankDeposit(event, player, clan, holder);
            case BANK_WITHDRAW -> handleBankWithdraw(event, player, clan, holder);
            case BANK_WITHDRAW_CONFIRM -> handleBankWithdrawConfirm(event, player, clan, holder);
            case BANK_HISTORY -> handleBankHistory(event, player, clan, holder);
            case SETTINGS -> handleSettings(event, player, clan);
            case MEMBER_LIST -> handleMemberList(event, player, clan, holder);
            case MEMBER_MANAGE -> handleMemberManage(event, player, clan, holder);
            case GIVE_ITEM -> handleGiveItem(event, player, clan);
            case GIVE_ITEM_TARGET -> handleGiveItemTarget(event, player, clan, holder);
            case BASE_LIST -> handleBaseList(event, player, clan, holder);
            default -> event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder rawHolder = event.getInventory().getHolder();
        if (!(rawHolder instanceof ClanGuiHolder holder)) {
            return;
        }
        Player player = (Player) event.getPlayer();

        if (holder.getType() == GuiType.BANK_DEPOSIT) {
            for (int i = 0; i < BankDepositGui.DEPOSIT_AREA_SIZE; i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    returnOrDrop(player, item);
                }
            }
        } else if (holder.getType() == GuiType.GIVE_ITEM) {
            ItemStack item = event.getInventory().getItem(GiveItemGui.ITEM_SLOT);
            if (item != null && item.getType() != Material.AIR) {
                returnOrDrop(player, item);
            }
        }
    }

    private void returnOrDrop(Player player, ItemStack item) {
        var leftover = player.getInventory().addItem(item);
        for (ItemStack extra : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), extra);
        }
    }

    // ------------------------------------------------------------------ MAIN MENU

    private void handleMainMenu(InventoryClickEvent event, Player player, Clan clan) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        switch (slot) {
            case 10 -> open(player, BankGui.build(clan));
            case 12 -> open(player, MemberListGui.build(clan, 0, plugin.getConfigManager().getMembersPerPage()));
            case 14 -> open(player, BaseListGui.build(clan, plugin.getConfigManager().getMaxBases()));
            case 16 -> open(player, GiveItemGui.build(clan));
            case 21 -> {
                ClanMember viewerMember = clan.getMember(player.getUniqueId());
                if (viewerMember != null && viewerMember.getRole().atLeast(ClanRole.OFFICER)) {
                    open(player, SettingsGui.build(clan));
                } else {
                    MessageUtil.sendError(player, "Only officers and the leader can edit settings.");
                }
            }
            case 22 -> player.closeInventory();
            case 23 -> {
                if (clan.getLeader().equals(player.getUniqueId())) {
                    plugin.getClanManager().disbandClan(clan);
                    player.closeInventory();
                    MessageUtil.sendSuccess(player, "Your clan has been disbanded.");
                } else {
                    plugin.getClanManager().removeMember(clan, player.getUniqueId());
                    player.closeInventory();
                    MessageUtil.sendSuccess(player, "You left " + clan.getName() + ".");
                }
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ BANK

    private void handleBank(InventoryClickEvent event, Player player, Clan clan) {
        event.setCancelled(true);
        switch (event.getRawSlot()) {
            case 11 -> open(player, BankDepositGui.build(clan));
            case 13 -> open(player, BankWithdrawGui.build(clan, 0, plugin.getConfigManager().getBankItemsPerPage()));
            case 15 -> open(player, BankHistoryGui.build(clan, 0, plugin.getConfigManager().getHistoryEntriesPerPage()));
            case 18 -> open(player, MainMenuGui.build(clan, player));
            case 22 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleBankDeposit(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < BankDepositGui.DEPOSIT_AREA_SIZE) {
            return; // allow free item movement in the deposit area
        }
        event.setCancelled(true);
        if (slot == BankDepositGui.BACK_SLOT) {
            returnDepositedItems(event.getInventory(), player);
            open(player, BankGui.build(clan));
        } else if (slot == BankDepositGui.CANCEL_SLOT) {
            returnDepositedItems(event.getInventory(), player);
            player.closeInventory();
        } else if (slot == BankDepositGui.CONFIRM_SLOT) {
            confirmDeposit(event.getInventory(), player, clan);
            open(player, BankGui.build(clan));
        }
    }

    private void returnDepositedItems(Inventory inv, Player player) {
        for (int i = 0; i < BankDepositGui.DEPOSIT_AREA_SIZE; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                returnOrDrop(player, item);
                inv.setItem(i, null);
            }
        }
    }

    private void confirmDeposit(Inventory inv, Player player, Clan clan) {
        ItemStack[] bank = clan.getBankContents();
        int deposited = 0;
        for (int i = 0; i < BankDepositGui.DEPOSIT_AREA_SIZE; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            int freeSlot = findFreeBankSlot(bank);
            if (freeSlot == -1) {
                returnOrDrop(player, item); // bank full, give it back
                continue;
            }
            bank[freeSlot] = item.clone();
            clan.addTransaction(new BankTransaction(BankTransaction.Type.IN, player.getName(), item.getType(), item.getAmount(), System.currentTimeMillis()));
            deposited += item.getAmount();
            inv.setItem(i, null);
        }
        clan.setBankContents(bank);
        plugin.getClanManager().save(clan);
        plugin.getSoundUtil().play(player, "deposit");
        if (deposited > 0) {
            MessageUtil.sendSuccess(player, "Deposited " + deposited + " item(s) into the clan bank.");
        }
    }

    private int findFreeBankSlot(ItemStack[] bank) {
        for (int i = 0; i < bank.length; i++) {
            if (bank[i] == null || bank[i].getType() == Material.AIR) return i;
        }
        return -1;
    }

    private void handleBankWithdraw(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        int perPage = plugin.getConfigManager().getBankItemsPerPage();
        int size = event.getInventory().getSize();
        int controlStart = GuiUtil.controlRowStart(size);

        if (slot == controlStart) {
            open(player, BankGui.build(clan));
            return;
        } else if (slot == controlStart + 4) {
            player.closeInventory();
            return;
        } else if (slot == controlStart + 8) {
            List<Integer> nonEmpty = BankWithdrawGui.nonEmptySlots(clan);
            if ((holder.getPage() + 1) * perPage < nonEmpty.size()) {
                open(player, BankWithdrawGui.build(clan, holder.getPage() + 1, perPage));
            }
            return;
        }

        if (slot < 0 || slot >= 45) return;
        ItemStack clicked = event.getInventory().getItem(slot);
        if (clicked == null || clicked.getType() == Material.AIR) return;

        List<Integer> nonEmpty = BankWithdrawGui.nonEmptySlots(clan);
        int index = holder.getPage() * perPage + slot;
        if (index >= nonEmpty.size()) return;
        int bankSlot = nonEmpty.get(index);
        ItemStack bankItem = clan.getBankContents()[bankSlot];
        if (bankItem == null) return;

        open(player, BankWithdrawConfirmGui.build(clan, bankSlot, bankItem, holder.getPage()));
    }

    private void handleBankWithdrawConfirm(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        int perPage = plugin.getConfigManager().getBankItemsPerPage();

        if (slot == BankWithdrawConfirmGui.CONFIRM_SLOT) {
            int bankSlot = (Integer) holder.getContext();
            ItemStack[] bank = clan.getBankContents();
            ItemStack item = bank[bankSlot];
            if (item != null && item.getType() != Material.AIR) {
                bank[bankSlot] = null;
                clan.setBankContents(bank);
                clan.addTransaction(new BankTransaction(BankTransaction.Type.OUT, player.getName(), item.getType(), item.getAmount(), System.currentTimeMillis()));
                plugin.getClanManager().save(clan);
                returnOrDrop(player, item);
                plugin.getSoundUtil().play(player, "withdraw");
                MessageUtil.sendSuccess(player, "Withdrew " + item.getAmount() + "x " + item.getType().name() + ".");
            }
            open(player, BankWithdrawGui.build(clan, holder.getPage(), perPage));
        } else if (slot == BankWithdrawConfirmGui.CANCEL_SLOT) {
            open(player, BankWithdrawGui.build(clan, holder.getPage(), perPage));
        }
    }

    private void handleBankHistory(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int size = event.getInventory().getSize();
        int controlStart = GuiUtil.controlRowStart(size);
        int slot = event.getRawSlot();
        int perPage = plugin.getConfigManager().getHistoryEntriesPerPage();

        if (slot == controlStart) {
            open(player, BankGui.build(clan));
        } else if (slot == controlStart + 4) {
            player.closeInventory();
        } else if (slot == controlStart + 8) {
            if ((holder.getPage() + 1) * perPage < clan.getTransactions().size()) {
                open(player, BankHistoryGui.build(clan, holder.getPage() + 1, perPage));
            }
        }
    }

    // ------------------------------------------------------------------ SETTINGS

    private void handleSettings(InventoryClickEvent event, Player player, Clan clan) {
        event.setCancelled(true);
        ClanMember viewerMember = clan.getMember(player.getUniqueId());
        if (viewerMember == null || !viewerMember.getRole().atLeast(ClanRole.OFFICER)) {
            MessageUtil.sendError(player, "You don't have permission to do that.");
            return;
        }

        switch (event.getRawSlot()) {
            case 11 -> {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().name().endsWith("_BANNER")) {
                    clan.setBannerData(serializeBanner(hand));
                    plugin.getClanManager().save(clan);
                    MessageUtil.sendSuccess(player, "Clan banner updated.");
                } else {
                    MessageUtil.sendError(player, "Hold a banner in your main hand first.");
                }
            }
            case 13 -> {
                player.closeInventory();
                plugin.getPendingActionManager().set(player.getUniqueId(), PendingActionManager.ActionType.RENAME_CLAN, null);
                MessageUtil.send(player, "&eType the new clan name in chat (or 'cancel').");
            }
            case 15 -> open(player, BaseListGui.build(clan, plugin.getConfigManager().getMaxBases()));
            case 18 -> open(player, MainMenuGui.build(clan, player));
            case 22 -> player.closeInventory();
            default -> {
            }
        }
    }

    private String serializeBanner(ItemStack banner) {
        try {
            java.io.ByteArrayOutputStream byteOut = new java.io.ByteArrayOutputStream();
            try (org.bukkit.util.io.BukkitObjectOutputStream out = new org.bukkit.util.io.BukkitObjectOutputStream(byteOut)) {
                out.writeObject(banner);
            }
            return java.util.Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ MEMBERS

    @SuppressWarnings("unchecked")
    private void handleMemberList(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int size = event.getInventory().getSize();
        int controlStart = GuiUtil.controlRowStart(size);
        int slot = event.getRawSlot();
        int perPage = plugin.getConfigManager().getMembersPerPage();

        if (slot == controlStart) {
            open(player, MainMenuGui.build(clan, player));
            return;
        } else if (slot == controlStart + 4) {
            player.closeInventory();
            return;
        } else if (slot == controlStart + 8) {
            if ((holder.getPage() + 1) * perPage < clan.getMembers().size()) {
                open(player, MemberListGui.build(clan, holder.getPage() + 1, perPage));
            }
            return;
        }

        if (slot < 0 || slot >= 45) return;
        Object context = holder.getContext();
        if (!(context instanceof List)) return;
        List<UUID> slotIndex = (List<UUID>) context;
        if (slot >= slotIndex.size()) return;
        UUID targetUuid = slotIndex.get(slot);
        open(player, MemberManageGui.build(clan, targetUuid, holder.getPage()));
    }

    private void handleMemberManage(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        UUID targetUuid = (UUID) holder.getContext();
        ClanMember actorMember = clan.getMember(player.getUniqueId());
        ClanMember target = clan.getMember(targetUuid);
        int returnPage = holder.getPage();
        int perPage = plugin.getConfigManager().getMembersPerPage();

        if (slot == 18) {
            open(player, MemberListGui.build(clan, returnPage, perPage));
            return;
        } else if (slot == 22) {
            player.closeInventory();
            return;
        }

        if (target == null || actorMember == null) {
            MessageUtil.sendError(player, "That player is no longer in the clan.");
            open(player, MemberListGui.build(clan, returnPage, perPage));
            return;
        }
        if (!actorMember.getRole().outranks(target.getRole())) {
            MessageUtil.sendError(player, "You don't outrank that member.");
            return;
        }

        switch (slot) {
            case MemberManageGui.PROMOTE_SLOT -> {
                if (target.getRole() == ClanRole.MEMBER) {
                    target.setRole(ClanRole.OFFICER);
                    plugin.getClanManager().save(clan);
                    MessageUtil.sendSuccess(player, target.getLastKnownName() + " promoted to Officer.");
                    open(player, MemberManageGui.build(clan, targetUuid, returnPage));
                } else {
                    MessageUtil.sendError(player, "That member can't be promoted further from here.");
                }
            }
            case MemberManageGui.DEMOTE_SLOT -> {
                if (target.getRole() == ClanRole.OFFICER) {
                    target.setRole(ClanRole.MEMBER);
                    plugin.getClanManager().save(clan);
                    MessageUtil.sendSuccess(player, target.getLastKnownName() + " demoted to Member.");
                    open(player, MemberManageGui.build(clan, targetUuid, returnPage));
                } else {
                    MessageUtil.sendError(player, "That member is already at the lowest rank.");
                }
            }
            case MemberManageGui.KICK_SLOT -> {
                if (target.getUuid().equals(clan.getLeader())) {
                    MessageUtil.sendError(player, "You can't kick the leader.");
                    return;
                }
                if (event.isShiftClick()) {
                    ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
                    player.getInventory().addItem(book);
                    plugin.getPendingActionManager().set(player.getUniqueId(), PendingActionManager.ActionType.KICK_REASON_BOOK, targetUuid);
                    player.closeInventory();
                    MessageUtil.send(player, "&eWrite a kick reason in the book, then sign or close it.");
                } else {
                    String name = target.getLastKnownName();
                    plugin.getClanManager().removeMember(clan, targetUuid);
                    MessageUtil.sendSuccess(player, name + " was kicked.");
                    Player targetPlayer = Bukkit.getPlayer(targetUuid);
                    if (targetPlayer != null) {
                        MessageUtil.sendError(targetPlayer, "You were kicked from " + clan.getName() + ".");
                    }
                    open(player, MemberListGui.build(clan, returnPage, perPage));
                }
            }
            case MemberManageGui.TRANSFER_LEADER_SLOT -> {
                if (!clan.getLeader().equals(player.getUniqueId())) {
                    MessageUtil.sendError(player, "Only the leader can transfer leadership.");
                    return;
                }
                if (plugin.getClanManager().hasPendingLeaderTransfer(clan)) {
                    plugin.getClanManager().cancelPendingLeaderTransfer(clan);
                    MessageUtil.sendSuccess(player, "Pending leadership transfer cancelled.");
                } else {
                    int delayMinutes = plugin.getConfigManager().getLeaderTransferDelayMinutes();
                    LeaderTransferTask task = new LeaderTransferTask(plugin, clan, targetUuid);
                    var scheduled = task.runTaskLater(plugin, delayMinutes * 60L * 20L);
                    plugin.getClanManager().schedulePendingLeaderTransfer(clan, scheduled);
                    MessageUtil.sendSuccess(player, "Leadership will transfer to " + target.getLastKnownName() + " in " + delayMinutes + " minute(s). Click again to cancel.");
                }
                open(player, MemberManageGui.build(clan, targetUuid, returnPage));
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ GIVE ITEM

    private void handleGiveItem(InventoryClickEvent event, Player player, Clan clan) {
        int slot = event.getRawSlot();
        if (slot == GiveItemGui.ITEM_SLOT) {
            return; // allow placing/removing the item to give
        }
        event.setCancelled(true);
        if (slot == GiveItemGui.CONFIRM_SLOT) {
            ItemStack item = event.getInventory().getItem(GiveItemGui.ITEM_SLOT);
            if (item == null || item.getType() == Material.AIR) {
                MessageUtil.sendError(player, "Place an item in the slot first.");
                return;
            }
            ItemStack toGive = item.clone();
            event.getInventory().setItem(GiveItemGui.ITEM_SLOT, null);
            open(player, GiveItemTargetGui.build(clan, toGive));
        }
    }

    private void handleGiveItemTarget(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 18) {
            open(player, MainMenuGui.build(clan, player));
            return;
        } else if (slot == 22) {
            player.closeInventory();
            return;
        }
        if (slot < 0 || slot >= 18) return;

        ItemStack clicked = event.getInventory().getItem(slot);
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemStack toGive = ((ItemStack) holder.getContext()).clone();
        // find which member this head belongs to by matching displayed order again
        List<ClanMember> members = clan.getMembers().values().stream().toList();
        int idx = 0;
        UUID targetUuid = null;
        for (ClanMember member : members) {
            if (Bukkit.getPlayer(member.getUuid()) == null) continue;
            if (idx == slot) {
                targetUuid = member.getUuid();
                break;
            }
            idx++;
        }
        if (targetUuid == null) return;

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null) {
            MessageUtil.sendError(player, "That player just went offline.");
            return;
        }
        var leftover = target.getInventory().addItem(toGive);
        for (ItemStack extra : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), extra);
        }
        MessageUtil.sendSuccess(player, "Gave the item to " + target.getName() + ".");
        MessageUtil.sendSuccess(target, player.getName() + " gave you an item from the clan.");
        player.closeInventory();
    }

    // ------------------------------------------------------------------ BASES

    private void handleBaseList(InventoryClickEvent event, Player player, Clan clan, ClanGuiHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        boolean renameMode = "RENAME".equals(holder.getContext());

        if (slot == 18) {
            open(player, renameMode ? SettingsGui.build(clan) : MainMenuGui.build(clan, player));
            return;
        } else if (slot == 22) {
            player.closeInventory();
            return;
        }

        int[] slots = {11, 13, 15};
        int baseIndex = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) baseIndex = i;
        }
        if (baseIndex == -1 || baseIndex >= clan.getBases().size()) return;

        ClanBase base = clan.getBases().get(baseIndex);
        if (renameMode) {
            player.closeInventory();
            plugin.getPendingActionManager().set(player.getUniqueId(), PendingActionManager.ActionType.RENAME_BASE, base.getName());
            MessageUtil.send(player, "&eType the new name for base '" + base.getName() + "' in chat (or 'cancel').");
        } else {
            var destination = base.toLocation();
            if (destination == null) {
                MessageUtil.sendError(player, "That base's world isn't loaded right now.");
                return;
            }
            player.closeInventory();
            int warmup = plugin.getConfigManager().getBaseWarmupSeconds();
            MessageUtil.send(player, "&eTeleporting in " + warmup + " second(s), don't move...");
            new TeleportWarmupTask(player, destination, plugin.getSoundUtil()).runTaskLater(plugin, warmup * 20L);
        }
    }

    // ------------------------------------------------------------------ helpers

    private void open(Player player, Inventory inventory) {
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inventory));
    }
}
