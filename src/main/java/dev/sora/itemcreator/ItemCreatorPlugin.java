package dev.sora.itemcreator;

import dev.sora.itemcreator.commands.CreateRecipeCommand;
import dev.sora.itemcreator.commands.GiveItemCommand;
import dev.sora.itemcreator.core.CustomItemRegistry;
import dev.sora.itemcreator.core.ItemFactory;
import dev.sora.itemcreator.core.RecipeRegistrar;
import dev.sora.itemcreator.listeners.BundleListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ItemCreatorPlugin extends JavaPlugin {
    private CustomItemRegistry registry;
    private ItemFactory itemFactory;
    private RecipeRegistrar recipeRegistrar;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("items.yml");

        this.registry = new CustomItemRegistry(this);
        this.itemFactory = new ItemFactory(this, registry);
        this.recipeRegistrar = new RecipeRegistrar(this, registry, itemFactory);

        // Load items and recipes from items.yml
        File itemsFile = new File(getDataFolder(), "items.yml");
        FileConfiguration itemsCfg = YamlConfiguration.loadConfiguration(itemsFile);
        registry.loadFromConfig(itemsCfg);
        recipeRegistrar.registerAll(itemsCfg);

        // Commands and listeners
        getCommand("giveitem").setExecutor(new GiveItemCommand(itemFactory));
        getCommand("giveitem").setTabCompleter(new dev.sora.itemcreator.commands.GiveItemTabCompleter(registry));
        getCommand("createrecipe").setExecutor(new CreateRecipeCommand(itemFactory, registry));
        getServer().getPluginManager().registerEvents(new BundleListener(registry, recipeRegistrar), this);
    }

    private void saveResourceIfMissing(String name) {
        File target = new File(getDataFolder(), name);
        if (target.exists()) return;
        saveResource(name, false);
    }

    public CustomItemRegistry getRegistry() { return registry; }
    public ItemFactory getItemFactory() { return itemFactory; }
}
