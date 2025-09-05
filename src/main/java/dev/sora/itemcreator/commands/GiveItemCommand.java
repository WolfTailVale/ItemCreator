package dev.sora.itemcreator.commands;

import dev.sora.itemcreator.core.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveItemCommand implements CommandExecutor {
    private final ItemFactory factory;

    public GiveItemCommand(ItemFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("itemcreator.give")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /" + label + " <id> [player]", NamedTextColor.YELLOW));
            return true;
        }
        String id = args[0];
        ItemStack stack = factory.create(id);
        if (stack == null) {
            sender.sendMessage(Component.text("Unknown item id: " + id, NamedTextColor.RED));
            return true;
        }
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found", NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(Component.text("Console must specify a player", NamedTextColor.RED));
            return true;
        }
        target.getInventory().addItem(stack);
        sender.sendMessage(Component.text("Gave 1x " + id + " to " + target.getName(), NamedTextColor.GREEN));
        return true;
    }
}
