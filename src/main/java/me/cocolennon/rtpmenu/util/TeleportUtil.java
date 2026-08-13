package me.cocolennon.rtpmenu.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import me.cocolennon.rtpmenu.Config;
import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.objects.RTPWorld;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TeleportUtil {
    static Main main = Main.getInstance();
    static BukkitScheduler scheduler = main.getServer().getScheduler();

    record CooldownKey(UUID uuid, String worldName) {}

    static final Map<UUID, Location> pendingTeleports = new HashMap<>();
    static final Map<UUID, Integer> countdowns = new HashMap<>();
    static final Map<CooldownKey, Long> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(Player player, String worldName) {
        CooldownKey key = new CooldownKey(player.getUniqueId(), main.config().perWorldCooldowns ? worldName : "RTPMenu");
        Long expiresAt = cooldowns.get(key);
        if(expiresAt == null) return false;
        if(System.currentTimeMillis() < expiresAt) return true;
        cooldowns.remove(key);
        return false;
    }

    public static Long getRemainingCooldown(Player player, String worldName) {
        CooldownKey key = new CooldownKey(player.getUniqueId(), main.config().perWorldCooldowns ? worldName : "RTPMenu");
        Long expiresAt = cooldowns.get(key);
        if(expiresAt == null) return 0L;
        long remaining = expiresAt - System.currentTimeMillis();
        return remaining / 1000L;
    }

    private static void setCooldown(Player player, String worldName) {
        Config config = main.config();
        long cooldown = main.config().rtpCooldown;
        if(config.softDependencies.isLuckPermsPresent) {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
            if(group != null) {
                String cooldownMeta = group.getCachedData().getMetaData().getMetaValue("rtpmenu.cooldown");
                if(cooldownMeta != null) cooldown = Long.parseLong(cooldownMeta);
            }
        }
        CooldownKey key = new CooldownKey(player.getUniqueId(), config.perWorldCooldowns ? worldName : "RTPMenu");
        cooldowns.put(key, System.currentTimeMillis() + (cooldown * 1000L));
    }

    public static boolean checkCooldown(Player player, RTPWorld rtpWorld) {
        if(TeleportUtil.isOnCooldown(player, rtpWorld.worldName)) {
            long remaining = TeleportUtil.getRemainingCooldown(player, rtpWorld.worldName);
            long hours = remaining / 3600;
            long minutes = (remaining % 3600) / 60;
            long seconds = remaining % 60;
            Component localizedMessage;
            if(hours > 1 || (hours == 1 && (minutes > 0))) localizedMessage = Localization.get(player, "error.cooldown.hours", true, hours, minutes);
            else if(minutes > 0 || hours == 1) localizedMessage = Localization.get(player, "error.cooldown.minutes", true, (hours * 60) + minutes, seconds);
            else localizedMessage = Localization.get(player, "error.cooldown.seconds", true, seconds);
            player.sendMessage(localizedMessage);
            return true;
        }
        return false;
    }

    public static void startTeleport(Player player, RTPWorld rtpWorld, World world) {
        UUID uuid = player.getUniqueId();
        generateRandomCoordinates(rtpWorld, world, location -> pendingTeleports.put(uuid, location));
        int countdownLength = Main.getInstance().config().rtpCountdown;
        countdowns.put(uuid, countdownLength);
        final AtomicInteger seconds = new AtomicInteger(countdownLength);
        scheduler.runTaskTimer(main, countdownTask -> {
            if(!player.isOnline()) {
                pendingTeleports.remove(uuid);
                countdowns.remove(uuid);
                countdownTask.cancel();
            }
            int currentSeconds = seconds.get();
            if(seconds.getAndDecrement() > 0) {
                player.sendActionBar(Localization.get(player, "teleport.countdown", false, currentSeconds));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f, 2f);
                return;
            }
            countdowns.remove(uuid);
            Location location = pendingTeleports.remove(uuid);
            if(location != null && !countdowns.containsKey(uuid)) teleportPlayer(player, location, rtpWorld.worldName);
            countdownTask.cancel();
        }, 0L, 20L);
    }

    private static void teleportPlayer(Player player, Location location, String worldName) {
        player.teleport(location);
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 2f);
        player.sendMessage(Localization.get(player, "teleport.teleported", true, (int) location.getX(), (int) location.getY(), (int) location.getZ()));
        setCooldown(player, worldName);
        MetricsUtil.rtpCounter.incrementAndGet();
    }

    private static void generateRandomCoordinates(RTPWorld rtpWorld, World world, Consumer<Location> callback) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        scheduler.runTaskAsynchronously(Main.getInstance(), () -> {
            Location location = null;
            int coordinatesGenerated = 0;
            while (location == null) {
                int x = random.nextInt(-rtpWorld.maxX, rtpWorld.maxX + 1);
                int z = random.nextInt(-rtpWorld.maxZ, rtpWorld.maxZ + 1);
                int y = world.getEnvironment() == World.Environment.NETHER ? getNetherY(world, x, z) : world.getHighestBlockYAt(x, z);
                Material groundBlock = world.getType(x, y, z);
                Location tempLocation = new Location(world, x, y, z);
                location = isTeleportLocationValid(tempLocation, rtpWorld, groundBlock) ? new Location(world, x + 0.5, y + 1, z + 0.5) : null;
                if(coordinatesGenerated >= 10) location = world.getSpawnLocation();
                coordinatesGenerated++;
            }
            Location finalLocation = location;
            scheduler.runTask(Main.getInstance(), () -> callback.accept(finalLocation));
        });
    }

    private static int getNetherY(World world, int x, int z) {
        for(int y = 120; y > world.getMinHeight(); y--) {
            Material ground = world.getType(x, y, z);
            Material feet = world.getType(x, y + 1, z);
            Material head = world.getType(x, y + 2, z);
            if(ground.isAir()) continue;
            if(!feet.isAir() || !head.isAir()) continue;
            return y;
        }
        return world.getMinHeight();
    }

    private static boolean isTeleportLocationValid(Location location, RTPWorld rtpWorld, Material groundBlock) {
        if(rtpWorld.blacklistedBlocks.contains(groundBlock)) return false;
        if(location.getY() <= location.getWorld().getMinHeight()) return false;
        Config config = Main.getInstance().config();
        if(config.softDependencies.isTownyPresent) {
            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);
            if(townBlock != null) return townBlock.isOutpost() ? config.rtpInOutposts : config.rtpInTowns;
        }
        if(config.softDependencies.isGriefPreventionPresent) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
            if(claim != null) return config.rtpInTowns;
        }
        return true;
    }
}
