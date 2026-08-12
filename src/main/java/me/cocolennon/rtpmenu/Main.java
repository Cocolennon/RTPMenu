package me.cocolennon.rtpmenu;

import me.cocolennon.rtpmenu.commands.RTPCommand;
import me.cocolennon.rtpmenu.commands.ReloadCommand;
import me.cocolennon.rtpmenu.listeners.InventoryClickListener;
import me.cocolennon.rtpmenu.util.MetricsUtil;
import me.cocolennon.rtpmenu.util.Updater;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private boolean usingOldVersion = false;
    private static Main instance;
    private Config config;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig(false);
        registerCommands();
        registerListeners();
        MetricsUtil.register(instance);
        getLogger().info("Plugin enabled");
        checkVersion();
    }

    public void loadConfig(boolean reload) {
        if(!reload) {
            saveDefaultConfig();
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        reloadConfig();
        config = new Config(this);
    }

    private void registerCommands() {
        getCommand("rtp").setExecutor(new RTPCommand());
        getCommand("rtpreload").setExecutor(new ReloadCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), instance);
    }

    private void checkVersion() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            Updater updater = new Updater(this, "rtp-menu", getFile(), config.autoUpdaterEnabled ? Updater.UpdateType.CHECK_DOWNLOAD : Updater.UpdateType.VERSION_CHECK, false);
            switch(updater.getResult()) {
                case SUCCESS -> {
                    usingOldVersion = true;
                    getLogger().info("Update will be applied after next restart!");
                }
                case UPDATE_FOUND -> {
                    usingOldVersion = true;
                    getLogger().info("You are using an older version of Filtering Hoppers, please update to version " + updater.getVersion());
                }
                case FAILED ->  {
                    usingOldVersion = true;
                    getLogger().warning("An update was found, but the updater failed to download it automatically. You might need to update manually!");
                }
            }
        });
    }

    @Override
    public void onDisable() {
        instance = null;
        config = null;
        getLogger().info("Plugin disabled");
    }

    public Config config() {
        return config;
    }
    public boolean getUsingOldVersion() { return instance.usingOldVersion; }
    public static Main getInstance() {
        return instance;
    }
}
