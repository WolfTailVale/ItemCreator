package dev.sora.itemcreator.gui;

import dev.sora.itemcreator.core.ItemFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeCreator {
    
    public static RecipeCreationResult createRecipe(
            RecipeCreatorGUI gui, 
            String recipeId, 
            String customName, 
            List<String> loreLines, 
            Integer customModelData,
            Boolean isPlaceable,
            ItemFactory factory) {
        
        try {
            // Create the custom item for output
            String outputItemId = recipeId + "_output";
            Material baseMaterial = gui.getOutputItem().getType();
            
            // Override material based on placeable setting
            if (isPlaceable != null && isPlaceable) {
                // Force to a block material if user wants it placeable
                baseMaterial = ensureBlockMaterial(baseMaterial);
            } else if (isPlaceable != null && !isPlaceable) {
                // Force to a non-block material if user wants it non-placeable
                baseMaterial = ensureItemMaterial(baseMaterial);
            }
            
            // Save the custom item to items.yml
            RecipeCreationResult itemResult = saveCustomItem(
                outputItemId, baseMaterial, customName, loreLines, customModelData, isPlaceable, factory
            );
            
            if (!itemResult.isSuccess()) {
                return itemResult;
            }
            
            // Reload the registry to include the new item
            reloadItemRegistry(factory);
            
            // Create and register the recipe
            return createAndRegisterRecipe(gui, recipeId, outputItemId, factory);
            
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
            File itemsFile = new File(factory.getPlugin().getDataFolder(), "items.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
            
            // Create items section if it doesn't exist
            if (!config.contains("items")) {
                config.createSection("items");
            }
            
            ConfigurationSection itemSection = config.createSection("items." + itemId);
            itemSection.set("material", material.name());
            
            if (customName != null && !customName.isEmpty()) {
                itemSection.set("name", customName);
            }
            
            if (!loreLines.isEmpty()) {
                itemSection.set("lore", loreLines);
            }
            
            if (customModelData != null) {
                itemSection.set("custom-model-data", customModelData);
            }
            
            if (isPlaceable != null) {
                itemSection.set("placeable", isPlaceable);
            }
            
            config.save(itemsFile);
            return RecipeCreationResult.success();
            
        } catch (IOException e) {
            return RecipeCreationResult.failure("Failed to save custom item: " + e.getMessage());
        }
    }
    
    private static void reloadItemRegistry(ItemFactory factory) {
        try {
            File itemsFile = new File(factory.getPlugin().getDataFolder(), "items.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
            
            // Get the registry and reload it
            // We'll need to add a reload method to CustomItemRegistry
            if (factory.getPlugin() instanceof dev.sora.itemcreator.ItemCreatorPlugin plugin) {
                plugin.getRegistry().loadFromConfig(config);
            }
        } catch (Exception e) {
            // Log error but don't fail completely
            factory.getPlugin().getLogger().warning("Failed to reload item registry: " + e.getMessage());
        }
    }
    
    private static RecipeCreationResult createAndRegisterRecipe(
            RecipeCreatorGUI gui, 
            String recipeId, 
            String outputItemId, 
            ItemFactory factory) {
        
        try {
            // Create the output item
            ItemStack result = factory.create(outputItemId);
            if (result == null) {
                return RecipeCreationResult.failure("Failed to create output item");
            }
            
            NamespacedKey key = new NamespacedKey(factory.getPlugin(), "custom_" + recipeId);
            
            if (gui.isShapedRecipe()) {
                return createShapedRecipe(gui, key, result, factory);
            } else {
                return createShapelessRecipe(gui, key, result, factory);
            }
            
        } catch (Exception e) {
            return RecipeCreationResult.failure("Failed to register recipe: " + e.getMessage());
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
            default -> Material.PAPER; // Fallback to paper
        };
    }
}
