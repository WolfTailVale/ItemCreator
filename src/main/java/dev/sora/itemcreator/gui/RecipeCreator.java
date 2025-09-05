package dev.sora.itemcreator.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import dev.sora.itemcreator.core.ItemFactory;

public class RecipeCreator {

    public static RecipeCreationResult createRecipe(
            RecipeCreatorGUI gui,
            String recipeId,
            String customName,
            List<String> loreLines,
            Integer customModelData,
            Boolean isPlaceable,
            Boolean useVanillaItem,
            Integer outputAmount,
            ItemFactory factory) {

        try {
            if (useVanillaItem != null && useVanillaItem) {
                // Use vanilla item - no custom item creation needed
                return createAndRegisterVanillaRecipe(gui, recipeId, outputAmount, factory);
            } else {
                // Create a custom item
                String outputItemId = recipeId + "_output";
                Material baseMaterial = gui.getOutputItem().getType();

                factory.getPlugin().getLogger().info("=== MATERIAL CONVERSION DEBUG ===");
                factory.getPlugin().getLogger().info("Original base material: " + baseMaterial.name());
                factory.getPlugin().getLogger().info("Is placeable setting: " + isPlaceable);

                // Override material based on placeable setting
                if (isPlaceable != null && isPlaceable) {
                    // Force to a block material if user wants it placeable
                    Material oldMaterial = baseMaterial;
                    baseMaterial = ensureBlockMaterial(baseMaterial);
                    factory.getPlugin().getLogger()
                            .info("Converted to block material: " + oldMaterial.name() + " -> " + baseMaterial.name());
                } else if (isPlaceable != null && !isPlaceable) {
                    // Force to a non-block material if user wants it non-placeable
                    Material oldMaterial = baseMaterial;
                    baseMaterial = ensureItemMaterial(baseMaterial);
                    factory.getPlugin().getLogger()
                            .info("Converted to item material: " + oldMaterial.name() + " -> " + baseMaterial.name());
                } else {
                    factory.getPlugin().getLogger().info("No material conversion applied (isPlaceable is null)");
                }

                factory.getPlugin().getLogger().info("Final material: " + baseMaterial.name());
                factory.getPlugin().getLogger().info("=== END MATERIAL CONVERSION DEBUG ===");

                // Save the custom item to items.yml
                RecipeCreationResult itemResult = saveCustomItem(
                        outputItemId, baseMaterial, customName, loreLines, customModelData, isPlaceable, factory);

                if (!itemResult.isSuccess()) {
                    return itemResult;
                }

                // Reload the registry to include the new item
                reloadItemRegistry(factory);

                // Create and register the recipe
                return createAndRegisterCustomRecipe(gui, recipeId, outputItemId, outputAmount, factory);
            }

        } catch (Exception e) {
            return RecipeCreationResult.failure("Error creating recipe: " + e.getMessage());
        }
    }

