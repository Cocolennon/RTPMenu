package me.cocolennon.rtpmenu.commands;

import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.util.Localization;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RTPCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) return false;
        if(!player.hasPermission("rtpmenu.teleport")) {
            sender.sendMessage(Localization.get(player, "error.permission", true));
            return false;
        }
        player.openInventory(Main.getInstance().config().pages.getFirst().getInventory());
        return true;
    }
}
