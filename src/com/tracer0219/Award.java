package com.tracer0219;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Award {
    public String id = "";
    public List<String> commands = new ArrayList<>();
    public int weight;
    public ItemStack head;

    public List<String> vipExtra=new ArrayList<>();

    public Award(UUID uuid) {
        head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        head.setItemMeta(skullMeta);
    }
}
