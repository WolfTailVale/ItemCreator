package dev.sora.itemcreator.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Teleport ability - teleports player forward in the direction they're looking
 */
public class TeleportAbility implements ItemAbility {

    private final double distance;
    private final long cooldown;

    public TeleportAbility(Map<String, Object> config) {
        // User-friendly configuration
        this.distance = ((Number) config.getOrDefault("distance", 5.0)).doubleValue(); // 5 blocks default
        this.cooldown = ((Number) config.getOrDefault("cooldown", 5)).longValue() * 1000; // 5 seconds default
    }

    @Override
    public String getType() {
        return "teleport";
    }

    @Override
    public void execute(Player player, ItemStack item, Location location, Event triggerEvent) {
        // Calculate teleport destination
        Location destination = player.getLocation().add(player.getLocation().getDirection().multiply(distance));

        // Find safe landing spot (check for solid blocks below)
        destination = findSafeLanding(destination);

        // Teleport effects at start location
        player.getWorld().spawnParticle(Particle.PORTAL,
                player.getLocation().add(0, 1, 0),
                50, 0.5, 1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Teleport player
        player.teleport(destination);

        // Teleport effects at destination
        player.getWorld().spawnParticle(Particle.PORTAL,
                destination.add(0, 1, 0),
                50, 0.5, 1, 0.5, 0.1);
        player.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);

        // Send feedback
        player.sendMessage("§d✦ Teleported " + String.format("%.1f", distance) + " blocks!");
    }

    @Override
    public boolean canTrigger(Class<? extends Event> eventClass) {
        return eventClass == org.bukkit.event.player.PlayerInteractEvent.class;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    /**
     * Find a safe landing spot by adjusting Y coordinate
     */
    private Location findSafeLanding(Location loc) {
        Location safe = loc.clone();

        // Check if destination is safe (not in wall, has ground below)
        for (int y = 0; y < 10; y++) {
            Location check = safe.clone().add(0, -y, 0);
            if (check.getBlock().getType().isSolid() &&
                    !check.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
                    !check.clone().add(0, 2, 0).getBlock().getType().isSolid()) {
                return check.add(0, 1, 0); // Stand on top of solid block
            }
        }

        return safe; // Return original if no safe spot found
    }
}
