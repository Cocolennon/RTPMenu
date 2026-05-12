package me.cocolennon.rtpmenu.listeners;

import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.misc.ItemUtil;
import me.cocolennon.rtpmenu.misc.RTPInventoryHolder;
import me.cocolennon.rtpmenu.misc.RTPWorld;
import me.cocolennon.rtpmenu.misc.TeleportUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    final Main main = Main.getInstance();
    final MiniMessage miniMessage = MiniMessage.miniMessage();
    final String RTP_MENU = "<#75FF7A>[<#45CC4B>RTP Menu<#75FF7A>] ";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof RTPInventoryHolder)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if(clicked == null || !clicked.hasItemMeta()) return;
        PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(ItemUtil.buttonAction)) return;
        String buttonAction = pdc.get(ItemUtil.buttonAction, PersistentDataType.STRING);
        if(StringUtils.isNumeric(buttonAction)) player.openInventory(main.config().pages.get(Integer.parseInt(buttonAction)).getInventory());
        else {
            RTPWorld rtpWorld = main.config().getWorld(buttonAction);
            World world = main.getServer().getWorld(rtpWorld == null ? "RTPMenuWorldDoesNotExist" : rtpWorld.worldName);
            if(rtpWorld == null || world == null) {
                player.sendMessage(miniMessage.deserialize(RTP_MENU + "<#FF4545>Something went wrong when teleporting to this world."));
                main.getLogger().warning("World " + buttonAction + " doesn't exist!");
                return;
            }
            TeleportUtil.startTeleport(player, rtpWorld, world);
            player.closeInventory();
        }
        event.setCancelled(true);
    }
}
