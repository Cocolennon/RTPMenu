package me.cocolennon.rtpmenu.misc;

import me.cocolennon.rtpmenu.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RTPInventoryHolder implements InventoryHolder {
    Inventory inventory;

    public RTPInventoryHolder(Main main) {
        this.inventory = main.getServer().createInventory(this, 27, MiniMessage.miniMessage().deserialize(main.config().menuTitle));
    }

    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
