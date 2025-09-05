package dev.sora.itemcreator.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import dev.sora.itemcreator.core.ItemFactory;

import java.util.*;

/**
 * Modern GUI for managing item abilities using triumph-gui
 */
public class ModernAbilityManagerGUI {
    private final ItemFactory itemFactory;
    
    public ModernAbilityManagerGUI(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }
    
    /**
     * Open the ability management GUI for a player
     */
    public void openAbilityManager(Player player, String itemId) {
        // Check if item exists
        if (!itemFactory.getRegistry().get(itemId).isPresent()) {
            player.sendMessage(Component.text("Item '" + itemId + "' not found!", NamedTextColor.RED));
            return;
        }
        
        // Create session for this player
        AbilityEditSession session = new AbilityEditSession(itemId);
        
        // Create main GUI
        Gui gui = Gui.gui()
            .title(Component.text("⚡ Ability Manager - " + itemId, NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .rows(6)
            .disableAllInteractions()
            .create();
        
        setupMainGUI(gui, session, player);
        gui.open(player);
    }
    
    private void setupMainGUI(Gui gui, AbilityEditSession session, Player player) {
        // Fill borders with decorative glass
        gui.getFiller().fillBorder(ItemBuilder.from(Material.PURPLE_STAINED_GLASS_PANE)
            .name(Component.text(" "))
            .asGuiItem());
        
        // Title area
        gui.setItem(4, ItemBuilder.from(Material.ENCHANTED_BOOK)
            .name(Component.text("🔮 Ability Creator", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .lore(
                Component.text("Click ability types below to add them", NamedTextColor.GRAY),
                Component.text("to your item!", NamedTextColor.GRAY)
            )
            .asGuiItem());
        
        // Ability type selection (row 2)
        setupAbilityTypes(gui, session, player);
        
        // Current abilities display (row 3)
        setupCurrentAbilities(gui, session, player);
        
        // Configuration area (row 4)
        setupConfiguration(gui, session, player);
        
        // Control buttons (row 5)
        setupControls(gui, session, player);
    }
    
    private void setupAbilityTypes(Gui gui, AbilityEditSession session, Player player) {
        // FlashBang ability
        gui.setItem(10, ItemBuilder.from(Material.TNT)
            .name(Component.text("💥 FlashBang", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .lore(
                Component.text("Creates a blinding flash that affects", NamedTextColor.GRAY),
                Component.text("players looking at the explosion", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Default Settings:", NamedTextColor.DARK_GRAY),
                Component.text("• Range: 10 blocks", NamedTextColor.DARK_GRAY),
                Component.text("• Duration: 5 seconds", NamedTextColor.DARK_GRAY),
                Component.text("• Cooldown: 3 seconds", NamedTextColor.DARK_GRAY),
                Component.empty(),
                Component.text("✦ Click to add!", NamedTextColor.GREEN, TextDecoration.ITALIC)
            )
            .asGuiItem(event -> {
                addAbility(session, "flashbang", player);
                setupMainGUI(gui, session, player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            }));
        
        // Heal ability
        gui.setItem(11, ItemBuilder.from(Material.GOLDEN_APPLE)
            .name(Component.text("❤ Heal", NamedTextColor.GREEN, TextDecoration.BOLD))
            .lore(
                Component.text("Restores player health with", NamedTextColor.GRAY),
                Component.text("beautiful heart particles", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Default Settings:", NamedTextColor.DARK_GRAY),
                Component.text("• Heal Amount: 6 HP (3 hearts)", NamedTextColor.DARK_GRAY),
                Component.text("• Cooldown: 10 seconds", NamedTextColor.DARK_GRAY),
                Component.empty(),
                Component.text("✦ Click to add!", NamedTextColor.GREEN, TextDecoration.ITALIC)
            )
            .asGuiItem(event -> {
                addAbility(session, "heal", player);
                setupMainGUI(gui, session, player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            }));
        
        // Teleport ability
        gui.setItem(12, ItemBuilder.from(Material.ENDER_PEARL)
            .name(Component.text("🌀 Teleport", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .lore(
                Component.text("Teleports player forward in the", NamedTextColor.GRAY),
                Component.text("direction they're looking", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Default Settings:", NamedTextColor.DARK_GRAY),
                Component.text("• Distance: 8 blocks", NamedTextColor.DARK_GRAY),
                Component.text("• Cooldown: 5 seconds", NamedTextColor.DARK_GRAY),
                Component.empty(),
                Component.text("✦ Click to add!", NamedTextColor.GREEN, TextDecoration.ITALIC)
            )
            .asGuiItem(event -> {
                addAbility(session, "teleport", player);
                setupMainGUI(gui, session, player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            }));
    }
    
    private void setupCurrentAbilities(Gui gui, AbilityEditSession session, Player player) {
        // Current abilities display
        List<Component> abilityLore = new ArrayList<>();
        if (session.abilities.isEmpty()) {
            abilityLore.add(Component.text("No abilities configured yet", NamedTextColor.GRAY, TextDecoration.ITALIC));
            abilityLore.add(Component.empty());
            abilityLore.add(Component.text("Click ability types above to add them!", NamedTextColor.YELLOW));
        } else {
            abilityLore.add(Component.text("Configured abilities:", NamedTextColor.YELLOW));
            abilityLore.add(Component.empty());
            for (int i = 0; i < session.abilities.size(); i++) {
                Map<String, Object> ability = session.abilities.get(i);
                String type = (String) ability.get("type");
                abilityLore.add(Component.text((i + 1) + ". " + formatAbilityName(type), NamedTextColor.WHITE));
            }
        }
        
        gui.setItem(22, ItemBuilder.from(Material.KNOWLEDGE_BOOK)
            .name(Component.text("📋 Current Abilities", NamedTextColor.AQUA, TextDecoration.BOLD))
            .lore(abilityLore)
            .asGuiItem());
    }
    
    private void setupConfiguration(Gui gui, AbilityEditSession session, Player player) {
        // Info about configuration
        gui.setItem(31, ItemBuilder.from(Material.WRITABLE_BOOK)
            .name(Component.text("⚙ Configuration", NamedTextColor.GOLD, TextDecoration.BOLD))
            .lore(
                Component.text("Abilities use smart defaults:", NamedTextColor.GRAY),
                Component.text("• Balanced for gameplay", NamedTextColor.GRAY),
                Component.text("• Easy to understand", NamedTextColor.GRAY),
                Component.text("• Configurable in items.yml", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Add abilities first, then edit", NamedTextColor.YELLOW),
                Component.text("the generated YAML config!", NamedTextColor.YELLOW)
            )
            .asGuiItem());
    }
    
    private void setupControls(Gui gui, AbilityEditSession session, Player player) {
        // Clear all abilities
        if (!session.abilities.isEmpty()) {
            gui.setItem(39, ItemBuilder.from(Material.RED_CONCRETE)
                .name(Component.text("🗑 Clear All", NamedTextColor.RED, TextDecoration.BOLD))
                .lore(
                    Component.text("Remove all abilities from this item", NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("⚠ This cannot be undone!", NamedTextColor.DARK_RED)
                )
                .asGuiItem(event -> {
                    session.abilities.clear();
                    setupMainGUI(gui, session, player);
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    player.sendMessage(Component.text("Cleared all abilities!", NamedTextColor.YELLOW));
                }));
        }
        
        // Save and generate YAML
        gui.setItem(40, ItemBuilder.from(Material.EMERALD)
            .name(Component.text("💾 Generate Config", NamedTextColor.GREEN, TextDecoration.BOLD))
            .lore(
                Component.text("Shows the YAML configuration", NamedTextColor.GRAY),
                Component.text("to add to your items.yml", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("✦ Click to generate!", NamedTextColor.GREEN, TextDecoration.ITALIC)
            )
            .asGuiItem(event -> {
                generateYAMLConfig(session, player);
                gui.close(player);
            }));
        
        // Cancel/Exit
        gui.setItem(41, ItemBuilder.from(Material.BARRIER)
            .name(Component.text("❌ Exit", NamedTextColor.RED, TextDecoration.BOLD))
            .lore(Component.text("Close without generating config", NamedTextColor.GRAY))
            .asGuiItem(event -> {
                gui.close(player);
                player.sendMessage(Component.text("Ability manager closed", NamedTextColor.GRAY));
            }));
    }
    
    private void addAbility(AbilityEditSession session, String type, Player player) {
        Map<String, Object> config = new HashMap<>();
        
        // Set smart defaults based on ability type
        switch (type) {
            case "flashbang":
                config.put("type", "flashbang");
                config.put("range", 10.0);
                config.put("duration", 5);
                config.put("cooldown", 3);
                break;
            case "heal":
                config.put("type", "heal");
                config.put("heal", 6.0);
                config.put("cooldown", 10);
                break;
            case "teleport":
                config.put("type", "teleport");
                config.put("distance", 8.0);
                config.put("cooldown", 5);
                break;
        }
        
        session.abilities.add(config);
        player.sendMessage(Component.text("✓ Added " + formatAbilityName(type) + " ability!", NamedTextColor.GREEN));
    }
    
    private void generateYAMLConfig(AbilityEditSession session, Player player) {
        if (session.abilities.isEmpty()) {
            player.sendMessage(Component.text("No abilities to generate config for!", NamedTextColor.RED));
            return;
        }
        
        // Generate YAML configuration
        StringBuilder yaml = new StringBuilder();
        yaml.append("\n§e§l=== YAML Configuration ===\n");
        yaml.append("§fAdd this to your items.yml under the '").append(session.itemId).append("' item:\n\n");
        yaml.append("§7abilities:\n");
        
        for (int i = 0; i < session.abilities.size(); i++) {
            Map<String, Object> ability = session.abilities.get(i);
            String abilityName = ability.get("type") + "_" + (i + 1);
            yaml.append("§7  ").append(abilityName).append(":\n");
            for (Map.Entry<String, Object> entry : ability.entrySet()) {
                yaml.append("§7    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        yaml.append("\n§eThen run: §f/itemcreator reload\n");
        yaml.append("§e=========================");
        
        // Send as multiple messages to avoid chat limits
        String[] lines = yaml.toString().split("\n");
        for (String line : lines) {
            player.sendMessage(Component.text(line.replace("§", "&")));
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
    
    private String formatAbilityName(String type) {
        return switch (type) {
            case "flashbang" -> "💥 FlashBang";
            case "heal" -> "❤ Heal";
            case "teleport" -> "🌀 Teleport";
            default -> type;
        };
    }
    
    // Session class to track editing state
    private static class AbilityEditSession {
        final String itemId;
        final List<Map<String, Object>> abilities = new ArrayList<>();
        
        AbilityEditSession(String itemId) {
            this.itemId = itemId;
        }
    }
}
