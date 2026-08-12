package me.cocolennon.rtpmenu.objects;

import org.bukkit.plugin.PluginManager;

public final class SoftDependencies {
    public final boolean isLuckPermsPresent;
    public final boolean isTownyPresent;
    public final boolean isGriefPreventionPresent;
    public final boolean isItemsAdderPresent;
    public final boolean isNexoPresent;
    public final boolean isOraxenPresent;

    public SoftDependencies(PluginManager pluginManager) {
        this.isLuckPermsPresent = pluginManager.isPluginEnabled("LuckPerms");
        this.isTownyPresent = pluginManager.isPluginEnabled("Towny");
        this.isGriefPreventionPresent = pluginManager.isPluginEnabled("GriefPrevention");
        this.isItemsAdderPresent = pluginManager.isPluginEnabled("ItemsAdder");
        this.isNexoPresent = pluginManager.isPluginEnabled("Nexo");
        this.isOraxenPresent = pluginManager.isPluginEnabled("Oraxen");
    }
}