    private static RecipeCreationResult saveCustomItem(
            String itemId,
            Material material,
            String customName,
            List<String> loreLines,
            Integer customModelData,
            Boolean isPlaceable,
            ItemFactory factory) {

        try {
            factory.getPlugin().getLogger().info("=== CUSTOM ITEM CREATION DEBUG ===");
            factory.getPlugin().getLogger().info("Item ID: " + itemId);
            factory.getPlugin().getLogger().info("Material: " + material.name());
            factory.getPlugin().getLogger().info("Custom Name: " + customName);
            factory.getPlugin().getLogger().info("Lore Lines: " + loreLines);
            factory.getPlugin().getLogger().info("Custom Model Data: " + customModelData);
            factory.getPlugin().getLogger().info("Is Placeable: " + isPlaceable);

            File itemsFile = new File(factory.getPlugin().getDataFolder(), "items.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);

            // Create items section if it doesn't exist
            if (!config.contains("items")) {
                config.createSection("items");
                factory.getPlugin().getLogger().info("Created new 'items' section in config");
            }

            ConfigurationSection itemSection = config.createSection("items." + itemId);
            itemSection.set("material", material.name());
            factory.getPlugin().getLogger().info("Set material in config: " + material.name());

            if (customName != null && !customName.isEmpty()) {
                itemSection.set("name", customName);
                factory.getPlugin().getLogger().info("Set custom name in config: " + customName);
            }

            if (!loreLines.isEmpty()) {
                itemSection.set("lore", loreLines);
                factory.getPlugin().getLogger().info("Set lore in config: " + loreLines);
            }

            if (customModelData != null) {
                itemSection.set("custom-model-data", customModelData);
                factory.getPlugin().getLogger().info("Set custom model data in config: " + customModelData);
            }

            if (isPlaceable != null) {
                itemSection.set("placeable", isPlaceable);
                factory.getPlugin().getLogger().info("Set placeable in config: " + isPlaceable);
            }

            config.save(itemsFile);
            factory.getPlugin().getLogger().info("Saved items.yml file successfully");
            factory.getPlugin().getLogger().info("=== END CUSTOM ITEM CREATION DEBUG ===");
            return RecipeCreationResult.success();

        } catch (IOException e) {
            factory.getPlugin().getLogger().severe("Failed to save custom item: " + e.getMessage());
            e.printStackTrace();
            return RecipeCreationResult.failure("Failed to save custom item: " + e.getMessage());
        }
    }

    private static void reloadItemRegistry(ItemFactory factory) {
        try {
            factory.getPlugin().getLogger().info("=== REGISTRY RELOAD DEBUG ===");
            File itemsFile = new File(factory.getPlugin().getDataFolder(), "items.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);

            // Get the registry and reload it
            // We'll need to add a reload method to CustomItemRegistry
            if (factory.getPlugin() instanceof dev.sora.itemcreator.ItemCreatorPlugin plugin) {
                factory.getPlugin().getLogger().info("Reloading item registry from config...");
                plugin.getRegistry().loadFromConfig(config);
                factory.getPlugin().getLogger()
                        .info("Registry reload completed. Available items: " + plugin.getRegistry().ids());
            }
            factory.getPlugin().getLogger().info("=== END REGISTRY RELOAD DEBUG ===");
        } catch (Exception e) {
            // Log error but don't fail completely
            factory.getPlugin().getLogger().warning("Failed to reload item registry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static RecipeCreationResult createAndRegisterCustomRecipe(
            RecipeCreatorGUI gui,
            String recipeId,
            String outputItemId,
            Integer outputAmount,
            ItemFactory factory) {

        try {
            factory.getPlugin().getLogger().info("=== CUSTOM RECIPE CREATION DEBUG ===");
            factory.getPlugin().getLogger().info("Recipe ID: " + recipeId);
            factory.getPlugin().getLogger().info("Output Item ID: " + outputItemId);
            factory.getPlugin().getLogger().info("Output Amount: " + outputAmount);

            // Create the output item
            ItemStack result = factory.create(outputItemId);
            if (result == null) {
                factory.getPlugin().getLogger().severe("Failed to create output item with ID: " + outputItemId);
                return RecipeCreationResult.failure("Failed to create output item");
            }

            factory.getPlugin().getLogger().info("Created result item: " + result.getType().name() +
                    " (Amount: " + result.getAmount() + ")");
            if (result.hasItemMeta() && result.getItemMeta().hasCustomModelData()) {
                factory.getPlugin().getLogger().info("Result item custom model data: " +
                        result.getItemMeta().getCustomModelData());
            }
            if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                factory.getPlugin().getLogger().info("Result item display name: " +
                        result.getItemMeta().displayName());
            }

            // Set the output amount
            if (outputAmount != null && outputAmount > 0) {
                result.setAmount(outputAmount);
                factory.getPlugin().getLogger().info("Set result amount to: " + outputAmount);
            }

            NamespacedKey key = new NamespacedKey(factory.getPlugin(), "custom_" + recipeId);
            factory.getPlugin().getLogger().info("Recipe key: " + key.toString());

            if (gui.isShapedRecipe()) {
                factory.getPlugin().getLogger().info("Creating shaped recipe...");
                return createShapedRecipe(gui, key, result, factory);
            } else {
                factory.getPlugin().getLogger().info("Creating shapeless recipe...");
                return createShapelessRecipe(gui, key, result, factory);
            }

        } catch (Exception e) {
            return RecipeCreationResult.failure("Failed to register recipe: " + e.getMessage());
        }
    }

    private static RecipeCreationResult createAndRegisterVanillaRecipe(
            RecipeCreatorGUI gui,
            String recipeId,
            Integer outputAmount,
            ItemFactory factory) {

        try {
            // Use the vanilla item from the output slot
            ItemStack originalItem = gui.getOutputItem();
            if (originalItem == null) {
                return RecipeCreationResult.failure("No output item found");
            }

            // Create a completely vanilla version of the item (no custom metadata)
            ItemStack result = new ItemStack(originalItem.getType());

            // Set the output amount
            if (outputAmount != null && outputAmount > 0) {
                result.setAmount(outputAmount);
            } else {
                result.setAmount(1); // Default to 1 if not specified
            }

            NamespacedKey key = new NamespacedKey(factory.getPlugin(), "vanilla_" + recipeId);

            if (gui.isShapedRecipe()) {
                return createShapedRecipe(gui, key, result, factory);
            } else {
                return createShapelessRecipe(gui, key, result, factory);
            }

        } catch (Exception e) {
            return RecipeCreationResult.failure("Failed to register vanilla recipe: " + e.getMessage());
        }
    }

    private static RecipeCreationResult createShapedRecipe(
            RecipeCreatorGUI gui,
            NamespacedKey key,
            ItemStack result,
            ItemFactory factory) {

        Map<Integer, ItemStack> grid = gui.getCraftingGrid();

        // Convert grid to 3x3 pattern
        String[] shape = new String[3];
        Map<Character, RecipeChoice> ingredients = new HashMap<>();
        char currentChar = 'A';
        Map<ItemStack, Character> itemToChar = new HashMap<>();

        for (int row = 0; row < 3; row++) {
            StringBuilder rowPattern = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                ItemStack item = grid.get(index);

                if (item == null || item.getType() == Material.AIR) {
                    rowPattern.append(' ');
                } else {
                    Character existing = findExistingChar(item, itemToChar);
                    if (existing != null) {
                        rowPattern.append(existing);
                    } else {
                        itemToChar.put(item.clone(), currentChar);
                        ingredients.put(currentChar, new RecipeChoice.ExactChoice(item));
                        rowPattern.append(currentChar);
                        currentChar++;
                    }
                }
            }
            shape[row] = rowPattern.toString();
        }

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape);

        for (Map.Entry<Character, RecipeChoice> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }

        factory.getPlugin().getServer().addRecipe(recipe);
        return RecipeCreationResult.success();
    }

    private static RecipeCreationResult createShapelessRecipe(
            RecipeCreatorGUI gui,
            NamespacedKey key,
            ItemStack result,
            ItemFactory factory) {

        Map<Integer, ItemStack> grid = gui.getCraftingGrid();
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);

        for (ItemStack item : grid.values()) {
            if (item != null && item.getType() != Material.AIR) {
                recipe.addIngredient(new RecipeChoice.ExactChoice(item));
            }
        }

        factory.getPlugin().getServer().addRecipe(recipe);
        return RecipeCreationResult.success();
    }

