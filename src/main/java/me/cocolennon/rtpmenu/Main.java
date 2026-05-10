package me.cocolennon.rtpmenu;

import me.cocolennon.rtpmenu.commands.RTPCommand;
import me.cocolennon.rtpmenu.commands.ReloadCommand;
import me.cocolennon.rtpmenu.listeners.InventoryClickListener;
import me.cocolennon.rtpmenu.misc.Config;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private Config config;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        registerCommands();
        registerListeners();
        getLogger().info("Plugin enabled");
    }

    public void loadConfig() {
        reloadConfig();
        this.config = new Config(this);
    }

    private void registerCommands() {
        getCommand("rtp").setExecutor(new RTPCommand());
        getCommand("rtpreload").setExecutor(new ReloadCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), instance);
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("Plugin disabled");
    }

    public Config config() {
        return config;
    }
    public static Main getInstance() {
        return instance;
    }
}
