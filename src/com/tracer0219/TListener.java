package com.tracer0219;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketTypeEnum;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.primitives.UnsignedBytes;
import com.sk89q.worldguard.bukkit.listener.WorldGuardPlayerListener;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import static com.tracer0219.TFishPlugin.*;

public class TListener implements Listener {
    public static ProtocolManager pm = TFishPlugin.protocolManager;
    public static HashSet<Player> vipPlayerFishing = new HashSet<>(), playerFishing = new HashSet<>();

    public void CheckPlayer(Player p, Location from, Location to) {
        if (from.getWorld() == to.getWorld() && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockZ() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        Location loc = to;
        if (!playerFishing.contains(p)) {
            RegionManager manager = TFishPlugin.worldGuardInstance.getRegionManager(loc.getWorld());
            ApplicableRegionSet applicableRegions = manager.getApplicableRegions(loc);
            for (ProtectedRegion r : applicableRegions) {
                if (r.getId().equals(config.get("area_name_of_world_guard"))) {
                    if (p.hasPermission("tf.vip")) {
                        vipPlayerFishing.add(p);
                    }
                    playerFishing.add(p);
                    p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', config.getString(p.hasPermission("tf.vip") ? "enter-message-vip" : "enter-message", " 您已经进进入自动钓鱼区域")));
                    p.getInventory().setItem(config.getInt("rod.slot"), rod);
                    return;
                }
            }
        }

        if (playerFishing.contains(p)) {
            RegionManager manager = TFishPlugin.worldGuardInstance.getRegionManager(loc.getWorld());
            ApplicableRegionSet applicableRegions = manager.getApplicableRegions(loc);
            boolean in = false;
            for (ProtectedRegion r : applicableRegions) {
                if (r.getId().equals(config.get("area_name_of_world_guard"))) {
                    in = true;
                    break;
                }
            }
            if (!in) {
                if (p.hasPermission("tf.vip")) {
                    vipPlayerFishing.remove(p);
                }
                playerFishing.remove(p);
                ItemStack[] contents = p.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null && contents[i].isSimilar(rod)) {
                        p.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                }

            }
        }
    }

    @EventHandler
    public void onPlayerEnterRegion(PlayerMoveEvent e) {
        CheckPlayer(e.getPlayer(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        CheckPlayer(e.getPlayer(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("tf.use")) {
            if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                Location loc = p.getLocation();
                RegionManager manager = TFishPlugin.worldGuardInstance.getRegionManager(loc.getWorld());
                ApplicableRegionSet applicableRegions = manager.getApplicableRegions(loc);
                for (ProtectedRegion r : applicableRegions) {
                    if (r.getId().equals(TFishPlugin.instance.getConfig().getString("area_name_of_world_guard"))) {
                        e.setExpToDrop(0);
                        Item caught = (Item) e.getCaught();
                        caught.setPickupDelay(Integer.MAX_VALUE);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                caught.remove();
                            }
                        }.runTaskLater(TFishPlugin.instance, 20L);

                        Award award = getAward();
                        caught.setItemStack(award.head);
                        for (String command : award.commands) {
                            command = PlaceholderAPI.setPlaceholders(p, command);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        }

                        if (vipPlayerFishing.contains(p)) {
//                            p.getWorld().playSound(p.getLocation(),Sound.ENTITY_FIREWORK_BLAST_FAR,1,1);
                            onEffect(p.getLocation());
                            for (String command : award.vipExtra) {
                                command = PlaceholderAPI.setPlaceholders(p, command);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                        }

                    }
                }
            }
        }
    }

    public void onEffect(Location loc) {
        FireworkEffect.Builder builder = FireworkEffect.builder();
        FireworkEffect effect = builder.flicker(false).trail(false).with(FireworkEffect.Type.BURST).withColor(Color.RED).withFade(Color.RED).build();
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwm = firework.getFireworkMeta();
        fwm.clearEffects();
        fwm.addEffect(effect);
        firework.setFireworkMeta(fwm);
        new BukkitRunnable() {
            public void run() {
                firework.detonate();
            }
        }.runTaskLater(instance, 1);
    }


    public Award getAward() {
        return TFishPlugin.instance.getAwardRandom();
    }
}
