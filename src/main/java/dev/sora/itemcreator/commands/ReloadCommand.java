package dev.sora.itemcreator.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.sora.itemcreator.ItemCreatorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReloadCommand implements CommandExecutor {
    private final ItemCreatorPlugin plugin;

    public ReloadCommand(ItemCreatorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if reload subcommand was used
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Component.text("Usage: /itemcreator reload", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Reloading ItemCreator configuration...", NamedTextColor.YELLOW));

        try {
            // Reload main config
            plugin.reloadConfig();

            // Reload items.yml and re-register everything
            File itemsFile = new File(plugin.getDataFolder(), "items.yml");
            FileConfiguration itemsCfg = YamlConfiguration.loadConfiguration(itemsFile);

            // Clear and reload the registry
            plugin.getRegistry().loadFromConfig(itemsCfg);

            // Re-register all recipes
            plugin.getRecipeRegistrar().unregisterAll();
            plugin.getRecipeRegistrar().registerAll(itemsCfg);
            
            // Clear tracked custom blocks (they'll need to be re-placed)
            plugin.getBlockListener().clearTrackedBlocks();
            
            sender.sendMessage(Component.text("✓ Configuration reloaded successfully!", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("✓ Items: " + plugin.getRegistry().ids().size(), NamedTextColor.GREEN));
            sender.sendMessage(Component.text("✓ Recipes re-registered", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("✓ Abilities system refreshed", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("✓ Custom block tracking cleared", NamedTextColor.GREEN));            plugin.getLogger().info("Configuration reloaded by " + sender.getName());

        } catch (Exception e) {
            sender.sendMessage(
                    Component.text("✗ Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().warning("Failed to reload configuration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
