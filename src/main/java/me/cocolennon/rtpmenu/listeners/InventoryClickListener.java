package me.cocolennon.rtpmenu.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.misc.Config;
import me.cocolennon.rtpmenu.misc.ItemUtil;
import me.cocolennon.rtpmenu.misc.RTPInventoryHolder;
import me.cocolennon.rtpmenu.misc.RTPWorld;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
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
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class InventoryClickListener implements Listener {
    final Main main = Main.getInstance();
    final BukkitScheduler scheduler = main.getServer().getScheduler();
    final MiniMessage miniMessage = MiniMessage.miniMessage();
    final String RTP_MENU = "<#75FF7A>[<#45CC4B>RTP Menu<#75FF7A>] ";

    final Map<UUID, Location> pendingTeleports = new HashMap<>();
    final Map<UUID, Integer> countdowns = new HashMap<>();

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
            startTeleport(player, rtpWorld, world);
        }
        event.setCancelled(true);
    }

    private void startTeleport(Player player, RTPWorld rtpWorld, World world) {
        player.closeInventory();
        UUID uuid = player.getUniqueId();
        generateRandomCoordinates(rtpWorld, world, location -> {
            pendingTeleports.put(uuid, location);
        });
        countdowns.put(uuid, 3);
        final AtomicInteger seconds = new AtomicInteger(3);
        scheduler.runTaskTimer(main, countdownTask -> {
            if(!player.isOnline()) {
                pendingTeleports.remove(uuid);
                countdowns.remove(uuid);
                countdownTask.cancel();
            }
            if(seconds.getAndDecrement() > 0) {
                player.sendActionBar(miniMessage.deserialize("<#26F525>Teleporting in " + seconds + " seconds..."));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f, 2f);
                return;
            }
            countdowns.remove(uuid);
            Location location = pendingTeleports.remove(uuid);
            if(location != null && !countdowns.containsKey(uuid)) teleportPlayer(player, location);
            countdownTask.cancel();
        }, 0L, 20L);
    }

    private void generateRandomCoordinates(RTPWorld rtpWorld, World world, Consumer<Location> callback) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        scheduler.runTaskAsynchronously(Main.getInstance(), () -> {
            Location location = null;
            while (location == null) {
                int x = random.nextInt(-rtpWorld.maxX, rtpWorld.maxX + 1);
                int z = random.nextInt(-rtpWorld.maxZ, rtpWorld.maxZ + 1);
                location = tryValidate(x, z, rtpWorld, world);
            }
            Location finalLocation = location;
            scheduler.runTask(Main.getInstance(), () -> callback.accept(finalLocation));
        });
    }

    private void teleportPlayer(Player player, Location location) {
        player.teleport(location);
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 2f);
        player.sendMessage(miniMessage.deserialize(RTP_MENU + "<#26F525>Teleported to X: " + (int)location.getX() + " Y: " + (int)location.getY() + " Z: " + (int)location.getZ()));
    }

    private Location tryValidate(int x, int z, RTPWorld rtpWorld, World world) {
        int y = world.getHighestBlockYAt(x, z);
        Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
        Material groundBlock = world.getType(x, y, z);
        return isTeleportLocationValid(location, rtpWorld, groundBlock) ? location : null;
    }

    private boolean isTeleportLocationValid(Location location, RTPWorld rtpWorld, Material groundBlock) {
        if(rtpWorld.blacklistedBlocks.contains(groundBlock)) return false;
        Config config = Main.getInstance().config();
        if(config.isTownyPresent) {
            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);
            if(townBlock != null) return townBlock.isOutpost() ? config.rtpInOutposts : config.rtpInTowns;
        }
        if(config.isGriefPreventionPresent) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            if(claim != null) return config.rtpInTowns;
        }
        return true;
    }
}
