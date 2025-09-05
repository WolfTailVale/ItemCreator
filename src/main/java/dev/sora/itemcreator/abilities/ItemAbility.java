package dev.sora.itemcreator.abilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Base interface for all item abilities
 */
public interface ItemAbility {

    /**
     * Get the unique identifier for this ability type
     */
    String getType();

    /**
     * Execute the ability
     * 
     * @param player       The player who triggered the ability
     * @param item         The item that has this ability
     * @param location     The location where the ability was triggered
     * @param triggerEvent The event that triggered this ability
     */
    void execute(Player player, ItemStack item, Location location, Event triggerEvent);

    /**
     * Check if this ability can be triggered by the given event type
     * 
     * @param eventClass The class of the event
     * @return true if this ability responds to this event type
     */
    boolean canTrigger(Class<? extends Event> eventClass);

    /**
     * Get the cooldown for this ability in milliseconds
     * 
     * @return cooldown in ms, or 0 for no cooldown
     */
    long getCooldown();
}
