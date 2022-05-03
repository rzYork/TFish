package com.tracer0219;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TFishPlugin extends JavaPlugin {
    public static ItemStack rod;
    public static FileConfiguration config;
    public static TFishPlugin instance;
    public static WorldGuardPlugin worldGuardInstance;

    public static String prefix;
    public static ProtocolManager protocolManager;

    WeightRandom<Award> weightRandom;

    @Override
    public void onEnable() {
        instance = this;
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists())
            saveDefaultConfig();
        config = getConfig();

        prefix = config.getString("prefix");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        if ((worldGuardInstance = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard")) == null) {
            getLogger().info(prefix + "缺少WorldGuard依赖!");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().info(prefix + "缺少ProtocolLib依赖!");
            getServer().getPluginManager().disablePlugin(this);
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info(prefix + "缺少PlaceholderAPI依赖!");
            getServer().getPluginManager().disablePlugin(this);
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        getServer().getPluginManager().registerEvents(new TListener(), this);
        getServer().getPluginCommand("tf").setExecutor(new TCommandExecutor());

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            TFishPlaceHolderExpansion ex = new TFishPlaceHolderExpansion();
            ex.register();
        }



        loadConfig();

    }

    public static HashMap<String, Award> awardMap = new HashMap<>();

    private void loadConfig() {
        ConfigurationSection awards = config.getConfigurationSection("awards");
        rod=new ItemStack(Material.FISHING_ROD);
        ItemMeta rodMeta = rod.getItemMeta();
        rodMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',getConfig().getString("rod.display")));
        List<String> lore =new ArrayList<>();
        for (String s : getConfig().getStringList("rod.lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&',s));
        }
        rodMeta.setLore(lore);
        ConfigurationSection sec = config.getConfigurationSection("rod.enchantments");
        Set<String> keys = sec.getKeys(false);
        for (String key : keys) {
            rodMeta.addEnchant(Enchantment.getByName(key),sec.getInt(key),true);
        }
        rodMeta.setUnbreakable(true);
        rod.setItemMeta(rodMeta);

        if (awards == null) {
            return;
        }
        for (String awardId : awards.getKeys(false)) {
            ConfigurationSection awardSec = awards.getConfigurationSection(awardId);
            List<String> commands = awardSec.getStringList("commands");
            List<String> vipExtra=awardSec.getStringList("vip_extra");
            if (commands == null)
                commands = new ArrayList<>();
            int weight = awardSec.getInt("weight");
            UUID id = null;
            try {
                id = UUID.fromString(awardSec.getString("item_id_of_player"));
            } catch (Exception e) {
                getLogger().info(prefix + "无效的玩家头颅! ");
                getLogger().info(prefix + awardSec.getString("item_id_of_player"));
                continue;
            }
            Award award = new Award(id);
            award.commands = commands;
            award.id = awardId;
            award.weight = weight;
            award.vipExtra=vipExtra;
            awardMap.put(awardId, award);
        }


        List<WeightRandom.ItemWithWeight<Award>> list = new ArrayList<>();
        for (Map.Entry<String, Award> entry : awardMap.entrySet()) {
            WeightRandom.ItemWithWeight<Award> item = new WeightRandom.ItemWithWeight<>(entry.getValue(), entry.getValue().weight);
            list.add(item);

        }



        weightRandom = new WeightRandom<>(list);

    }

    public Award getAwardRandom() {

        return weightRandom == null ? null : weightRandom.choose();
    }
}
