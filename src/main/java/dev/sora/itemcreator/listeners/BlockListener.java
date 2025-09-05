package dev.sora.itemcreator.listeners;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import dev.sora.itemcreator.core.CustomItemRegistry;
import dev.sora.itemcreator.core.ItemFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles block placement and breaking for custom items to maintain their
 * identity
 */
public class BlockListener implements Listener {
    private final Plugin plugin;
    private final CustomItemRegistry registry;
    private final ItemFactory itemFactory;
    private final NamespacedKey customItemKey;
    
    // Fallback storage for non-tile entities (regular blocks like concrete)
    private final Map<String, String> customBlocksMap = new HashMap<>();

    public BlockListener(Plugin plugin, CustomItemRegistry registry, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.registry = registry;
        this.itemFactory = itemFactory;
        this.customItemKey = new NamespacedKey(plugin, "custom_item_id");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack placedItem = event.getItemInHand();

        // Check if this is a custom item
        String customItemId = getCustomItemId(placedItem);
        if (customItemId != null) {
            Block block = event.getBlock();
            String locationKey = locationToKey(block.getLocation());

            // Try to store the custom item ID in the block's persistent data (for tile entities)
            if (block.getState() instanceof TileState tileState) {
                PersistentDataContainer pdc = tileState.getPersistentDataContainer();
                pdc.set(customItemKey, PersistentDataType.STRING, customItemId);
                tileState.update();

                plugin.getLogger().info("Stored custom item ID '" + customItemId + "' in tile entity at " +
                        locationToString(block.getLocation()));
            } else {
                // For non-tile blocks (like concrete), use fallback storage
                customBlocksMap.put(locationKey, customItemId);
                plugin.getLogger().info("Stored custom item ID '" + customItemId + "' in fallback storage for block at " +
                        locationToString(block.getLocation()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Block block = event.getBlock();
        final String customItemId;
        String locationKey = locationToKey(block.getLocation());

        // Check if this block has custom item data stored (try tile entity first, then fallback)
        if (block.getState() instanceof TileState tileState) {
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            customItemId = pdc.get(customItemKey, PersistentDataType.STRING);
        } else {
            // Check fallback storage for regular blocks
            customItemId = customBlocksMap.get(locationKey);
        }

        if (customItemId != null) {
            // Cancel default drops
            event.setDropItems(false);

            // Drop the custom item instead
            registry.get(customItemId).ifPresentOrElse(
                    customItem -> {
                        ItemStack customItemStack = customItem.toItemStack(itemFactory);
                        block.getWorld().dropItemNaturally(block.getLocation(), customItemStack);
                        plugin.getLogger().info("Dropped custom item '" + customItemId + "' from block break at " +
                                locationToString(block.getLocation()));
                    },
                    () -> {
                        plugin.getLogger().warning(
                                "Custom item '" + customItemId + "' not found in registry, dropping vanilla item");
                    });
            
            // Remove from fallback storage
            customBlocksMap.remove(locationKey);
        }
    }

    /**
     * Gets the custom item ID from an ItemStack's persistent data
     */
    private String getCustomItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "cid");

        return pdc.get(key, PersistentDataType.STRING);
    }

    /**
     * Converts location to string for logging
     */
    private String locationToString(Location loc) {
        return String.format("(%d, %d, %d) in %s", 
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 
            loc.getWorld().getName());
    }
    
    /**
     * Converts location to a unique string key for storage
     */
    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
    
    /**
     * Get the number of tracked custom blocks (for debugging)
     */
    public int getTrackedBlockCount() {
        return customBlocksMap.size();
    }
    
    /**
     * Clear all tracked blocks (for reload)
     */
    public void clearTrackedBlocks() {
        customBlocksMap.clear();
    }
}