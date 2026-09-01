package com.cytril.cytrilclan.manager;

import com.cytril.cytrilclan.model.BankTransaction;
import com.cytril.cytrilclan.model.Clan;
import com.cytril.cytrilclan.model.ClanBase;
import com.cytril.cytrilclan.model.ClanMember;
import com.cytril.cytrilclan.model.ClanRole;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Persists each Clan as a single flat-file YAML document under
 * plugins/CytrilClan/clans/<name>.yml
 */
public class StorageManager {

    private final JavaPlugin plugin;
    private final File clansFolder;

    public StorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.clansFolder = new File(plugin.getDataFolder(), "clans");
        if (!clansFolder.exists()) {
            clansFolder.mkdirs();
        }
    }

    public File getClansFolder() {
        return clansFolder;
    }

    public List<Clan> loadAll() {
        List<Clan> clans = new ArrayList<>();
        File[] files = clansFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return clans;
        }
        for (File file : files) {
            try {
                Clan clan = load(file);
                if (clan != null) {
                    clans.add(clan);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load clan file " + file.getName(), ex);
            }
        }
        return clans;
    }

    private Clan load(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String name = yml.getString("name");
        String tag = yml.getString("tag");
        UUID leader = UUID.fromString(yml.getString("leader"));
        long createdAt = yml.getLong("created-at", System.currentTimeMillis());
        Clan clan = new Clan(name, tag, leader, createdAt);
        clan.setBannerData(yml.getString("banner-data"));

        if (yml.isConfigurationSection("members")) {
            for (String uuidStr : yml.getConfigurationSection("members").getKeys(false)) {
                String path = "members." + uuidStr + ".";
                UUID uuid = UUID.fromString(uuidStr);
                String lastName = yml.getString(path + "name", "Unknown");
                ClanRole role = ClanRole.valueOf(yml.getString(path + "role", "MEMBER"));
                long joinedAt = yml.getLong(path + "joined-at", createdAt);
                ClanMember member = new ClanMember(uuid, lastName, role, joinedAt);
                member.setKills(yml.getInt(path + "kills", 0));
                member.setDeaths(yml.getInt(path + "deaths", 0));
                clan.getMembers().put(uuid, member);
            }
        }

        for (String key : yml.getStringList("bases-index")) {
            String path = "bases." + key + ".";
            ClanBase base = new ClanBase(
                    yml.getString(path + "name"),
                    yml.getString(path + "world"),
                    yml.getDouble(path + "x"),
                    yml.getDouble(path + "y"),
                    yml.getDouble(path + "z"),
                    (float) yml.getDouble(path + "yaw"),
                    (float) yml.getDouble(path + "pitch")
            );
            clan.getBases().add(base);
        }

        String bankB64 = yml.getString("bank-contents");
        if (bankB64 != null && !bankB64.isEmpty()) {
            clan.setBankContents(deserializeItems(bankB64));
        }

        if (yml.isConfigurationSection("transactions")) {
            for (String key : yml.getConfigurationSection("transactions").getKeys(false)) {
                String path = "transactions." + key + ".";
                try {
                    BankTransaction.Type type = BankTransaction.Type.valueOf(yml.getString(path + "type"));
                    String playerName = yml.getString(path + "player");
                    Material material = Material.valueOf(yml.getString(path + "material"));
                    int amount = yml.getInt(path + "amount");
                    long timestamp = yml.getLong(path + "timestamp");
                    clan.getTransactions().add(new BankTransaction(type, playerName, material, amount, timestamp));
                } catch (Exception ignored) {
                    // skip corrupt individual transaction entries
                }
            }
        }

        return clan;
    }

    public void save(Clan clan) {
        File file = new File(clansFolder, clan.getName().toLowerCase() + ".yml");
        YamlConfiguration yml = new YamlConfiguration();

        yml.set("name", clan.getName());
        yml.set("tag", clan.getTag());
        yml.set("leader", clan.getLeader().toString());
        yml.set("created-at", clan.getCreatedAt());
        yml.set("banner-data", clan.getBannerData());

        for (ClanMember member : clan.getMembers().values()) {
            String path = "members." + member.getUuid() + ".";
            yml.set(path + "name", member.getLastKnownName());
            yml.set(path + "role", member.getRole().name());
            yml.set(path + "joined-at", member.getJoinedAt());
            yml.set(path + "kills", member.getKills());
            yml.set(path + "deaths", member.getDeaths());
        }

        List<String> baseIndex = new ArrayList<>();
        int i = 0;
        for (ClanBase base : clan.getBases()) {
            String key = "base" + i;
            baseIndex.add(key);
            String path = "bases." + key + ".";
            yml.set(path + "name", base.getName());
            yml.set(path + "world", base.getWorldName());
            yml.set(path + "x", base.getX());
            yml.set(path + "y", base.getY());
            yml.set(path + "z", base.getZ());
            yml.set(path + "yaw", base.getYaw());
            yml.set(path + "pitch", base.getPitch());
            i++;
        }
        yml.set("bases-index", baseIndex);

        yml.set("bank-contents", serializeItems(clan.getBankContents()));

        int t = 0;
        for (BankTransaction tx : clan.getTransactions()) {
            String path = "transactions.tx" + t + ".";
            yml.set(path + "type", tx.getType().name());
            yml.set(path + "player", tx.getPlayerName());
            yml.set(path + "material", tx.getMaterial().name());
            yml.set(path + "amount", tx.getAmount());
            yml.set(path + "timestamp", tx.getTimestamp());
            t++;
        }

        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save clan " + clan.getName(), e);
        }
    }

    public void delete(Clan clan) {
        File file = new File(clansFolder, clan.getName().toLowerCase() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    private String serializeItems(ItemStack[] items) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(byteOut)) {
                dataOut.writeInt(items.length);
                for (ItemStack item : items) {
                    dataOut.writeObject(item);
                }
            }
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to serialize bank contents", e);
            return "";
        }
    }

    private ItemStack[] deserializeItems(String data) {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            try (BukkitObjectInputStream dataIn = new BukkitObjectInputStream(byteIn)) {
                int length = dataIn.readInt();
                ItemStack[] items = new ItemStack[length];
                for (int i = 0; i < length; i++) {
                    items[i] = (ItemStack) dataIn.readObject();
                }
                return items;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deserialize bank contents", e);
            return new ItemStack[54];
        }
    }
}
