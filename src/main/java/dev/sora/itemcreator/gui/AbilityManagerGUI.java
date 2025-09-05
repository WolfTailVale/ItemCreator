package dev.sora.itemcreator.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.sora.itemcreator.core.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

/**
 * GUI for managing item abilities
 */
public class AbilityManagerGUI implements Listener {
    private final ItemFactory itemFactory;
    private final Map<UUID, AbilityEditSession> activeSessions = new HashMap<>();
    
    public AbilityManagerGUI(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }
    
    /**
     * Open the ability management GUI for a player
     */
    public void openAbilityManager(Player player, String itemId) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("Ability Manager - " + itemId, NamedTextColor.DARK_PURPLE));
        
        // Create session
        AbilityEditSession session = new AbilityEditSession(itemId);
        activeSessions.put(player.getUniqueId(), session);
        
        setupAbilityManagerGUI(gui, session);
        player.openInventory(gui);
    }
    
    private void setupAbilityManagerGUI(Inventory gui, AbilityEditSession session) {
        // Title bar
        fillRow(gui, 0, Material.PURPLE_STAINED_GLASS_PANE, " ");
        
        // Ability type selection
        gui.setItem(10, createAbilityTypeItem(Material.TNT, "FlashBang", 
            "Creates a blinding flash", "flashbang"));
        gui.setItem(11, createAbilityTypeItem(Material.GOLDEN_APPLE, "Heal", 
            "Restores player health", "heal"));
        gui.setItem(12, createAbilityTypeItem(Material.ENDER_PEARL, "Teleport", 
            "Teleports player forward", "teleport"));
        
        // Current abilities display
        gui.setItem(20, createInfoItem(Material.BOOK, "Current Abilities", 
            session.abilities.isEmpty() ? 
                Arrays.asList("§7No abilities configured") :
                getAbilitiesDescription(session.abilities)));
        
        // Configuration area
        gui.setItem(30, createConfigItem(Material.REDSTONE, "Range", "10.0"));
        gui.setItem(31, createConfigItem(Material.CLOCK, "Duration", "5"));
        gui.setItem(32, createConfigItem(Material.BARRIER, "Cooldown", "3"));
        
        // Control buttons
        gui.setItem(40, createControlItem(Material.LIME_CONCRETE, "Add Ability", 
            "Click to add the configured ability"));
        gui.setItem(41, createControlItem(Material.RED_CONCRETE, "Clear All", 
            "Remove all abilities"));
        gui.setItem(42, createControlItem(Material.EMERALD, "Save & Exit", 
            "Save abilities and close"));
        
        // Bottom bar
        fillRow(gui, 5, Material.PURPLE_STAINED_GLASS_PANE, " ");
        gui.setItem(49, createControlItem(Material.BARRIER, "Cancel", "Exit without saving"));
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        AbilityEditSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        String expectedTitle = "Ability Manager - " + session.itemId;
        if (!event.getView().title().equals(Component.text(expectedTitle, NamedTextColor.DARK_PURPLE))) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String action = getItemAction(clicked);
        if (action == null) return;
        
        switch (action) {
            case "flashbang":
            case "heal":
            case "teleport":
                session.selectedType = action;
                player.sendMessage(Component.text("Selected ability type: " + action, NamedTextColor.GREEN));
                break;
                
            case "add":
                if (session.selectedType == null) {
                    player.sendMessage(Component.text("Please select an ability type first!", NamedTextColor.RED));
                    return;
                }
                addAbilityToSession(session, player);
                setupAbilityManagerGUI(event.getInventory(), session);
                break;
                
            case "clear":
                session.abilities.clear();
                player.sendMessage(Component.text("Cleared all abilities", NamedTextColor.YELLOW));
                setupAbilityManagerGUI(event.getInventory(), session);
                break;
                
            case "save":
                saveAbilities(session, player);
                player.closeInventory();
                break;
                
            case "cancel":
                player.closeInventory();
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            activeSessions.remove(player.getUniqueId());
        }
    }
    
    private void addAbilityToSession(AbilityEditSession session, Player player) {
        Map<String, Object> config = new HashMap<>();
        
        // Set default values based on ability type
        switch (session.selectedType) {
            case "flashbang":
                config.put("type", "flashbang");
                config.put("range", 10.0);
                config.put("duration", 5);
                config.put("cooldown", 3);
                break;
            case "heal":
                config.put("type", "heal");
                config.put("heal", 6.0);
                config.put("cooldown", 10);
                break;
            case "teleport":
                config.put("type", "teleport");
                config.put("distance", 8.0);
                config.put("cooldown", 5);
                break;
        }
        
        session.abilities.add(config);
        player.sendMessage(Component.text("Added " + session.selectedType + " ability!", NamedTextColor.GREEN));
    }
    
    private void saveAbilities(AbilityEditSession session, Player player) {
        // TODO: Integrate with item configuration system
        // For now, just show the YAML that should be added
        
        StringBuilder yaml = new StringBuilder();
        yaml.append("abilities:\n");
        
        for (int i = 0; i < session.abilities.size(); i++) {
            Map<String, Object> ability = session.abilities.get(i);
            yaml.append("  ability").append(i + 1).append(":\n");
            for (Map.Entry<String, Object> entry : ability.entrySet()) {
                yaml.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        player.sendMessage(Component.text("Add this to your items.yml:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text(yaml.toString(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Then use /itemcreator reload", NamedTextColor.GREEN));
    }
    
    // Helper methods
    private ItemStack createAbilityTypeItem(Material material, String name, String description, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW));
        meta.lore(Arrays.asList(
            Component.text(description, NamedTextColor.GRAY),
            Component.text("Click to select", NamedTextColor.GREEN)
        ));
        meta.getPersistentDataContainer().set(
            new NamespacedKey(itemFactory.getPlugin(), "action"), 
            org.bukkit.persistence.PersistentDataType.STRING, 
            action
        );
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createInfoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.AQUA));
        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(Component.text(line, NamedTextColor.GRAY));
        }
        meta.lore(loreComponents);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createConfigItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name + ": " + value, NamedTextColor.WHITE));
        meta.lore(Arrays.asList(
            Component.text("Current value: " + value, NamedTextColor.GRAY),
            Component.text("Right-click to edit", NamedTextColor.GREEN)
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createControlItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.WHITE));
        meta.lore(Arrays.asList(Component.text(description, NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(
            new NamespacedKey(itemFactory.getPlugin(), "action"), 
            org.bukkit.persistence.PersistentDataType.STRING, 
            name.toLowerCase().replace(" ", "").replace("&", "")
        );
        item.setItemMeta(meta);
        return item;
    }
    
    private void fillRow(Inventory inv, int row, Material material, String name) {
        for (int i = row * 9; i < (row + 1) * 9; i++) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(name));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
    }
    
    private String getItemAction(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(
            new NamespacedKey(itemFactory.getPlugin(), "action"), 
            org.bukkit.persistence.PersistentDataType.STRING
        );
    }
    
    private List<String> getAbilitiesDescription(List<Map<String, Object>> abilities) {
        List<String> desc = new ArrayList<>();
        for (int i = 0; i < abilities.size(); i++) {
            Map<String, Object> ability = abilities.get(i);
            desc.add("§e" + (i + 1) + ". " + ability.get("type"));
        }
        return desc;
    }
    
    // Session class to track editing state
    private static class AbilityEditSession {
        final String itemId;
        String selectedType;
        final List<Map<String, Object>> abilities = new ArrayList<>();
        
        AbilityEditSession(String itemId) {
            this.itemId = itemId;
        }
    }
}
