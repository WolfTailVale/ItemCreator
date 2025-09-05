package dev.sora.itemcreator.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomItem {
    public static final String PDC_NAMESPACE = "itemcreator";

    private final String id;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final Integer customModelData;

    public CustomItem(String id, Material material, String displayName, List<String> lore, Integer customModelData) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.customModelData = customModelData;
    }

    public String getId() { return id; }

    public ItemStack toItemStack(@NotNull ItemFactory factory) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (displayName != null) {
            Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
            meta.displayName(name);
        }
        if (!lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            meta.lore(loreComponents);
        }
        if (customModelData != null) meta.setCustomModelData(customModelData);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Tag with PDC id
        NamespacedKey key = new NamespacedKey(factory.getPlugin(), "cid");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, id);
        stack.setItemMeta(meta);
        return stack;
    }

    public static CustomItem fromConfig(String id, ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "PAPER"));
        if (material == null) material = Material.PAPER;
        String name = section.getString("name", id);
        List<String> lore = section.getStringList("lore");
        Integer cmd = section.isInt("custom-model-data") ? section.getInt("custom-model-data") : null;
        return new CustomItem(id, material, name, lore, cmd);
    }
}