    private static Character findExistingChar(ItemStack item, Map<ItemStack, Character> itemToChar) {
        for (Map.Entry<ItemStack, Character> entry : itemToChar.entrySet()) {
            if (entry.getKey().isSimilar(item)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static Material ensureBlockMaterial(Material original) {
        // If already a block, keep it
        if (original.isBlock()) {
            return original;
        }

        // Map common items to reasonable block equivalents
        return switch (original) {
            case DIAMOND -> Material.DIAMOND_BLOCK;
            case EMERALD -> Material.EMERALD_BLOCK;
            case IRON_INGOT -> Material.IRON_BLOCK;
            case GOLD_INGOT -> Material.GOLD_BLOCK;
            case COPPER_INGOT -> Material.COPPER_BLOCK;
            case REDSTONE -> Material.REDSTONE_BLOCK;
            case COAL -> Material.COAL_BLOCK;
            case LAPIS_LAZULI -> Material.LAPIS_BLOCK;
            case STICK -> Material.OAK_PLANKS;
            case STRING -> Material.WHITE_WOOL;
            case LEATHER -> Material.BROWN_WOOL;
            case PAPER -> Material.WHITE_CONCRETE;
            default -> Material.STONE; // Fallback to stone
        };
    }

    private static Material ensureItemMaterial(Material original) {
        // If already an item (not placeable), keep it
        if (!original.isBlock()) {
            return original;
        }

        // Map common blocks to reasonable item equivalents
        return switch (original) {
            case DIAMOND_BLOCK -> Material.DIAMOND;
            case EMERALD_BLOCK -> Material.EMERALD;
            case IRON_BLOCK -> Material.IRON_INGOT;
            case GOLD_BLOCK -> Material.GOLD_INGOT;
            case COPPER_BLOCK -> Material.COPPER_INGOT;
            case REDSTONE_BLOCK -> Material.REDSTONE;
            case COAL_BLOCK -> Material.COAL;
            case LAPIS_BLOCK -> Material.LAPIS_LAZULI;
            case OAK_PLANKS, BIRCH_PLANKS, SPRUCE_PLANKS -> Material.STICK;
            case WHITE_WOOL, GRAY_WOOL, BLACK_WOOL -> Material.STRING;
            case STONE, COBBLESTONE -> Material.FLINT;
            // Keep concrete blocks as-is since they can represent items well
            case WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE,
                    YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE,
                    LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE,
                    BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE ->
                original;
            default -> Material.PAPER; // Fallback to paper
        };
    }
}
