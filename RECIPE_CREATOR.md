# Recipe Creator UI - Quick Start Guide

## Installation
1. Put `ItemCreator-0.1.0-dev.jar` in your `plugins/` folder
2. Start your Paper 1.21.1+ server
3. Grant yourself `itemcreator.createrecipe` permission (default: op)

## Creating Your First Recipe

### Step 1: Open the Interface
```
/createrecipe
```
This opens a GUI with:
- 3x3 crafting grid (yellow borders)
- Recipe type toggle (top center)
- Output slot (right side)
- Control buttons (bottom)

### Step 2: Design Your Recipe
1. **Place ingredients** - Drag items into the 3x3 grid
2. **Toggle recipe type** - Click the crafting table icon to switch between:
   - **Shaped** (position matters) - crafting table icon
   - **Shapeless** (position doesn't matter) - dropper icon
3. **Set output item** - Place your base item in the output slot

### Step 3: Save and Configure
1. Click the **emerald block** (Save Recipe)
2. Follow the chat prompts:
   - Enter recipe ID (e.g., `magic_sword`)
   - Set custom name (e.g., `&6Magic Sword` or `skip`)
   - Add lore lines (one per message, `done` when finished)
   - Set model data (number or `skip`)
   - Choose if placeable (`yes` for blocks, `no` for items only)

### Step 4: Test Your Recipe
- The recipe is immediately available server-wide
- Players can craft it normally
- Use `/giveitem <recipe_id>_output` to give the item directly

## Examples

### Simple Shaped Recipe (Magic Diamond)
```
Grid Layout:
[Gold] [Gold] [Gold]
[Gold] [Diamond] [Gold]  
[Gold] [Gold] [Gold]

Recipe ID: magic_diamond
Name: &bMagic Diamond
Lore: &7A diamond infused with gold magic
Model Data: 12345
```

### Shapeless Recipe (Energy Drink)
```
Ingredients (any arrangement):
- Sugar
- Redstone Dust  
- Glass Bottle

Recipe ID: energy_drink
Name: &cEnergy Drink
Lore: &7Gives you speed and energy
Model Data: skip
```

## Tips
- Use `&` for color codes in names/lore (`&a` = green, `&c` = red, etc.)
- Recipe IDs must be unique and use only letters, numbers, underscore
- Type `cancel` during chat prompts to abort
- Shaped recipes preserve exact positions, shapeless don't
- The output item's base material comes from what you place in the output slot

Your recipes are saved to `plugins/ItemCreator/items.yml` and persist across server restarts!
