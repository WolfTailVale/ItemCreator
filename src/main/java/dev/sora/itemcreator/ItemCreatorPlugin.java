package dev.sora.itemcreator;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import dev.sora.itemcreator.abilities.AbilityManager;
import dev.sora.itemcreator.commands.AbilitiesCommand;
import dev.sora.itemcreator.commands.CreateRecipeCommand;
import dev.sora.itemcreator.commands.GiveItemCommand;
import dev.sora.itemcreator.commands.ReloadCommand;
import dev.sora.itemcreator.core.CustomItemRegistry;
import dev.sora.itemcreator.core.ItemFactory;
import dev.sora.itemcreator.core.RecipeRegistrar;
import dev.sora.itemcreator.listeners.AbilityListener;
import dev.sora.itemcreator.listeners.BlockListener;
import dev.sora.itemcreator.listeners.BundleListener;

public final class ItemCreatorPlugin extends JavaPlugin {
    private CustomItemRegistry registry;
    private ItemFactory itemFactory;
    private RecipeRegistrar recipeRegistrar;
    private AbilityManager abilityManager;
    private BlockListener blockListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("items.yml");

        this.registry = new CustomItemRegistry(this);
        this.itemFactory = new ItemFactory(this, registry);
        this.recipeRegistrar = new RecipeRegistrar(this, registry, itemFactory);
        this.abilityManager = new AbilityManager();
        this.blockListener = new BlockListener(this, registry, itemFactory);

        // Set up ability manager in registry
        registry.setAbilityManager(abilityManager);

        // Load items and recipes from items.yml
        File itemsFile = new File(getDataFolder(), "items.yml");
        FileConfiguration itemsCfg = YamlConfiguration.loadConfiguration(itemsFile);
        registry.loadFromConfig(itemsCfg);
        recipeRegistrar.registerAll(itemsCfg);

        // Commands and listeners
        getCommand("giveitem").setExecutor(new GiveItemCommand(itemFactory));
        getCommand("giveitem").setTabCompleter(new dev.sora.itemcreator.commands.GiveItemTabCompleter(registry));
        getCommand("createrecipe").setExecutor(new CreateRecipeCommand(itemFactory, registry));
        getCommand("recipes").setExecutor(new dev.sora.itemcreator.commands.RecipesCommand(itemFactory));
        getCommand("itemcreator").setExecutor(new ReloadCommand(this));
        
        // New abilities command
        AbilitiesCommand abilitiesCommand = new AbilitiesCommand(itemFactory);
        getCommand("abilities").setExecutor(abilitiesCommand);
        
        getServer().getPluginManager().registerEvents(new BundleListener(registry, recipeRegistrar), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(registry, abilityManager), this);
        getServer().getPluginManager().registerEvents(blockListener, this);
    }

    private void saveResourceIfMissing(String name) {
        File target = new File(getDataFolder(), name);
        if (target.exists())
            return;
        saveResource(name, false);
    }

    public CustomItemRegistry getRegistry() {
        return registry;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public RecipeRegistrar getRecipeRegistrar() {
        return recipeRegistrar;
    }

    public BlockListener getBlockListener() {
        return blockListener;
    }
}
