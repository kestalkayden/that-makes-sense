# That Makes Sense

A collection of small quality-of-life tweaks that make Minecraft behave the way you'd expect — each one independently toggleable from an in-game config screen.

Targets Minecraft 26.2 on Fabric and NeoForge.

## Features

| Feature | Default | What it does |
|---|---|---|
| **No Crop Trample** | on | Farmland no longer reverts to dirt when players, mobs, or projectiles land on it. Fall damage still applies. |
| **Copper Chest Rows** | always | A single copper chest holds 4 rows (36 slots); a double copper chest holds 8 rows (72 slots), with a custom screen. Not toggleable — resizing chests is one-way, so this avoids ever dropping items. |
| **Silk Touch → Budding Amethyst** | on | A Silk Touch tool harvests budding amethyst blocks (vanilla drops nothing), making amethyst geodes renewable. |
| **Infinity + Mending** | on | Allow both enchantments on one bow. |
| **Stack Damage Enchantments** | on | Allow the melee damage enchantments (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach) to coexist on one weapon. |
| **Cobblestone → Blackstone** | on | Blasting cobblestone in a **blast furnace** yields blackstone (a regular furnace still makes stone). Makes blackstone renewable. Gated server-side. |
| **Double Doors** | on | Opening one door of a matching pair opens both, like a real double door. Iron doors left to redstone. |
| **No Berry Damage** | on | Sweet berry bushes no longer hurt you when you walk through them (mobs still take it). |
| **Right-Click Harvest** | on | Right-clicking a fully-grown crop (wheat, carrots, potatoes, beetroot, nether wart) harvests and replants it in place. Sneak to place blocks instead. |
| **No Creeper Block Damage** | on | Creeper explosions still hurt entities but leave terrain intact. |
| **No Enderman Griefing** | on | Endermen no longer pick up or place blocks. |
| **Totem from Inventory** | on | A Totem of Undying saves you from anywhere in the inventory, not just a hand. |
| **Disable Phantoms** | on | Phantoms never spawn from insomnia (spawn eggs/commands still work). |
| **No Pet Teleport Damage** | on | Tamed pets take no fall damage teleporting to you. |
| **No Ender Pearl Damage** | on | Throwing an ender pearl no longer deals teleport damage. |
| **Rotten Flesh → Leather** | on | Smelting rotten flesh in a **furnace** yields leather. |
| **Zombie Jerky** | on | Cooking rotten flesh in a **smoker** yields Zombie Jerky, a small always-edible snack (adds the item). |
| **Stackable Totems** | on | Totems of Undying stack up to 64 instead of one at a time. Gated server-side. |
| **Hide Armor** | on | An inventory button hides your own armor from view while it stays equipped. Client-side — only changes what you see. |
| **No Ghast Fireball Damage** | on | Ghast fireball explosions still hurt entities but leave terrain intact. |
| **Cobweb Shears** | on | Shears break cobweb instantly (and it still drops cobweb). |
| **Villager Stock** | on | Villagers sell each trade ~4× as often before it locks (multiplier configurable). Gated server-side. |
| **Bonemeal Extras** | on | Bonemeal grows nether wart, cactus, and sugar cane. |
| **Stable Villager Prices** | on | Trade prices stop climbing from heavy-use demand (discounts still apply). Gated server-side. |
| **Longer Leads** | on | Leads snap at ~24 blocks instead of 12 (distance configurable). |
| **Infinity on All Arrows** | on | An Infinity bow shoots tipped & spectral arrows free too (no pickup, so no dupe). |
| **Heal Parrots** | on | Feed seeds to an injured tamed parrot to heal it (like wolves/cats). |
| **Pet Damage Immunity** | on | Tamed pets take no damage from you or your other pets (mobs/environment still do). |
| **No Pet Fall Damage** | on | Tamed wolves, cats, and parrots take no fall damage. |

More on the way, one at a time.

## Requirements

- Minecraft **26.2**
- Java **25**
- Fabric Loader **0.18.4+** with **Fabric API** (and optionally **Mod Menu** for the config button), *or* NeoForge **26.2+**

## Configuration

Config lives at `config/thatmakessense.json` (created on first launch) and can be edited in-game:

- **Fabric**: open **Mod Menu → That Makes Sense → Config**.
- **NeoForge**: open the **Mods** list, select **That Makes Sense**, click **Config**.

Changes are saved when you close the screen.

## Repo layout

```
common/            shared code compiled into both loaders (config + config screen)
  src/main/        loader-agnostic (config)
  src/client/      client-only (config screen)
fabric/            Fabric loader subproject (No Crop Trample mixin, Mod Menu hook)
neoforge/          NeoForge loader subproject (No Crop Trample event, config-screen hook)
shared-resources/  assets, lang — shared between both loaders
```

`common` is a purity gate: it compiles the shared sources against vanilla NeoForm-provided
Minecraft (no loader classes) and fails the build if shared code reaches for a Fabric/NeoForge API.
Each loader pulls those same sources in via `srcDir` and compiles them against its own Minecraft view.

## Building

```bash
./gradlew buildAll
```

Produces:
- `fabric/build/libs/thatmakessense-fabric-<version>.jar`
- `neoforge/build/libs/thatmakessense-neoforge-<version>.jar`

Individual loaders: `./gradlew :fabric:build` or `./gradlew :neoforge:build`.
Run the shared-code purity check: `./gradlew :common:build`.
Dev clients: `./gradlew :fabric:runClient` or `./gradlew :neoforge:runClient`.

## License

MIT.
