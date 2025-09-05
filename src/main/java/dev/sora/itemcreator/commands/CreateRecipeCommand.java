package dev.sora.itemcreator.commands;

import dev.sora.itemcreator.core.CustomItemRegistry;
import dev.sora.itemcreator.core.ItemFactory;
import dev.sora.itemcreator.gui.RecipeCreatorGUI;
import dev.sora.itemcreator.gui.RecipeMetadataInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateRecipeCommand implements CommandExecutor {
    private final ItemFactory factory;
    private final CustomItemRegistry registry;

    public CreateRecipeCommand(ItemFactory factory, CustomItemRegistry registry) {
        this.factory = factory;
        this.registry = registry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("itemcreator.createrecipe")) {
            player.sendMessage(Component.text("You don't have permission to create recipes.", NamedTextColor.RED));
            return true;
        }

        // Clean up any existing chat input session
        RecipeMetadataInput.cleanupPlayer(player);

        // Open the recipe creator GUI
        RecipeCreatorGUI gui = new RecipeCreatorGUI(player, registry, factory);
        factory.getPlugin().getServer().getPluginManager().registerEvents(gui, factory.getPlugin());
        player.openInventory(gui.getInventory());

        player.sendMessage(Component.text("Recipe Creator opened!", NamedTextColor.GREEN));
        player.sendMessage(Component.text("Arrange items in the crafting grid, set recipe type, and configure output.", NamedTextColor.YELLOW));

        return true;
    }
}
