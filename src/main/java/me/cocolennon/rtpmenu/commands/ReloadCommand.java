package me.cocolennon.rtpmenu.commands;

import me.cocolennon.rtpmenu.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final String RTP_MENU = "<#75FF7A>[<#45CC4B>RTP Menu<#75FF7A>] ";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) return false;
        MiniMessage miniMessage = MiniMessage.miniMessage();
        if(!player.hasPermission("rtpmenu.reload")) {
            sender.sendMessage(miniMessage.deserialize(RTP_MENU + "<#FF4545>You aren't allowed to do this!"));
            return false;
        }
        Main.getInstance().loadConfig();
        sender.sendMessage(miniMessage.deserialize(RTP_MENU + "<#26F525>Configuration reloaded!"));
        return true;
    }
}