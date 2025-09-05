package dev.sora.itemcreator.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

public class RecipeRegistrar {
    private final Plugin plugin;
    private final CustomItemRegistry registry;
    private final ItemFactory factory;
    private final Map<String, BundleInfo> bundlesByBoxId = new HashMap<>();
    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();

    public RecipeRegistrar(Plugin plugin, CustomItemRegistry registry, ItemFactory factory) {
        this.plugin = plugin;
        this.registry = registry;
        this.factory = factory;
    }

    public void registerAll(FileConfiguration cfg) {
        bundlesByBoxId.clear();
        ConfigurationSection bundles = cfg.getConfigurationSection("bundles");
        if (bundles != null) {
            for (String id : bundles.getKeys(false)) {
                registerBundle(id, bundles.getConfigurationSection(id));
            }
        }
    }

    public void unregisterAll() {
        // Remove all tracked recipes from the server
        for (NamespacedKey key : registeredRecipes) {
            plugin.getServer().removeRecipe(key);
        }
        registeredRecipes.clear();
        bundlesByBoxId.clear();
    }

    private void registerBundle(String id, ConfigurationSection sec) {
        if (sec == null)
            return;
        String itemId = sec.getString("item");
        int count = sec.getInt("count", 9);
        String boxId = sec.getString("box-id");

        ItemStack unit = parseItem(itemId);
        if (unit == null)
            return;

        ItemStack box = registry.get(boxId).map(i -> i.toItemStack(factory)).orElse(null);
        if (box == null)
            return;

        // Craft: 3x3 of unit -> box
        NamespacedKey bundleKey = new NamespacedKey(plugin, "bundle_" + id);
        ShapedRecipe shaped = new ShapedRecipe(bundleKey, box.clone());
        shaped.shape("AAA", "AAA", "AAA");
        shaped.setIngredient('A', new RecipeChoice.ExactChoice(unit));
        plugin.getServer().addRecipe(shaped);
        registeredRecipes.add(bundleKey);

        // Unpack: box -> count x unit
        ItemStack unpackResult = unit.clone();
        unpackResult.setAmount(count);
        NamespacedKey unbundleKey = new NamespacedKey(plugin, "unbundle_" + id);
        ShapelessRecipe shapeless = new ShapelessRecipe(unbundleKey, unpackResult);
        shapeless.addIngredient(new RecipeChoice.ExactChoice(box));
        plugin.getServer().addRecipe(shapeless);
        registeredRecipes.add(unbundleKey);

        // Keep for runtime lookups (unboxing on right-click)
        bundlesByBoxId.put(boxId, new BundleInfo(unit.clone(), count));
    }

    private ItemStack parseItem(String spec) {
        if (spec == null)
            return null;
        if (spec.startsWith("custom:")) {
            return factory.create(spec.substring("custom:".length()));
        }
        try {
            Material m = Material.matchMaterial(spec.toUpperCase());
            if (m != null)
                return new ItemStack(m);
        } catch (Exception ignored) {
        }
        return null;
    }

    public Optional<BundleInfo> getBundleByBoxId(String boxId) {
        return Optional.ofNullable(bundlesByBoxId.get(boxId));
    }

    public static class BundleInfo {
        private final ItemStack unit;
        private final int count;

        public BundleInfo(ItemStack unit, int count) {
            this.unit = unit;
            this.count = count;
        }

        public ItemStack unit() {
            return unit.clone();
        }

        public int count() {
            return count;
        }
    }
}
