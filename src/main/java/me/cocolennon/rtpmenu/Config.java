package me.cocolennon.rtpmenu;

import me.cocolennon.rtpmenu.objects.SoftDependencies;
import me.cocolennon.rtpmenu.util.ItemUtil;
import me.cocolennon.rtpmenu.objects.RTPInventoryHolder;
import me.cocolennon.rtpmenu.objects.RTPWorld;
import me.cocolennon.rtpmenu.util.Localization;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.*;

public final class Config {
    private final Main plugin;
    private final FileConfiguration config;
    private final YamlConfiguration worldsConfig;

    public final String defaultLocale;
    public final boolean autoUpdaterEnabled;
    public final String menuTitle;
    public final String previousPageItem;
    public final String nextPageItem;
    public final boolean rtpInTowns;
    public final boolean rtpInOutposts;
    public final boolean rtpInGriefPrevention;
    public final boolean enforceWorldPermissions;
    public final int rtpCooldown;
    public final boolean perWorldCooldowns;
    public final List<RTPWorld> worlds;
    public final SoftDependencies softDependencies;
    public final List<RTPInventoryHolder> pages;

    public Config(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.worldsConfig = loadWorldsConfig();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        this.defaultLocale = config.getString("default-locale");
        Localization.init(plugin, defaultLocale);
        this.autoUpdaterEnabled = config.getBoolean("auto-updater-enabled");
        this.menuTitle = config.getString("menu-title");
        this.previousPageItem = config.getString("previous-page-item");
        this.nextPageItem = config.getString("next-page-item");
        this.rtpInTowns = config.getBoolean("allow-rtp-in-towns");
        this.rtpInOutposts = config.getBoolean("allow-rtp-in-outposts");
        this.rtpInGriefPrevention = config.getBoolean("allow-rtp-in-griefprevention");
        this.enforceWorldPermissions = config.getBoolean("enforce-world-permissions");
        this.rtpCooldown = config.getInt("rtp-cooldown");
        this.perWorldCooldowns = config.getBoolean("per-world-cooldowns");
        this.worlds = loadWorlds();
        this.softDependencies = new SoftDependencies(pluginManager);
        this.pages = getPages();
    }

    public RTPWorld getWorld(String worldName) {
        return worlds.stream().filter(world -> world.worldName.equalsIgnoreCase(worldName)).findFirst().orElse(null);
    }

    private YamlConfiguration loadWorldsConfig() {
        File worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        if(!worldsFile.exists()) {
            plugin.saveResource("worlds.yml", false);
            worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        }
        YamlConfiguration worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
        migrateWorldsConfig(worldsFile, worldsConfig);
        return worldsConfig;
    }

    private void migrateWorldsConfig(File worldsFile, YamlConfiguration worldsConfig) {
        ConfigurationSection oldWorldsSection = config.getConfigurationSection("worlds");
        if(oldWorldsSection == null) return;
        try {
            worldsConfig.set("worlds", oldWorldsSection);
            worldsConfig.save(worldsFile);
            config.set("worlds", null);
            plugin.saveConfig();
            plugin.getLogger().info("Migrated " + oldWorldsSection.getKeys(false).size() + " world(s) from config.yml to worlds.yml");
        }catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    private List<RTPWorld> loadWorlds() {
        ConfigurationSection configWorlds = worldsConfig.getConfigurationSection("worlds");
        if(configWorlds == null) return List.of();
        List<RTPWorld> worlds = new ArrayList<>();
        for(String worldName : configWorlds.getKeys(false)) {
            ConfigurationSection world = configWorlds.getConfigurationSection(worldName);
            if(world == null) continue;
            String itemName = world.getString("item");
            String displayName = world.getString("name");
            List<String> blacklistedBlocksNames = world.getStringList("blacklisted-blocks");
            int maxX = world.getInt("max-x");
            int maxZ = world.getInt("max-z");
            worlds.add(new RTPWorld(worldName, itemName, displayName, blacklistedBlocksNames, maxX, maxZ));
        }
        return worlds;
    }

    private List<RTPInventoryHolder> getPages() {
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
                ItemStack worldItem = ItemUtil.getWorldItem(world, softDependencies);
                newPage.setItem(menuSlot, worldItem);
                menuSlot += slotsToAssign == 2 ? 4 : 2;
            }
            if(pageNumber < pagesCount && this.worlds.size() > 3) newPage.setItem(23, ItemUtil.getNextPageItem(defaultLocale, pageNumber + 1, this.nextPageItem, softDependencies));
            if(pageNumber > 0) newPage.setItem(21, ItemUtil.getPreviousPageItem(defaultLocale, pageNumber - 1, this.previousPageItem, softDependencies));
            pages.add(newPage);
        }
        return pages;
    }
}
