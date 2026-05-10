package me.cocolennon.rtpmenu.misc;

import dev.lone.itemsadder.api.CustomStack;
import me.cocolennon.rtpmenu.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtil {
    private final static Main main = Main.getInstance();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    public static NamespacedKey buttonAction = new NamespacedKey("rtpmenu", "button_action");

    public static ItemStack getWorldItem(RTPWorld world) {
        ItemStack itemStack = getBaseStack(world.itemName);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(buttonAction, PersistentDataType.STRING, world.worldName);
        if(!world.itemName.startsWith("itemsadder-")) itemMeta.displayName(miniMessage.deserialize(world.displayName));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getPreviousPageItem(int pageNumber, String itemName) {
        ItemStack itemStack = getBaseStack(itemName);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(buttonAction, PersistentDataType.STRING, String.valueOf(pageNumber));
        if(!itemName.startsWith("itemsadder-")) itemMeta.displayName(miniMessage.deserialize("<#45CC4B>Previous Page"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getNextPageItem(int pageNumber, String itemName) {
        ItemStack itemStack = getBaseStack(itemName);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(buttonAction, PersistentDataType.STRING, String.valueOf(pageNumber));
        if(!itemName.startsWith("itemsadder-")) itemMeta.displayName(miniMessage.deserialize("<#45CC4B>Next Page"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack getBaseStack(String itemName) {
        if(itemName.startsWith("itemsadder-")) {
            CustomStack customStack = CustomStack.getInstance(itemName.replace("itemsadder-", ""));
            if(customStack == null) {
                main.getLogger().warning("Item " + itemName + " doesn't exist! Replacing with generic invisible barrier block.");
                return new ItemStack(Material.BARRIER);
            }
            return customStack.getItemStack();
        }else{
            Material material = Material.matchMaterial(itemName);
            if(material == null) {
                main.getLogger().warning("Item " + itemName + " doesn't exist! Replacing with generic invisible barrier block.");
                return new ItemStack(Material.BARRIER);
            }
            return new ItemStack(material);
        }
    }
}
