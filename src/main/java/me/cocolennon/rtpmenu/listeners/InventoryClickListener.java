package me.cocolennon.rtpmenu.listeners;

import me.cocolennon.rtpmenu.Config;
import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.util.ItemUtil;
import me.cocolennon.rtpmenu.objects.RTPInventoryHolder;
import me.cocolennon.rtpmenu.objects.RTPWorld;
import me.cocolennon.rtpmenu.util.Localization;
import me.cocolennon.rtpmenu.util.TeleportUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof RTPInventoryHolder)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if(clicked == null || !clicked.hasItemMeta()) return;
        PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(ItemUtil.buttonAction)) return;
        String buttonAction = pdc.get(ItemUtil.buttonAction, PersistentDataType.STRING);
        Main main = Main.getInstance();
        Config config = main.config();
        if(StringUtils.isNumeric(buttonAction)) player.openInventory(config.pages.get(Integer.parseInt(buttonAction)).getInventory());
        else {
            RTPWorld rtpWorld = config.getWorld(buttonAction);
            World world = main.getServer().getWorld(rtpWorld == null ? "RTPMenuWorldDoesNotExist" : rtpWorld.worldName);
            if(rtpWorld == null || world == null) {
                player.sendMessage(Localization.get(player, "error.teleport", true));
                main.getLogger().warning(Localization.console("console.world-does-not-exist", buttonAction));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            if(config.enforceWorldPermissions && !player.hasPermission("rtpmenu.world." + rtpWorld.worldName)) {
                player.sendMessage(Localization.get(player, "error.world-permission", true, rtpWorld.displayName));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            if(TeleportUtil.checkCooldown(player, rtpWorld)) return;
            TeleportUtil.startTeleport(player, rtpWorld, world);
            player.closeInventory();
        }
    }
}
