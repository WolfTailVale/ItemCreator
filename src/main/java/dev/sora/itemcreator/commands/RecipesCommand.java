package dev.sora.itemcreator.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.sora.itemcreator.core.ItemFactory;
import dev.sora.itemcreator.gui.RecipeManagerGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RecipesCommand implements CommandExecutor {
    private final ItemFactory factory;

    public RecipesCommand(ItemFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("itemcreator.recipes")) {
            player.sendMessage(Component.text("You don't have permission to manage recipes!", NamedTextColor.RED));
            return true;
        }

        // Open the recipe manager GUI
        RecipeManagerGUI gui = new RecipeManagerGUI(player, factory);
        factory.getPlugin().getServer().getPluginManager().registerEvents(gui, factory.getPlugin());
        player.openInventory(gui.getInventory());

        return true;
    }
}
