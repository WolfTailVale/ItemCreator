package dev.sora.itemcreator.abilities;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Flash-bang ability - creates a bright flash that blinds players looking at it
 */
public class FlashBangAbility implements ItemAbility {

    private final double range;
    private final int blindnessDuration; // in ticks
    private final long cooldown; // in milliseconds

    public FlashBangAbility(Map<String, Object> config) {
        // User-friendly configuration with smart defaults
        this.range = ((Number) config.getOrDefault("range", 10.0)).doubleValue();

        // Accept both "blindness-duration" and "duration" in seconds (convert to ticks)
        Object durationObj = config.getOrDefault("duration", config.getOrDefault("blindness-duration", 5));
        this.blindnessDuration = ((Number) durationObj).intValue() * 20; // Convert seconds to ticks

        // Accept cooldown in seconds (convert to milliseconds)
        Object cooldownObj = config.getOrDefault("cooldown", 3);
        this.cooldown = ((Number) cooldownObj).longValue() * 1000; // Convert seconds to milliseconds
    }

    @Override
    public String getType() {
        return "flashbang";
    }

    @Override
    public void execute(Player player, ItemStack item, Location location, Event triggerEvent) {
        // Create flash effect at the location
        createFlashEffect(location);

        // Find players in range and check if they're looking at the flash
        for (Player nearbyPlayer : location.getWorld().getPlayers()) {
            if (nearbyPlayer.getLocation().distance(location) <= range) {
                if (isPlayerLookingAt(nearbyPlayer, location)) {
                    // Apply blindness effect
                    nearbyPlayer.addPotionEffect(new PotionEffect(
                            PotionEffectType.BLINDNESS,
                            blindnessDuration,
                            0,
                            false,
                            true,
                            true));

                    // Add nausea for extra disorientation
                    nearbyPlayer.addPotionEffect(new PotionEffect(
                            PotionEffectType.NAUSEA,
                            blindnessDuration / 2,
                            0,
                            false,
                            true,
                            true));

                    nearbyPlayer.sendMessage("§c§lFLASH! You are temporarily blinded!");
                }
            }
        }

        // Remove the item (it's consumed)
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }
    }

    @Override
    public boolean canTrigger(Class<? extends Event> eventClass) {
        return PlayerInteractEvent.class.isAssignableFrom(eventClass);
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    private void createFlashEffect(Location location) {
        // Create bright white particle explosion
        location.getWorld().spawnParticle(
                Particle.EXPLOSION,
                location,
                3,
                0.1, 0.1, 0.1,
                0.1);

        // Create white firework particles for flash effect
        location.getWorld().spawnParticle(
                Particle.FIREWORK,
                location,
                50,
                2.0, 2.0, 2.0,
                0.1);

        // Create white colored particles
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.WHITE, 2.0f);
        location.getWorld().spawnParticle(
                Particle.DUST,
                location,
                100,
                3.0, 3.0, 3.0,
                0.1,
                dustOptions);

        // Play sound effects
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.8f);

        // Schedule a delayed bright flash particle burst
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("ItemCreator"),
                () -> {
                    location.getWorld().spawnParticle(
                            Particle.FLASH,
                            location,
                            1);
                },
                2L);
    }

    private boolean isPlayerLookingAt(Player player, Location target) {
        Vector playerDirection = player.getEyeLocation().getDirection().normalize();
        Vector toTarget = target.toVector().subtract(player.getEyeLocation().toVector()).normalize();

        // Calculate the angle between player's view direction and target
        double dot = playerDirection.dot(toTarget);
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));

        // Consider player "looking at" if within 60 degrees (π/3 radians)
        return angle < Math.PI / 3;
    }
}
