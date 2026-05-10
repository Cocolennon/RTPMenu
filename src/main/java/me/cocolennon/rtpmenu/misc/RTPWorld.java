package me.cocolennon.rtpmenu.misc;

import me.cocolennon.rtpmenu.Main;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public final class RTPWorld {
    public final String worldName;
    public final String itemName;
    public final String displayName;
    public final List<Material> blacklistedBlocks;
    public final int maxX;
    public final int maxZ;

    public RTPWorld(String worldName, String itemName, String displayName, List<String> blacklistedBlocksNames, int maxX, int maxZ) {
        this.worldName = worldName;
        this.itemName = itemName;
        this.displayName = displayName;
        this.maxX = maxX;
        this.maxZ = maxZ;
        List<Material> blacklistedBlocks = new ArrayList<>();
        for(String blockName : blacklistedBlocksNames) {
            Material material =  Material.matchMaterial(blockName.toUpperCase());
            if(material == null || !material.isBlock()) {
                Main.getInstance().getLogger().warning(blockName + " isn't a block!");
                continue;
            }
            blacklistedBlocks.add(material);
        }
        this.blacklistedBlocks = blacklistedBlocks;
    }
}
