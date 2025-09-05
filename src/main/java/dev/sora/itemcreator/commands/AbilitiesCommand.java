package dev.sora.itemcreator.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.sora.itemcreator.core.ItemFactory;
import dev.sora.itemcreator.gui.ModernAbilityManagerGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AbilitiesCommand implements CommandExecutor {
    private final ItemFactory itemFactory;
    private final ModernAbilityManagerGUI modernAbilityGUI;

    public AbilitiesCommand(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
        this.modernAbilityGUI = new ModernAbilityManagerGUI(itemFactory);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /abilities <item_id>", NamedTextColor.RED));
            player.sendMessage(Component.text("Example: /abilities crate_of_gunpowder", NamedTextColor.GRAY));
            return true;
        }

        String itemId = args[0];
        
        // Check if item exists in registry
        if (!itemFactory.getRegistry().get(itemId).isPresent()) {
            player.sendMessage(Component.text("Item '" + itemId + "' not found!", NamedTextColor.RED));
            player.sendMessage(Component.text("Use /giveitem to see available items", NamedTextColor.GRAY));
            return true;
        }

        // Open the modern ability manager GUI
        modernAbilityGUI.openAbilityManager(player, itemId);
        return true;
    }

    public ModernAbilityManagerGUI getModernAbilityGUI() {
        return modernAbilityGUI;
    }
}
