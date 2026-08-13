package me.cocolennon.rtpmenu.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.cocolennon.rtpmenu.Config;
import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.objects.RTPWorld;
import me.cocolennon.rtpmenu.util.Localization;
import me.cocolennon.rtpmenu.util.TeleportUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RTPCommand {
    public static LiteralCommandNode<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("rtp")
                .then(Commands.literal("menu")
                        .requires(source -> source.getSender().hasPermission("rtpmenu.teleport") && source.getSender() instanceof Player)
                        .executes(RTPCommand::openMenu))
                .then(Commands.literal("info")
                        .requires(source -> source.getSender().hasPermission("rtpmenu.info"))
                        .executes(RTPCommand::info))
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("rtpmenu.reload"))
                        .executes(RTPCommand::reload))
                .then(Commands.argument("world", StringArgumentType.greedyString())
                        .requires(source -> source.getSender().hasPermission("rtpmenu.teleport") && source.getSender() instanceof Player)
                        .suggests(RTPCommand::worldSuggestions)
                        .executes(RTPCommand::teleport))
                .requires(source -> source.getSender().hasPermission("rtpmenu.teleport") && source.getSender() instanceof Player)
                .executes(RTPCommand::openMenu);
        return root.build();
    }

    private static int openMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        player.openInventory(Main.getInstance().config().pages.getFirst().getInventory());
        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 1.0f, 1.0f);
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        List<Component> info = new ArrayList<>();
        info.add(miniMessage.deserialize("<#75FF7A><bold>========================="));
        info.add(miniMessage.deserialize("<#45CC4B><bold>RTP Menu <#45CC4B>" + Main.getInstance().getPluginMeta().getVersion()));
        if(Main.getInstance().getUsingOldVersion()) info.add(miniMessage.deserialize("<#75FF7A>An update is available!"));
        else info.add(miniMessage.deserialize("<#45CC4B>You're using the latest version"));
        info.add(miniMessage.deserialize("<#45CC4B>Made with <#FF5555>❤ <#45CC4B>by Cocolennon"));
        info.add(miniMessage.deserialize("<#75FF7A><bold>========================="));
        info.forEach(sender::sendMessage);
        if(sender instanceof Player player) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Main main = Main.getInstance();
        main.loadConfig(true);
        sender.sendMessage(Localization.get(sender, "reload", true));
        if(main.getUsingOldVersion()) sender.sendMessage(Localization.get(sender, "new-version", true));
        if(sender instanceof Player player) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> worldSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSender sender = context.getSource().getSender();
        Config config = Main.getInstance().config();
        for(RTPWorld rtpWorld : config.worlds) {
            if(!sender.hasPermission("rtpmenu.world." + rtpWorld.worldName)) continue;
            builder.suggest(rtpWorld.displayName);
        }
        return builder.buildFuture();
    }

    private static int teleport(CommandContext<CommandSourceStack> context) {
        Player player =  (Player) context.getSource().getSender();
        Main main = Main.getInstance();
        Config config = main.config();
        String worldName = context.getArgument("world", String.class);
        RTPWorld rtpWorld = config.getWorldFromName(worldName);
        World world = main.getServer().getWorld(rtpWorld == null ? "RTPMenuWorldDoesNotExist" : rtpWorld.worldName);
        if(rtpWorld == null || world == null) {
            player.sendMessage(Localization.get(player, "error.teleport", true));
            main.getLogger().warning(Localization.console("console.world-does-not-exist", rtpWorld.worldName));
            return 0;
        }
        if(config.enforceWorldPermissions && !player.hasPermission("rtpmenu.world." + rtpWorld.worldName)) {
            player.sendMessage(Localization.get(player, "error.world-permission", true, rtpWorld.displayName));
            return 0;
        }
        if(TeleportUtil.checkCooldown(player, rtpWorld)) return 0;
        TeleportUtil.startTeleport(player, rtpWorld, world);
        return Command.SINGLE_SUCCESS;
    }
}
