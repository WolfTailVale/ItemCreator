package dev.sora.itemcreator.abilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Manages item abilities - registration, cooldowns, and execution
 */
public class AbilityManager {

    private final Map<String, ItemAbility> registeredAbilities = new HashMap<>();
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    public AbilityManager() {
        // Register built-in abilities
        registerAbility("flashbang", config -> new FlashBangAbility(config));
    }

    /**
     * Register a new ability type
     */
    public void registerAbility(String type, AbilityFactory factory) {
        // We'll store the factory for creating abilities with different configs
        // For now, we'll create with empty config as default
        registeredAbilities.put(type, factory.create(new HashMap<>()));
    }

    /**
     * Create an ability instance with specific configuration
     */
    public ItemAbility createAbility(String type, Map<String, Object> config) {
        if ("flashbang".equals(type)) {
            return new FlashBangAbility(config);
        } else if ("heal".equals(type)) {
            return new HealAbility(config);
        } else if ("teleport".equals(type)) {
            return new TeleportAbility(config);
        }
        return null;
    }

    /**
     * Execute abilities for an item
     */
    public void executeAbilities(Player player, ItemStack item, Location location, Event triggerEvent,
            List<ItemAbility> abilities) {
        for (ItemAbility ability : abilities) {
            // Check if ability can trigger on this event type
            if (!ability.canTrigger(triggerEvent.getClass())) {
                continue;
            }

            // Check cooldown
            if (isOnCooldown(player, ability)) {
                continue;
            }

            // Execute the ability
            try {
                ability.execute(player, item, location, triggerEvent);

                // Set cooldown
                setCooldown(player, ability);

            } catch (Exception e) {
                player.sendMessage("§cAbility execution failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if player is on cooldown for this ability
     */
    private boolean isOnCooldown(Player player, ItemAbility ability) {
        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        if (cooldowns == null)
            return false;

        Long lastUsed = cooldowns.get(ability.getType());
        if (lastUsed == null)
            return false;

        return System.currentTimeMillis() - lastUsed < ability.getCooldown();
    }

    /**
     * Set cooldown for player and ability
     */
    private void setCooldown(Player player, ItemAbility ability) {
        if (ability.getCooldown() <= 0)
            return;

        playerCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(ability.getType(), System.currentTimeMillis());
    }

    /**
     * Get remaining cooldown time in milliseconds
     */
    public long getRemainingCooldown(Player player, ItemAbility ability) {
        if (!isOnCooldown(player, ability))
            return 0;

        Map<String, Long> cooldowns = playerCooldowns.get(player.getUniqueId());
        Long lastUsed = cooldowns.get(ability.getType());

        return ability.getCooldown() - (System.currentTimeMillis() - lastUsed);
    }

    /**
     * Clean up cooldowns for offline players
     */
    public void cleanup() {
        // This could be called periodically to clean up cooldowns for offline players
        // For now, we'll keep it simple and let them persist
    }

    /**
     * Factory interface for creating abilities
     */
    @FunctionalInterface
    public interface AbilityFactory {
        ItemAbility create(Map<String, Object> config);
    }
}
