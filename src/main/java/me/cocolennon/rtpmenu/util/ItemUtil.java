package me.cocolennon.rtpmenu.util;

import dev.lone.itemsadder.api.CustomStack;
import me.cocolennon.rtpmenu.Main;
import me.cocolennon.rtpmenu.objects.RTPWorld;
import net.kyori.adventure.text.format.TextDecoration;
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
        if(!world.itemName.startsWith("itemsadder-")) itemMeta.displayName(miniMessage.deserialize(world.displayName).decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getPreviousPageItem(String locale, int pageNumber, String itemName) {
        ItemStack itemStack = getBaseStack(itemName);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(buttonAction, PersistentDataType.STRING, String.valueOf(pageNumber));
        if(!itemName.startsWith("itemsadder-")) itemMeta.displayName(Localization.get(locale, "menu.previous-page", false).decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getNextPageItem(String locale, int pageNumber, String itemName) {
        ItemStack itemStack = getBaseStack(itemName);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(buttonAction, PersistentDataType.STRING, String.valueOf(pageNumber));
        if(!itemName.startsWith("itemsadder-")) itemMeta.displayName(Localization.get(locale, "menu.next-page", false).decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack getBaseStack(String itemName) {
        if(itemName.startsWith("itemsadder-")) {
            CustomStack customStack = CustomStack.getInstance(itemName.replace("itemsadder-", ""));
            if(customStack == null) {
                main.getLogger().warning(Localization.console("item-does-not-exist", itemName));
                return new ItemStack(Material.BARRIER);
            }
            return customStack.getItemStack();
        }else{
            Material material = Material.matchMaterial(itemName);
            if(material == null) {
                main.getLogger().warning(Localization.console("item-does-not-exist", itemName));
                return new ItemStack(Material.BARRIER);
            }
            return new ItemStack(material);
        }
    }
}
