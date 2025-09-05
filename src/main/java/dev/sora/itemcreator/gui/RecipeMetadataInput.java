package dev.sora.itemcreator.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.sora.itemcreator.core.ItemFactory;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class RecipeMetadataInput implements Listener {
    private static final Map<Player, RecipeMetadataInput> activeInputs = new HashMap<>();

    private final Player player;
    private final RecipeCreatorGUI gui;
    private final ItemFactory factory;
    private final List<String> loreLines = new ArrayList<>();
    private String customName = null;
    private Integer customModelData = null;
    private String recipeId = null;
    private Boolean isPlaceable = null;
    private Boolean useVanillaItem = null;
    private Integer outputAmount = 1;
    private InputState state = InputState.RECIPE_ID;

    private enum InputState {
        RECIPE_ID,
        OUTPUT_TYPE,
        OUTPUT_AMOUNT,
        CUSTOM_NAME,
        LORE_LINES,
        MODEL_DATA,
        COMPLETE
    }

    private RecipeMetadataInput(Player player, RecipeCreatorGUI gui, ItemFactory factory) {
        this.player = player;
        this.gui = gui;
        this.factory = factory;
    }

    public static void startInput(Player player, RecipeCreatorGUI gui, ItemFactory factory) {
        // Clean up any existing input session
        RecipeMetadataInput existing = activeInputs.get(player);
        if (existing != null) {
            existing.cleanup();
        }

        RecipeMetadataInput input = new RecipeMetadataInput(player, gui, factory);
        activeInputs.put(player, input);
        factory.getPlugin().getServer().getPluginManager().registerEvents(input, factory.getPlugin());

        input.startRecipeIdInput();
    }

    private void startRecipeIdInput() {
        player.sendMessage(Component.text("=== Recipe Creation ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Step 1/6: Recipe Identifier", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Enter a unique ID for this recipe (like 'magic_sword' or 'super_helmet'):",
                NamedTextColor.YELLOW));
        player.sendMessage(Component.text("• Use only letters, numbers, and underscores", NamedTextColor.GRAY));
        player.sendMessage(Component.text("• This ID will be used internally by the server", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Type 'cancel' to abort", NamedTextColor.RED));
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (!event.getPlayer().equals(player))
            return;

        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(Component.text("Recipe creation cancelled.", NamedTextColor.RED));
            cleanup();
            return;
        }

        switch (state) {
            case RECIPE_ID -> handleRecipeId(message);
            case OUTPUT_TYPE -> handleOutputType(message);
            case OUTPUT_AMOUNT -> handleOutputAmount(message);
            case CUSTOM_NAME -> handleCustomName(message);
            case LORE_LINES -> handleLoreLine(message);
            case MODEL_DATA -> handleModelData(message);
            case COMPLETE -> {
                /* do nothing, shouldn't happen */ }
        }
    }

    private void handleRecipeId(String input) {
        if (!input.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(
                    Component.text("Invalid ID! Use only letters, numbers, and underscores.", NamedTextColor.RED));
            return;
        }

        this.recipeId = input.toLowerCase();
        player.sendMessage(Component.text("✓ Recipe ID set: " + recipeId, NamedTextColor.GREEN));

        state = InputState.OUTPUT_TYPE;
        player.sendMessage(Component.text("Step 2/6: Output Item Type", NamedTextColor.AQUA));
        player.sendMessage(
                Component.text("Should the output be a custom item or vanilla item?", NamedTextColor.YELLOW));
        player.sendMessage(
                Component.text("• Type 'custom' to create a new custom item with metadata", NamedTextColor.GRAY));
        player.sendMessage(Component.text("• Type 'vanilla' to use the placed item as-is (like ARROW, DIAMOND, etc.)",
                NamedTextColor.GRAY));
    }

    private void handleOutputType(String input) {
        if (input.equalsIgnoreCase("custom")) {
            useVanillaItem = false;
            player.sendMessage(Component.text("✓ Will create a custom item with metadata", NamedTextColor.GREEN));
        } else if (input.equalsIgnoreCase("vanilla")) {
            useVanillaItem = true;
            player.sendMessage(Component.text("✓ Will use vanilla item as-is", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Please type 'custom' or 'vanilla'", NamedTextColor.RED));
            return;
        }

        state = InputState.OUTPUT_AMOUNT;
        player.sendMessage(Component.text("Step 3/6: Output Amount", NamedTextColor.AQUA));
        player.sendMessage(Component.text("How many items should this recipe produce?", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("• Type a number (like 1, 9, 16, 64)", NamedTextColor.GRAY));
        player.sendMessage(Component.text("• For reference: your output slot currently has " +
                (gui.getOutputItem() != null ? gui.getOutputItem().getAmount() : 0) + " items", NamedTextColor.GRAY));
    }

    private void handleOutputAmount(String input) {
        try {
            outputAmount = Integer.parseInt(input);
            if (outputAmount <= 0 || outputAmount > 64) {
                player.sendMessage(Component.text("Amount must be between 1 and 64!", NamedTextColor.RED));
                return;
            }
            player.sendMessage(Component.text("✓ Output amount set: " + outputAmount, NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid number! Enter a number between 1 and 64", NamedTextColor.RED));
            return;
        }

        // Skip custom item metadata if using vanilla item
        if (useVanillaItem) {
            state = InputState.COMPLETE;
            completeRecipeCreation();
        } else {
            state = InputState.CUSTOM_NAME;
            player.sendMessage(Component.text("Step 4/6: Display Name", NamedTextColor.AQUA));
            player.sendMessage(Component.text("Enter the display name players will see (like '&6Magic Sword'):",
                    NamedTextColor.YELLOW));
            player.sendMessage(
                    Component.text("• Use & for color codes: &a=green, &c=red, &6=gold, etc.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("• Type 'skip' to use the default item name", NamedTextColor.GRAY));
        }
    }

    private void handleCustomName(String input) {
        if (input.equalsIgnoreCase("skip")) {
            customName = null;
            player.sendMessage(Component.text("✓ Using default item name", NamedTextColor.GREEN));
        } else {
            customName = input;
            player.sendMessage(Component.text("✓ Display name set: " + input, NamedTextColor.GREEN));
        }

        state = InputState.LORE_LINES;
        player.sendMessage(Component.text("Step 5/6: Item Description (Lore)", NamedTextColor.AQUA));
        player.sendMessage(
                Component.text("Enter description lines that appear below the item name:", NamedTextColor.YELLOW));
        player.sendMessage(
                Component.text("• Type one line at a time (like '&7A powerful weapon')", NamedTextColor.GRAY));
        player.sendMessage(
                Component.text("• Type 'done' when finished, or 'skip' for no description", NamedTextColor.GRAY));
    }

    private void handleLoreLine(String input) {
        if (input.equalsIgnoreCase("done") || input.equalsIgnoreCase("skip")) {
            if (loreLines.isEmpty()) {
                player.sendMessage(Component.text("✓ No description lines added", NamedTextColor.GREEN));
            } else {
                player.sendMessage(
                        Component.text("✓ Added " + loreLines.size() + " description lines", NamedTextColor.GREEN));
            }

            state = InputState.MODEL_DATA;
            player.sendMessage(Component.text("Step 6/6: Custom Model (Resource Pack Support)", NamedTextColor.AQUA));
            player.sendMessage(Component.text("Enter a number for custom model data (for resource pack textures):",
                    NamedTextColor.YELLOW));
            player.sendMessage(Component.text("• Example: 1001, 2500, etc.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("• Type 'skip' if you don't have a custom texture", NamedTextColor.GRAY));
            return;
        }

        loreLines.add(input);
        player.sendMessage(Component.text("✓ Added line: " + input, NamedTextColor.GREEN));
        player.sendMessage(Component.text("Add another description line, or type 'done':", NamedTextColor.YELLOW));
    }

    private void handleModelData(String input) {
        if (input.equalsIgnoreCase("skip")) {
            customModelData = null;
            player.sendMessage(Component.text("✓ Using default model data", NamedTextColor.GREEN));
        } else {
            try {
                customModelData = Integer.parseInt(input);
                player.sendMessage(Component.text("✓ Custom model data set: " + customModelData, NamedTextColor.GREEN));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid number! Enter a valid integer (like 1001) or 'skip'",
                        NamedTextColor.RED));
                return;
            }
        }

        state = InputState.COMPLETE;
        completeRecipeCreation();
    }

    private void completeRecipeCreation() {
        // Schedule recipe creation on the main thread since AsyncChatEvent is async
        factory.getPlugin().getServer().getScheduler().runTask(factory.getPlugin(), () -> {
            factory.getPlugin().getLogger().info("=== RECIPE METADATA INPUT DEBUG ===");
            factory.getPlugin().getLogger().info("Recipe ID: " + recipeId);
            factory.getPlugin().getLogger().info("Custom Name: " + customName);
            factory.getPlugin().getLogger().info("Lore Lines: " + loreLines);
            factory.getPlugin().getLogger().info("Custom Model Data: " + customModelData);
            factory.getPlugin().getLogger().info("Use Vanilla Item: " + useVanillaItem);
            factory.getPlugin().getLogger().info("Output Amount: " + outputAmount);
            factory.getPlugin().getLogger().info("GUI Output Item: " +
                    (gui.getOutputItem() != null ? gui.getOutputItem().getType().name() : "null"));

            // Create the recipe using the gathered information
            // For vanilla items, we don't need placeable setting
            // For custom items, default to null (no material conversion) to preserve
            // original material
            Boolean placeableForRecipe = useVanillaItem ? null : null;
            factory.getPlugin().getLogger().info("Placeable setting: " + placeableForRecipe);
            factory.getPlugin().getLogger().info("=== END RECIPE METADATA INPUT DEBUG ===");

            RecipeCreationResult result = RecipeCreator.createRecipe(
                    gui, recipeId, customName, loreLines, customModelData, placeableForRecipe, useVanillaItem,
                    outputAmount, factory);

            if (result.isSuccess()) {
                player.sendMessage(Component.text("Recipe created successfully!", NamedTextColor.GREEN));
                player.sendMessage(Component.text("Recipe ID: " + recipeId, NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Players can now craft this item!", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("Failed to create recipe: " + result.getError(), NamedTextColor.RED));
            }

            cleanup();
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            cleanup();
        }
    }

    private void cleanup() {
        activeInputs.remove(player);
        HandlerList.unregisterAll(this);
    }

    public static void cleanupPlayer(Player player) {
        RecipeMetadataInput input = activeInputs.get(player);
        if (input != null) {
            input.cleanup();
        }
    }
}
