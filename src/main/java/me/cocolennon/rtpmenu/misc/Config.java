package me.cocolennon.rtpmenu.misc;

import me.cocolennon.rtpmenu.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.*;

public final class Config {
    public final String menuTitle;
    public final String previousPageItem;
    public final String nextPageItem;
    public final boolean rtpInTowns;
    public final boolean rtpInOutposts;
    public final boolean rtpInGriefPrevention;
    public final List<RTPWorld> worlds;

    public final boolean isTownyPresent;
    public final boolean isGriefPreventionPresent;
    public final boolean isItemsAdderPresent;

    public final List<RTPInventoryHolder> pages;

    public Config(Main plugin) {
        FileConfiguration config = plugin.getConfig();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        this.menuTitle = config.getString("menu-title");
        this.previousPageItem = config.getString("previous-page-item");
        this.nextPageItem = config.getString("next-page-item");
        this.rtpInTowns = config.getBoolean("allow-rtp-in-towns");
        this.rtpInOutposts = config.getBoolean("allow-rtp-in-outposts");
        this.rtpInGriefPrevention = config.getBoolean("allow-rtp-in-griefprevention");
        this.worlds = loadWorlds(config.getConfigurationSection("worlds"));
        this.isTownyPresent = pluginManager.isPluginEnabled("Towny");
        this.isGriefPreventionPresent = pluginManager.isPluginEnabled("GriefPrevention");
        this.isItemsAdderPresent = pluginManager.isPluginEnabled("ItemsAdder");
        this.pages = getPages(plugin);
    }

    public RTPWorld getWorld(String worldName) {
        return worlds.stream().filter(world -> world.worldName.equalsIgnoreCase(worldName)).findFirst().orElse(null);
    }

    private List<RTPWorld> loadWorlds(ConfigurationSection configWorlds) {
        if(configWorlds == null) return List.of();
       List<RTPWorld> worlds = new ArrayList<>();
        for(String worldName : configWorlds.getKeys(false)) {
            ConfigurationSection world = configWorlds.getConfigurationSection(worldName);
            if(world == null) continue;
            String itemName = world.getString("item");
            List<String> blacklistedBlocksNames = world.getStringList("blacklisted-blocks");
            int maxX = world.getInt("max-x");
            int maxZ = world.getInt("max-z");
            worlds.add(new RTPWorld(worldName, itemName, blacklistedBlocksNames, maxX, maxZ));
        }
        return worlds;
    }

    private List<RTPInventoryHolder> getPages(Main plugin) {
        List<RTPInventoryHolder> pages = new ArrayList<>();
        int worldCount = 0;
        int slotsToAssign = 3;
        int pagesCount = this.worlds.size() / 3;

        for(int pageNumber = 0; pageNumber <= pagesCount; pageNumber++){
            RTPInventoryHolder newPage = new RTPInventoryHolder(plugin, this.menuTitle);
            List<RTPWorld> worldsInPage = new ArrayList<>();
            if(pageNumber == pagesCount) slotsToAssign = this.worlds.size() - worldCount;
            for(int i = 0; i < slotsToAssign; i++) {
                worldsInPage.add(this.worlds.get(worldCount));
                worldCount++;
            }
            int menuSlot = slotsToAssign == 1 ? 13 : 11;
            for(RTPWorld world : worldsInPage) {
                ItemStack worldItem = ItemUtil.getWorldItem(world);
                newPage.setItem(menuSlot, worldItem);
                menuSlot += slotsToAssign == 2 ? 4 : 2;
            }
            if(pageNumber < pagesCount && this.worlds.size() > 3) newPage.setItem(23, ItemUtil.getNextPageItem(pageNumber + 1, this.nextPageItem));
            if(pageNumber > 0) newPage.setItem(21, ItemUtil.getPreviousPageItem(pageNumber - 1, this.previousPageItem));
            pages.add(newPage);
        }
        return pages;
    }
}
