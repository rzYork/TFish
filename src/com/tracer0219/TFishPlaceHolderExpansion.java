package com.tracer0219;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TFishPlaceHolderExpansion extends PlaceholderExpansion {
    @Override

    public @NotNull String getIdentifier() {
        return "tfish";
    }

    @Override
    public @NotNull String getAuthor() {
        return "tracer0219";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }



    @Override
    public String onRequest(OfflinePlayer p,  @NotNull String params) {
        if(params.equalsIgnoreCase("player_name")){
            return p.getName();
        }else if(params.equalsIgnoreCase("is_in_fishing_area"))
        {
            if(!p.isOnline())
                return "否";
            Location loc = ((Player)p).getLocation();
            RegionManager manager = TFishPlugin.worldGuardInstance.getRegionManager(loc.getWorld());
            ApplicableRegionSet applicableRegions = manager.getApplicableRegions(loc);
            for (ProtectedRegion r : applicableRegions) {
                if(r.getId().equals(TFishPlugin.config.getString("area_name_of_world_guard"))){
                    return "是";
                }
            }
            return "否";
        }
        return super.onRequest(p, params);
    }
}
