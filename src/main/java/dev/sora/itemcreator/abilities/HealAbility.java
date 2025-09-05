package dev.sora.itemcreator.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Heal ability - restores player health
 */
public class HealAbility implements ItemAbility {

    private final double healAmount;
    private final long cooldown;

    public HealAbility(Map<String, Object> config) {
        // User-friendly configuration
        this.healAmount = ((Number) config.getOrDefault("heal", 4.0)).doubleValue(); // 2 hearts default
        this.cooldown = ((Number) config.getOrDefault("cooldown", 10)).longValue() * 1000; // 10 seconds default
    }

    @Override
    public String getType() {
        return "heal";
    }

    @Override
    public void execute(Player player, ItemStack item, Location location, Event triggerEvent) {
        // Calculate new health (don't exceed max health)
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = Math.min(currentHealth + healAmount, maxHealth);

        // Apply healing
        player.setHealth(newHealth);

        // Visual and audio effects
        player.getWorld().spawnParticle(Particle.HEART,
                player.getLocation().add(0, 1, 0),
                10, 0.5, 0.5, 0.5, 0.1);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        // Send feedback to player
        double heartsHealed = healAmount / 2;
        player.sendMessage("§a✚ Healed " + heartsHealed + " hearts!");
    }

    @Override
    public boolean canTrigger(Class<? extends Event> eventClass) {
        // Respond to player interaction events
        return eventClass == org.bukkit.event.player.PlayerInteractEvent.class;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }
}
