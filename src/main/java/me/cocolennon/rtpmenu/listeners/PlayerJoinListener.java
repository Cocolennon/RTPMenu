package me.cocolennon.rtpmenu.listeners;

import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.util.Localization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission("rtpmenu.reload")) return;
        if(Main.getInstance().getUsingOldVersion()) player.sendMessage(Localization.get(player, "new-version", true));
    }
}
