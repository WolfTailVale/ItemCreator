package dev.sora.itemcreator.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import dev.sora.itemcreator.ItemCreatorPlugin;
import dev.sora.itemcreator.core.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RecipeManagerGUI implements InventoryHolder, Listener {
    private final Player player;
    private final ItemFactory factory;
    private final Inventory inventory;
    private final List<Recipe> allRecipes = new ArrayList<>();
    private int currentPage = 0;
    private static final int RECIPES_PER_PAGE = 28; // 7x4 grid

    // Navigation slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int CLOSE_SLOT = 49;
    private static final int CREATE_NEW_SLOT = 4;

    public RecipeManagerGUI(Player player, ItemFactory factory) {
        this.player = player;
        this.factory = factory;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Recipe Manager", NamedTextColor.DARK_PURPLE));
        loadRecipes();
        setupGUI();
    }

    private void loadRecipes() {
        allRecipes.clear();
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();

            // Get the key based on recipe type
            NamespacedKey key = null;
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                key = shapedRecipe.getKey();
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                key = shapelessRecipe.getKey();
            } else {
                continue; // Skip other recipe types we don't support
            }

            // Only show recipes from our plugin
            if (key != null && key.getNamespace().equals(factory.getPlugin().getName().toLowerCase())) {
                allRecipes.add(recipe);
            }
        }
    }

    private void setupGUI() {
        // Clear inventory
        inventory.clear();

        // Fill background with gray glass
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.displayName(Component.text(" "));
        background.setItemMeta(backgroundMeta);

        // Fill bottom row and edges
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, background);
            }
        }

        // Create new recipe button
        ItemStack createButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.displayName(Component.text("Create New Recipe", NamedTextColor.GREEN));
        createMeta.lore(List.of(Component.text("Click to open recipe creator", NamedTextColor.GRAY)));
        createButton.setItemMeta(createMeta);
        inventory.setItem(CREATE_NEW_SLOT, createButton);

        // Navigation buttons
        setupNavigationButtons();

        // Display recipes for current page
        displayRecipePage();
    }

    private void setupNavigationButtons() {
        int totalPages = (int) Math.ceil((double) allRecipes.size() / RECIPES_PER_PAGE);

        // Previous page button
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page", NamedTextColor.YELLOW));
            prevMeta.lore(List
                    .of(Component.text("Page " + currentPage + "/" + Math.max(1, totalPages), NamedTextColor.GRAY)));
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(PREV_PAGE_SLOT, prevButton);
        }

        // Next page button
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", NamedTextColor.YELLOW));
            nextMeta.lore(List.of(Component.text("Page " + (currentPage + 2) + "/" + totalPages, NamedTextColor.GRAY)));
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(NEXT_PAGE_SLOT, nextButton);
        }

        // Close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED));
        closeMeta.lore(List.of(
                Component.text("Total recipes: " + allRecipes.size(), NamedTextColor.GRAY),
                Component.text("Page " + (currentPage + 1) + "/" + Math.max(1, totalPages), NamedTextColor.GRAY)));
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(CLOSE_SLOT, closeButton);
    }

    private void displayRecipePage() {
        // Clear recipe display area (slots 10-44, excluding edges)
        for (int i = 10; i <= 43; i++) {
            if (i % 9 != 0 && i % 9 != 8) {
                inventory.setItem(i, null);
            }
        }

        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, allRecipes.size());

        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            Recipe recipe = allRecipes.get(i);
            ItemStack recipeDisplay = createRecipeDisplayItem(recipe);

            // Skip edge slots
            while (slot % 9 == 0 || slot % 9 == 8 || slot >= 45) {
                slot++;
            }

            inventory.setItem(slot, recipeDisplay);
            slot++;
        }
    }

    private ItemStack createRecipeDisplayItem(Recipe recipe) {
        ItemStack result = recipe.getResult().clone();
        ItemMeta meta = result.getItemMeta();

        List<Component> lore = new ArrayList<>();

        // Get recipe key based on type
        String recipeKey = "Unknown";
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            recipeKey = shapedRecipe.getKey().getKey();
            lore.add(Component.text("Type: Shaped Recipe", NamedTextColor.YELLOW));
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            recipeKey = shapelessRecipe.getKey().getKey();
            lore.add(Component.text("Type: Shapeless Recipe", NamedTextColor.YELLOW));
        }

        lore.add(Component.text("Recipe Key: " + recipeKey, NamedTextColor.GRAY));
        lore.add(Component.text(""));
        lore.add(Component.text("Left-click to view details", NamedTextColor.GREEN));
        lore.add(Component.text("Right-click to delete", NamedTextColor.RED));

        meta.lore(lore);
        result.setItemMeta(meta);

        return result;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this)
            return;
        if (!(event.getWhoClicked() instanceof Player clicker))
            return;
        if (!clicker.equals(player))
            return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        // Handle navigation
        if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            setupGUI();
            return;
        }

        if (slot == NEXT_PAGE_SLOT) {
            int totalPages = (int) Math.ceil((double) allRecipes.size() / RECIPES_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                setupGUI();
            }
            return;
        }

        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        if (slot == CREATE_NEW_SLOT) {
            player.closeInventory();
            // Open recipe creator
            ItemCreatorPlugin plugin = (ItemCreatorPlugin) factory.getPlugin();
            RecipeCreatorGUI gui = new RecipeCreatorGUI(player, plugin.getRegistry(), factory);
            factory.getPlugin().getServer().getPluginManager().registerEvents(gui, factory.getPlugin());
            player.openInventory(gui.getInventory());
            return;
        }

        // Handle recipe interaction
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR
                && clicked.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            int recipeIndex = getRecipeIndexFromSlot(slot);
            if (recipeIndex >= 0 && recipeIndex < allRecipes.size()) {
                Recipe recipe = allRecipes.get(recipeIndex);

                if (event.isLeftClick()) {
                    showRecipeDetails(recipe);
                } else if (event.isRightClick()) {
                    deleteRecipe(recipe);
                }
            }
        }
    }

    private int getRecipeIndexFromSlot(int slot) {
        // Convert GUI slot to recipe index
        int displaySlots = 0;
        for (int i = 10; i <= 43; i++) {
            if (i % 9 != 0 && i % 9 != 8) {
                if (i == slot) {
                    return currentPage * RECIPES_PER_PAGE + displaySlots;
                }
                displaySlots++;
            }
        }
        return -1;
    }

    private void showRecipeDetails(Recipe recipe) {
        player.sendMessage(Component.text("=== Recipe Details ===", NamedTextColor.GOLD));

        // Get recipe key based on type
        String recipeKey = "Unknown";
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            recipeKey = shapedRecipe.getKey().getKey();
            player.sendMessage(Component.text("Type: Shaped Recipe", NamedTextColor.AQUA));
            String[] shape = shapedRecipe.getShape();
            player.sendMessage(Component.text("Pattern:", NamedTextColor.YELLOW));
            for (String row : shape) {
                player.sendMessage(Component.text("  " + row, NamedTextColor.WHITE));
            }
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            recipeKey = shapelessRecipe.getKey().getKey();
            player.sendMessage(Component.text("Type: Shapeless Recipe", NamedTextColor.AQUA));
        }

        player.sendMessage(Component.text("Key: " + recipeKey, NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Result: " + recipe.getResult().getAmount() + "x " +
                recipe.getResult().getType().name(), NamedTextColor.GREEN));
    }

    private void deleteRecipe(Recipe recipe) {
        // Get key based on recipe type
        NamespacedKey key = null;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            key = shapedRecipe.getKey();
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            key = shapelessRecipe.getKey();
        }

        if (key == null) {
            player.sendMessage(Component.text("Cannot delete this recipe type", NamedTextColor.RED));
            return;
        }

        boolean removed = factory.getPlugin().getServer().removeRecipe(key);

        if (removed) {
            player.sendMessage(Component.text("Recipe deleted: " + key.getKey(), NamedTextColor.GREEN));
            // Reload and refresh display
            loadRecipes();
            // Adjust page if needed
            int totalPages = (int) Math.ceil((double) allRecipes.size() / RECIPES_PER_PAGE);
            if (currentPage >= totalPages && totalPages > 0) {
                currentPage = totalPages - 1;
            }
            setupGUI();
        } else {
            player.sendMessage(Component.text("Failed to delete recipe: " + key.getKey(), NamedTextColor.RED));
        }
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
