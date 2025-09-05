package dev.sora.itemcreator.core;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public class ItemFactory {
    private final Plugin plugin;
    private final CustomItemRegistry registry;

    public ItemFactory(Plugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public Plugin getPlugin() { return plugin; }

    public ItemStack create(String id) {
        return registry.get(id).map(i -> i.toItemStack(this)).orElse(null);
    }

    public ShapedRecipe shapedRecipe(String key, ItemStack result, String... shape) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, key), result);
        recipe.shape(shape);
        return recipe;
    }

    public static RecipeChoice.ExactChoice exact(ItemStack stack) {
        return new RecipeChoice.ExactChoice(stack);
    }

    public static RecipeChoice.MaterialChoice mat(Material... materials) {
        return new RecipeChoice.MaterialChoice(materials);
    }
}
