# That Makes Sense

A collection of small quality-of-life tweaks that make Minecraft behave the way you'd expect — each one independently toggleable from an in-game config screen.

Targets Minecraft 26.2 on Fabric and NeoForge.

## Features

| Feature | Default | What it does |
|---|---|---|
| **No Crop Trample** | on | Farmland no longer reverts to dirt when players, mobs, or projectiles land on it. Fall damage still applies. |
| **Copper Chest Rows** | always | A single copper chest holds 4 rows (36 slots); a double copper chest holds 8 rows (72 slots), with a custom screen. Not toggleable — resizing chests is one-way, so this avoids ever dropping items. |
| **Silk Touch → Budding Amethyst** | on | A Silk Touch tool harvests budding amethyst blocks (vanilla drops nothing), making amethyst geodes renewable. |

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
