package dev.sora.itemcreator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import dev.sora.itemcreator.abilities.AbilityManager;
import dev.sora.itemcreator.abilities.ItemAbility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CustomItem {
    public static final String PDC_NAMESPACE = "itemcreator";

    private final String id;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final Integer customModelData;
    private final List<ItemAbility> abilities;

    public CustomItem(String id, Material material, String displayName, List<String> lore, Integer customModelData) {
        this(id, material, displayName, lore, customModelData, new ArrayList<>());
    }

    public CustomItem(String id, Material material, String displayName, List<String> lore, Integer customModelData,
            List<ItemAbility> abilities) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.customModelData = customModelData;
        this.abilities = abilities != null ? abilities : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<ItemAbility> getAbilities() {
        return new ArrayList<>(abilities);
    }

    public ItemStack toItemStack(@NotNull ItemFactory factory) {
        factory.getPlugin().getLogger().info("=== CUSTOM ITEM STACK CREATION DEBUG ===");
        factory.getPlugin().getLogger().info("Item ID: " + id);
        factory.getPlugin().getLogger().info("Material: " + material.name());
        factory.getPlugin().getLogger().info("Display Name: " + displayName);
        factory.getPlugin().getLogger().info("Lore: " + lore);
        factory.getPlugin().getLogger().info("Custom Model Data: " + customModelData);

        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (displayName != null) {
            Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
            meta.displayName(name);
            factory.getPlugin().getLogger().info("Set display name component: " + name);
        }
        if (!lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            meta.lore(loreComponents);
            factory.getPlugin().getLogger().info("Set lore components: " + loreComponents.size() + " lines");
        }
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
            factory.getPlugin().getLogger().info("Set custom model data: " + customModelData);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Tag with PDC id
        NamespacedKey key = new NamespacedKey(factory.getPlugin(), "cid");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, id);
        factory.getPlugin().getLogger().info("Set PDC tag with ID: " + id);
        stack.setItemMeta(meta);

        factory.getPlugin().getLogger().info("Final ItemStack: " + stack.getType().name() +
                " (Amount: " + stack.getAmount() + ")");
        if (stack.hasItemMeta() && stack.getItemMeta().hasCustomModelData()) {
            factory.getPlugin().getLogger().info("Final custom model data: " +
                    stack.getItemMeta().getCustomModelData());
        }
        factory.getPlugin().getLogger().info("=== END CUSTOM ITEM STACK CREATION DEBUG ===");

        return stack;
    }

    public static CustomItem fromConfig(String id, ConfigurationSection section) {
        return fromConfig(id, section, null);
    }

    public static CustomItem fromConfig(String id, ConfigurationSection section, AbilityManager abilityManager) {
        System.out.println("=== CUSTOM ITEM FROM CONFIG DEBUG ===");
        System.out.println("Loading item ID: " + id);
        System.out.println("Config section: " + section.getName());

        String materialString = section.getString("material", "PAPER");
        System.out.println("Material string from config: " + materialString);

        Material material = Material.matchMaterial(materialString);
        if (material == null) {
            System.out.println("Failed to match material '" + materialString + "', defaulting to PAPER");
            material = Material.PAPER;
        } else {
            System.out.println("Matched material: " + material.name());
        }

        String name = section.getString("name", null); // Don't default to id, use null for no custom name
        System.out.println("Display name: " + name);

        List<String> lore = section.getStringList("lore");
        System.out.println("Lore lines: " + lore);

        Integer cmd = section.isInt("custom-model-data") ? section.getInt("custom-model-data") : null;
        System.out.println("Custom model data: " + cmd);

        // Load abilities
        List<ItemAbility> abilities = new ArrayList<>();
        ConfigurationSection abilitiesSection = section.getConfigurationSection("abilities");
        if (abilitiesSection != null && abilityManager != null) {
            System.out.println("Loading abilities...");

            for (String abilityKey : abilitiesSection.getKeys(false)) {
                ConfigurationSection abilityConfig = abilitiesSection.getConfigurationSection(abilityKey);
                if (abilityConfig != null) {
                    String abilityType = abilityConfig.getString("type");
                    System.out.println("Loading ability: " + abilityKey + " of type: " + abilityType);

                    if (abilityType != null) {
                        Map<String, Object> config = new HashMap<>();
                        for (String configKey : abilityConfig.getKeys(false)) {
                            if (!"type".equals(configKey)) {
                                config.put(configKey, abilityConfig.get(configKey));
                            }
                        }

                        ItemAbility ability = abilityManager.createAbility(abilityType, config);
                        if (ability != null) {
                            abilities.add(ability);
                            System.out.println("Successfully loaded ability: " + abilityType);
                        } else {
                            System.out.println("Failed to create ability of type: " + abilityType);
                        }
                    }
                }
            }
        } else if (abilitiesSection != null) {
            System.out.println("Abilities section found but no AbilityManager provided");
        }

        CustomItem item = new CustomItem(id, material, name, lore, cmd, abilities);
        System.out.println("Created CustomItem: " + item.getId() + " -> " + item.material.name() + " with "
                + abilities.size() + " abilities");
        System.out.println("=== END CUSTOM ITEM FROM CONFIG DEBUG ===");

        return item;
    }
}
