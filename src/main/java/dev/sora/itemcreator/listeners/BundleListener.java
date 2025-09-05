package dev.sora.itemcreator.listeners;

import dev.sora.itemcreator.core.CustomItemRegistry;
import dev.sora.itemcreator.core.RecipeRegistrar;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BundleListener implements Listener {
    private final CustomItemRegistry registry;
    private final RecipeRegistrar recipes;

    public BundleListener(CustomItemRegistry registry, RecipeRegistrar recipes) {
        this.registry = registry;
        this.recipes = recipes;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = e.getItem();
        if (item == null) return;

        registry.fromStack(item).ifPresent(ci -> {
            String id = ci.getId();
            // If the item id starts with box_of_, try to unbox on right-click
            switch (e.getAction()) {
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (id.startsWith("box_of_")) {
                    recipes.getBundleByBoxId(id).ifPresent(info -> {
                    Player p = e.getPlayer();
                    if (p.getGameMode() != GameMode.CREATIVE) {
                        item.setAmount(item.getAmount() - 1);
                    }
                        ItemStack unit = info.unit();
                        unit.setAmount(info.count());
                        p.getInventory().addItem(unit);
                    });
                    e.setCancelled(true);
                }
                }
                default -> {}
            }
        });
    }

    
}
