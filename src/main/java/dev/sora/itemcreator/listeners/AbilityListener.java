package dev.sora.itemcreator.listeners;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.sora.itemcreator.abilities.AbilityManager;
import dev.sora.itemcreator.abilities.ItemAbility;
import dev.sora.itemcreator.core.CustomItem;
import dev.sora.itemcreator.core.CustomItemRegistry;

/**
 * Handles ability triggers for custom items
 */
public class AbilityListener implements Listener {

    private final CustomItemRegistry registry;
    private final AbilityManager abilityManager;

    public AbilityListener(CustomItemRegistry registry, AbilityManager abilityManager) {
        this.registry = registry;
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR)
            return;

        // Check if this is a custom item with abilities
        Optional<CustomItem> customItem = registry.fromStack(item);
        if (customItem.isEmpty())
            return;

        List<ItemAbility> abilities = customItem.get().getAbilities();
        if (abilities.isEmpty())
            return;

        // Check for flint and steel interaction (special trigger for flash-bang)
        if (isFlintAndSteelInteraction(event)) {
            // Execute abilities
            abilityManager.executeAbilities(
                    player,
                    item,
                    getInteractionLocation(event),
                    event,
                    abilities);

            // Cancel the original interaction to prevent normal flint and steel behavior
            event.setCancelled(true);
        }
        // Add more trigger types here (right-click, left-click, etc.)
        else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Right-click abilities - cancel default behavior to prevent placement/eating
            event.setCancelled(true);

            // Play activation sound
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

            // Right-click abilities
            abilityManager.executeAbilities(
                    player,
                    item,
                    getInteractionLocation(event),
                    event,
                    abilities);
        }
    }

    private boolean isFlintAndSteelInteraction(PlayerInteractEvent event) {
        // Check if player is holding flint and steel in off-hand while interacting with
        // the item
        ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();
        return offHandItem.getType() == Material.FLINT_AND_STEEL &&
                (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR);
    }

    private org.bukkit.Location getInteractionLocation(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            return event.getClickedBlock().getLocation().add(0.5, 1, 0.5); // Center of block, slightly above
        } else {
            return event.getPlayer().getLocation(); // Player's location if no block
        }
    }
}
