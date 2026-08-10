package me.cocolennon.rtpmenu.listeners;

import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.util.ItemUtil;
import me.cocolennon.rtpmenu.objects.RTPInventoryHolder;
import me.cocolennon.rtpmenu.objects.RTPWorld;
import me.cocolennon.rtpmenu.util.Localization;
import me.cocolennon.rtpmenu.util.TeleportUtil;
import net.kyori.adventure.text.Component;
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
        if(StringUtils.isNumeric(buttonAction)) player.openInventory(main.config().pages.get(Integer.parseInt(buttonAction)).getInventory());
        else {
            if(TeleportUtil.isOnCooldown(player)) {
                long remaining = TeleportUtil.getRemainingCooldown(player);
                long hours = remaining / 3600;
                long minutes = (remaining % 3600) / 60;
                long seconds = remaining % 60;
                Component localizedMessage;
                if(hours > 1 || (hours == 1 && (minutes > 0))) localizedMessage = Localization.get(player, "error.cooldown.hours", true, hours, minutes);
                else if(minutes > 0 || hours == 1) localizedMessage = Localization.get(player, "error.cooldown.minutes", true, (hours * 60) + minutes, seconds);
                else localizedMessage = Localization.get(player, "error.cooldown.seconds", true, seconds);
                player.sendMessage(localizedMessage);
                return;
            }
            RTPWorld rtpWorld = main.config().getWorld(buttonAction);
            World world = main.getServer().getWorld(rtpWorld == null ? "RTPMenuWorldDoesNotExist" : rtpWorld.worldName);
            if(rtpWorld == null || world == null) {
                player.sendMessage(Localization.get(player, "error.teleport", true));
                main.getLogger().warning(Localization.console("console.world-does-not-exist", buttonAction));
                return;
            }
            TeleportUtil.startTeleport(player, rtpWorld, world);
            player.closeInventory();
        }
    }
}
