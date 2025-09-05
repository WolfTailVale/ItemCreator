# ItemCreator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Paper API](https://img.shields.io/badge/Paper-1.21.1+-blue.svg)](https://papermc.io/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)

A powerful Paper plugin for creating custom items and bundle recipes through an intuitive GUI and configuration system. Perfect for survival servers wanting to add custom crafting recipes without client-side mods.

## ✨ Features

- 🎨 **GUI Recipe Creator** - Visual crafting interface with drag-and-drop item placement
- 📦 **Bundle System** - Create "box of items" that pack/unpack resources (e.g., box of gunpowder)
- ⚙️ **Configuration-Driven** - Define items and recipes through YAML files
- 🎯 **Custom Items** - Items with custom names, lore, model data, and block placement control
- 🔧 **Modern API** - Built with Paper 1.21.1+ and Adventure Components
- 🎮 **User-Friendly** - Chat-based metadata input with guided prompts

## 🚀 Quick Start

### Requirements

- Java 21
- Paper 1.21.1+ (tested with 1.21.1)
- Gradle (wrapper included - no system installation needed)

### Installation

1. **Download** the latest release from the [Releases](../../releases) page
2. **Place** `ItemCreator-x.x.x.jar` in your server's `plugins/` directory
3. **Start** your server to generate default configuration files
4. **Configure** your items in `plugins/ItemCreator/items.yml`
5. **Restart** or reload to apply changes

### Building from Source

```bash
git clone https://github.com/yourusername/ItemCreator.git
cd ItemCreator
./gradlew build
```

The built jar will be located at: `build/libs/ItemCreator-0.1.0-dev.jar`

## 📖 Usage

### Creating Recipes

1. **Open the GUI** - `/createrecipe` (requires `itemcreator.createrecipe` permission)
2. **Add ingredients** - Drag items from your inventory into the 3x3 crafting grid
3. **Set output** - Place the result item in the output slot
4. **Configure recipe** - Click "Save Recipe" and follow the chat prompts:
   - Recipe ID (unique identifier)
   - Custom name with color codes
   - Lore lines (item description)
   - Custom model data
   - Whether the item can be placed as a block

### Giving Items

Use `/giveitem <item_id>` to give custom items to players (requires `itemcreator.give` permission).

## ⚙️ Configuration

Edit `plugins/ItemCreator/items.yml` to define your custom items and bundles:

```yaml
items:
  box_of_gunpowder:
    material: CHEST
    name: "&eBox of Gunpowder"
    lore:
      - "&7A neatly packed crate of explosive dust."
      - "&7Right-click to unpack 9 gunpowder."
    custom-model-data: 1001
    placeable: false

bundles:
  gunpowder:
    item: GUNPOWDER
    count: 9
    box-id: box_of_gunpowder
```

### Item Properties

- `material` - Minecraft material type
- `name` - Display name (supports `&` color codes)
- `lore` - List of description lines
- `custom-model-data` - For resource pack models
- `placeable` - Whether item can be placed as a block

### Bundle System

Bundles allow players to craft multiple items into a "box" and unpack them later:

- **Packing**: Place 9 items in a 3x3 pattern to create the box
- **Unpacking**: Right-click the box to get the original items back

## 🎮 Commands & Permissions

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/createrecipe` | Open the recipe creation GUI | `itemcreator.createrecipe` |
| `/giveitem <id>` | Give a custom item to yourself | `itemcreator.give` |

### Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `itemcreator.give` | `op` | Use the `/giveitem` command |
| `itemcreator.createrecipe` | `op` | Use the recipe creator GUI |

## 🔧 Development

After installing the plugin on your Paper server:

1. **Start server** - ItemCreator will create `plugins/ItemCreator/items.yml` and `config.yml`
2. **Give items** - Try `/giveitem box_of_gunpowder` to get a box
3. **Test bundles** - Craft 9 gunpowder in a 3x3 pattern to make a box
4. **Test unboxing** - Right-click a box to get 9 gunpowder back
5. **Add items** - Edit `items.yml`, then restart server to load new items

## Example Usage

```yaml
# In plugins/ItemCreator/items.yml
items:
  custom_sword:
    material: DIAMOND_SWORD
    name: "&6Legendary Blade"
    lore:
      - "&7A sword of immense power"
      - "&7Deals extra damage to mobs"
    custom-model-data: 2001

bundles:
  arrows:
    item: ARROW           # What item to bundle
    count: 9             # How many you get back
    box-id: box_of_arrows # Which custom item is the "box"
```

## Commands

- `/giveitem custom_sword` - Give yourself the legendary blade
- `/giveitem box_of_arrows steve` - Give steve a box of arrows
- `/createrecipe` - Open the recipe creator GUI interface

## 📖 Recipe Creator GUI

The plugin includes a powerful in-game interface for creating custom recipes:

1. **Open the interface** - `/createrecipe` (requires `itemcreator.createrecipe` permission)
2. **Arrange ingredients** - Drag items from your inventory into the 3x3 crafting grid
3. **Set recipe type** - Click the crafting table/dropper to toggle between shaped/shapeless
4. **Configure output** - Place your base item in the output slot  
5. **Save recipe** - Click the emerald block to start the configuration process

### Chat-Based Configuration

When saving, you'll be prompted via chat to configure:

- **Recipe ID** - Unique identifier (letters, numbers, underscores only)
- **Custom name** - Display name with & color codes (or 'skip')
- **Lore lines** - Multiple lines of item description (type 'done' when finished)
- **Model data** - Custom model data number (or 'skip')
- **Placeable** - Whether the item can be placed as a block ('yes' or 'no')

## 💡 Example Workflow

1. **Create a recipe** - `/createrecipe` opens the GUI
2. **Add ingredients** - Place 8 iron ingots around the outside edge  
3. **Set output** - Place a diamond in the center output slot
4. **Configure** - Click save, enter "super_diamond" as ID, set name "&bSuper Diamond", choose 'yes' for placeable
5. **Result** - Players can now craft placeable super diamond blocks with your recipe!
6. **Give items** - `/giveitem super_diamond_output` gives the custom item directly

Want to contribute? Check out our [Contributing Guide](CONTRIBUTING.md)!

### Building

```bash
./gradlew clean build
```

### Project Structure

```text
src/main/java/dev/sora/itemcreator/
├── ItemCreatorPlugin.java          # Main plugin class
├── commands/                       # Command handlers
├── core/                          # Item management system
├── gui/                           # Recipe creation interface
└── listeners/                     # Event handlers
```

## 📋 Roadmap

- [ ] **Smithing Recipes** - Support for smithing table recipes
- [ ] **Furnace Recipes** - Custom smelting and blasting recipes  
- [ ] **Item Behaviors** - Right-click actions and custom interactions
- [ ] **Advanced Items** - Items with cooldowns, durability, and effects
- [ ] **Recipe Book** - Integration with Minecraft's recipe book
- [ ] **API** - Developer API for other plugins

## 🐛 Bug Reports & Feature Requests

Found a bug or have an idea? [Open an issue](../../issues) on GitHub!

Please include:

- Server version and plugin version
- Steps to reproduce (for bugs)
- Expected vs actual behavior
- Any relevant console errors

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built for the [Paper](https://papermc.io/) Minecraft server platform
- Uses the [Adventure](https://github.com/KyoriPowered/adventure) text component library
- Inspired by the need for server-side custom content without client mods

---

Happy crafting! 🎮⚡
