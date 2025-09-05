package dev.sora.itemcreator.gui;

import dev.sora.itemcreator.core.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private InputState state = InputState.RECIPE_ID;
    
    private enum InputState {
        RECIPE_ID,
        CUSTOM_NAME,
        LORE_LINES,
        MODEL_DATA,
        PLACEABLE,
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
        player.sendMessage(Component.text("Enter a unique ID for this recipe:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("(Use letters, numbers, and underscores only)", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Type 'cancel' to abort", NamedTextColor.RED));
    }
    
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (!event.getPlayer().equals(player)) return;
        
        event.setCancelled(true);
        
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(Component.text("Recipe creation cancelled.", NamedTextColor.RED));
            cleanup();
            return;
        }
        
        switch (state) {
            case RECIPE_ID -> handleRecipeId(message);
            case CUSTOM_NAME -> handleCustomName(message);
            case LORE_LINES -> handleLoreLine(message);
            case MODEL_DATA -> handleModelData(message);
            case PLACEABLE -> handlePlaceable(message);
            case COMPLETE -> { /* do nothing, shouldn't happen */ }
        }
    }
    
    private void handleRecipeId(String input) {
        if (!input.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(Component.text("Invalid ID! Use only letters, numbers, and underscores.", NamedTextColor.RED));
            return;
        }
        
        this.recipeId = input.toLowerCase();
        player.sendMessage(Component.text("Recipe ID: " + recipeId, NamedTextColor.GREEN));
        
        state = InputState.CUSTOM_NAME;
        player.sendMessage(Component.text("Enter a custom name for the output item:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("(Use & for color codes, or 'skip' to use default)", NamedTextColor.GRAY));
    }
    
    private void handleCustomName(String input) {
        if (input.equalsIgnoreCase("skip")) {
            customName = null;
            player.sendMessage(Component.text("Using default item name.", NamedTextColor.GREEN));
        } else {
            customName = input;
            player.sendMessage(Component.text("Custom name: " + input, NamedTextColor.GREEN));
        }
        
        state = InputState.LORE_LINES;
        player.sendMessage(Component.text("Enter lore lines (one per message):", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Type 'done' when finished, or 'skip' for no lore", NamedTextColor.GRAY));
    }
    
    private void handleLoreLine(String input) {
        if (input.equalsIgnoreCase("done") || input.equalsIgnoreCase("skip")) {
            if (loreLines.isEmpty()) {
                player.sendMessage(Component.text("No lore lines added.", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Added " + loreLines.size() + " lore lines.", NamedTextColor.GREEN));
            }
            
            state = InputState.MODEL_DATA;
            player.sendMessage(Component.text("Enter custom model data (number):", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Type 'skip' to use default", NamedTextColor.GRAY));
            return;
        }
        
        loreLines.add(input);
        player.sendMessage(Component.text("Added lore: " + input, NamedTextColor.GREEN));
        player.sendMessage(Component.text("Add another line, or type 'done':", NamedTextColor.YELLOW));
    }
    
    private void handleModelData(String input) {
        if (input.equalsIgnoreCase("skip")) {
            customModelData = null;
            player.sendMessage(Component.text("Using default model data.", NamedTextColor.GREEN));
        } else {
            try {
                customModelData = Integer.parseInt(input);
                player.sendMessage(Component.text("Custom model data: " + customModelData, NamedTextColor.GREEN));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid number! Enter a valid integer or 'skip'", NamedTextColor.RED));
                return;
            }
        }
        
        state = InputState.PLACEABLE;
        player.sendMessage(Component.text("Should this item be placeable as a block?", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Type 'yes' for placeable block, 'no' for item only", NamedTextColor.GRAY));
    }
    
    private void handlePlaceable(String input) {
        if (input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("true")) {
            isPlaceable = true;
            player.sendMessage(Component.text("Item will be placeable as a block.", NamedTextColor.GREEN));
        } else if (input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n") || input.equalsIgnoreCase("false")) {
            isPlaceable = false;
            player.sendMessage(Component.text("Item will be non-placeable (item only).", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Invalid input! Type 'yes' or 'no'", NamedTextColor.RED));
            return;
        }
        
        state = InputState.COMPLETE;
        completeRecipeCreation();
    }
    
    private void completeRecipeCreation() {
        // Create the recipe using the gathered information
        RecipeCreationResult result = RecipeCreator.createRecipe(
            gui, recipeId, customName, loreLines, customModelData, isPlaceable, factory
        );
        
        if (result.isSuccess()) {
            player.sendMessage(Component.text("Recipe created successfully!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Recipe ID: " + recipeId, NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Players can now craft this item!", NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("Failed to create recipe: " + result.getError(), NamedTextColor.RED));
        }
        
        cleanup();
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
