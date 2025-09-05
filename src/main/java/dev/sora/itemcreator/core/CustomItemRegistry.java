package dev.sora.itemcreator.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import dev.sora.itemcreator.abilities.AbilityManager;

public class CustomItemRegistry {
    private final Plugin plugin;
    private final Map<String, CustomItem> items = new HashMap<>();
    private AbilityManager abilityManager;

    public CustomItemRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setAbilityManager(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }

    public void loadFromConfig(FileConfiguration cfg) {
        items.clear();
        ConfigurationSection itemsSec = cfg.getConfigurationSection("items");
        if (itemsSec == null)
            return;
        for (String id : itemsSec.getKeys(false)) {
            CustomItem item = CustomItem.fromConfig(id, itemsSec.getConfigurationSection(id), abilityManager);
            items.put(id, item);
        }
    }

    public Optional<CustomItem> get(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public Collection<String> ids() {
        return Collections.unmodifiableSet(items.keySet());
    }

    public Optional<CustomItem> fromStack(ItemStack stack) {
        if (stack == null)
            return Optional.empty();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return Optional.empty();
        String id = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "cid"), PersistentDataType.STRING);
        if (id == null)
            return Optional.empty();
        return get(id);
    }
}
