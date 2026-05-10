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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class InventoryClickListener implements Listener {
    private final String RTP_MENU = "<#75FF7A>[<#45CC4B>RTP Menu<#75FF7A>] ";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof RTPInventoryHolder)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if(clicked == null || !clicked.hasItemMeta()) return;
        PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(ItemUtil.buttonAction)) return;
        String buttonAction = pdc.get(ItemUtil.buttonAction, PersistentDataType.STRING);
        Main main = Main.getInstance();
        if(StringUtils.isNumeric(buttonAction)) player.openInventory(main.config().pages.get(Integer.parseInt(buttonAction)).getInventory());
        else {
            MiniMessage miniMessage = MiniMessage.miniMessage();
            RTPWorld rtpWorld = main.config().getWorld(buttonAction);
            World world = main.getServer().getWorld(rtpWorld == null ? "RTPMenuWorldDoesNotExist" : rtpWorld.worldName);
            if(rtpWorld == null || world == null) {
                player.sendMessage(miniMessage.deserialize(RTP_MENU + "<#FF4545>Something went wrong when teleporting to this world."));
                main.getLogger().warning("World " + buttonAction + " doesn't exist!");
                return;
            }
            generateRandomCoordinates(rtpWorld, world, location -> {
                player.teleport(location);
                player.sendMessage(miniMessage.deserialize(RTP_MENU + "<#26F525>Teleported to X: " + (int)location.getX() + " Y: " + (int)location.getY() + " Z: " + (int)location.getZ()));
            });
        }
    }

    private void generateRandomCoordinates(RTPWorld rtpWorld, World world, Consumer<Location> callback) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Location location = null;
            while (location == null) {
                int x = random.nextInt(-rtpWorld.maxX, rtpWorld.maxX + 1);
                int z = random.nextInt(-rtpWorld.maxZ, rtpWorld.maxZ + 1);
                location = tryValidate(x, z, rtpWorld, world);
            }
            Location finalLocation = location;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(finalLocation));
        });
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
