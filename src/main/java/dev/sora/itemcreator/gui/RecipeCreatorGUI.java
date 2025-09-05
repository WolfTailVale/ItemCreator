package dev.sora.itemcreator.gui;

import dev.sora.itemcreator.core.CustomItemRegistry;
import dev.sora.itemcreator.core.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeCreatorGUI implements InventoryHolder, Listener {
    private final Player player;
    private final CustomItemRegistry registry;
    private final ItemFactory factory;
    private final Inventory inventory;
    private final Map<Integer, ItemStack> craftingGrid = new HashMap<>();
    private boolean isShapedRecipe = true;
    private ItemStack outputItem = null;
    private String outputId = null;
    
    // GUI slot mapping
    private static final int[] CRAFTING_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int OUTPUT_SLOT = 24;
    private static final int RECIPE_TYPE_SLOT = 4;
    private static final int SAVE_SLOT = 53;
    private static final int CANCEL_SLOT = 45;
    private static final int METADATA_SLOT = 49;

    public RecipeCreatorGUI(Player player, CustomItemRegistry registry, ItemFactory factory) {
        this.player = player;
        this.registry = registry;
        this.factory = factory;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Recipe Creator", NamedTextColor.DARK_PURPLE));
        setupGUI();
    }

    private void setupGUI() {
        // Fill background with gray glass
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.displayName(Component.text(" "));
        background.setItemMeta(backgroundMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, background);
        }
        
        // Clear crafting grid slots
        for (int slot : CRAFTING_SLOTS) {
            inventory.setItem(slot, null);
        }
        
        // Clear special slots
        inventory.setItem(OUTPUT_SLOT, null);
        
        // Recipe type toggle
        updateRecipeTypeButton();
        
        // Control buttons
        setupControlButtons();
        
        // Crafting grid borders
        setupCraftingGridBorders();
    }
    
    private void setupCraftingGridBorders() {
        ItemStack border = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text("Crafting Grid", NamedTextColor.YELLOW));
        border.setItemMeta(borderMeta);
        
        // Add borders around crafting grid
        int[] borderSlots = {9, 13, 18, 22, 27, 31};
        for (int slot : borderSlots) {
            inventory.setItem(slot, border);
        }
        
        // Output arrow
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.displayName(Component.text("Result", NamedTextColor.GREEN));
        arrowMeta.lore(List.of(Component.text("Place your output item here", NamedTextColor.GRAY)));
        arrow.setItemMeta(arrowMeta);
        inventory.setItem(23, arrow);
    }
    
    private void updateRecipeTypeButton() {
        ItemStack typeButton = new ItemStack(isShapedRecipe ? Material.CRAFTING_TABLE : Material.DROPPER);
        ItemMeta typeMeta = typeButton.getItemMeta();
        typeMeta.displayName(Component.text(
            isShapedRecipe ? "Shaped Recipe" : "Shapeless Recipe", 
            NamedTextColor.AQUA
        ));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Click to toggle", NamedTextColor.GRAY));
        if (isShapedRecipe) {
            lore.add(Component.text("Position matters", NamedTextColor.YELLOW));
        } else {
            lore.add(Component.text("Position doesn't matter", NamedTextColor.YELLOW));
        }
        typeMeta.lore(lore);
        typeButton.setItemMeta(typeMeta);
        inventory.setItem(RECIPE_TYPE_SLOT, typeButton);
    }
    
    private void setupControlButtons() {
        // Save button
        ItemStack saveButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.displayName(Component.text("Save Recipe", NamedTextColor.GREEN));
        saveMeta.lore(List.of(
            Component.text("Click to save this recipe", NamedTextColor.GRAY),
            Component.text("Output item must be set first", NamedTextColor.RED)
        ));
        saveButton.setItemMeta(saveMeta);
        inventory.setItem(SAVE_SLOT, saveButton);
        
        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel", NamedTextColor.RED));
        cancelMeta.lore(List.of(Component.text("Close without saving", NamedTextColor.GRAY)));
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(CANCEL_SLOT, cancelButton);
        
        // Metadata button
        ItemStack metadataButton = new ItemStack(Material.NAME_TAG);
        ItemMeta metadataMeta = metadataButton.getItemMeta();
        metadataMeta.displayName(Component.text("Set Output Metadata", NamedTextColor.LIGHT_PURPLE));
        metadataMeta.lore(List.of(
            Component.text("Click to set custom name/lore", NamedTextColor.GRAY),
            Component.text("Uses chat input", NamedTextColor.YELLOW)
        ));
        metadataButton.setItemMeta(metadataMeta);
        inventory.setItem(METADATA_SLOT, metadataButton);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(player)) return;
        
        int slot = event.getRawSlot();
        
        // Handle crafting grid - allow normal item interaction
        if (isCraftingSlot(slot)) {
            handleCraftingGridClick(event);
            return;
        }
        
        // Handle output slot - allow normal item interaction
        if (slot == OUTPUT_SLOT) {
            handleOutputSlotClick(event);
            return;
        }
        
        // Allow interaction with player inventory (slots 54+)
        if (slot >= 54) {
            return; // Don't cancel player inventory interactions
        }
        
        // Handle control buttons - cancel these interactions
        event.setCancelled(true);
        
        switch (slot) {
            case RECIPE_TYPE_SLOT -> {
                isShapedRecipe = !isShapedRecipe;
                updateRecipeTypeButton();
                player.sendMessage(Component.text(
                    "Recipe type: " + (isShapedRecipe ? "Shaped" : "Shapeless"), 
                    NamedTextColor.YELLOW
                ));
            }
            case SAVE_SLOT -> handleSaveRecipe();
            case CANCEL_SLOT -> player.closeInventory();
            case METADATA_SLOT -> handleMetadataEdit();
        }
    }
    
    private boolean isCraftingSlot(int slot) {
        for (int craftingSlot : CRAFTING_SLOTS) {
            if (slot == craftingSlot) return true;
        }
        return false;
    }
    
    private void handleCraftingGridClick(InventoryClickEvent event) {
        // Allow normal item placement/removal in crafting grid
        // Store the state for recipe creation
        Bukkit.getScheduler().runTaskLater(factory.getPlugin(), () -> {
            updateCraftingGrid();
        }, 1L);
    }
    
    private void handleOutputSlotClick(InventoryClickEvent event) {
        // Allow output item placement
        Bukkit.getScheduler().runTaskLater(factory.getPlugin(), () -> {
            outputItem = inventory.getItem(OUTPUT_SLOT);
            if (outputItem != null) {
                player.sendMessage(Component.text("Output item set!", NamedTextColor.GREEN));
            }
        }, 1L);
    }
    
    private void updateCraftingGrid() {
        craftingGrid.clear();
        for (int i = 0; i < CRAFTING_SLOTS.length; i++) {
            ItemStack item = inventory.getItem(CRAFTING_SLOTS[i]);
            if (item != null && item.getType() != Material.AIR) {
                craftingGrid.put(i, item.clone());
            }
        }
    }
    
    private void handleSaveRecipe() {
        if (outputItem == null || outputItem.getType() == Material.AIR) {
            player.sendMessage(Component.text("Please set an output item first!", NamedTextColor.RED));
            return;
        }
        
        if (craftingGrid.isEmpty()) {
            player.sendMessage(Component.text("Please add ingredients to the crafting grid!", NamedTextColor.RED));
            return;
        }
        
        // Start the metadata input process
        player.closeInventory();
        startMetadataInput();
    }
    
    private void handleMetadataEdit() {
        if (outputItem == null || outputItem.getType() == Material.AIR) {
            player.sendMessage(Component.text("Please set an output item first!", NamedTextColor.RED));
            return;
        }
        
        player.closeInventory();
        startMetadataInput();
    }
    
    private void startMetadataInput() {
        // This will be handled by a separate chat listener system
        RecipeMetadataInput.startInput(player, this, factory);
    }
    
    // Getters for the metadata input system
    public Map<Integer, ItemStack> getCraftingGrid() { return new HashMap<>(craftingGrid); }
    public boolean isShapedRecipe() { return isShapedRecipe; }
    public ItemStack getOutputItem() { return outputItem; }
    public void setOutputId(String id) { this.outputId = id; }
    public String getOutputId() { return outputId; }
    
    public void reopenGUI() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() == this) {
            // Unregister this listener when GUI closes
            event.getHandlers().unregister(this);
        }
    }
}
