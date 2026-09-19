# Ultimate Transport System

One transport mod for energy, fluids, gases, items and players.

| Branch   | Minecraft | NeoForge  | JDK |
| -------- | --------- | --------- | --- |
| `1.21.1` | 1.21.1    | 21.1.240  | 21  |

Two transport systems live side by side, because they are good at different things.

**Cables** move things instantly along a network. You configure each face of each cable, slot an
upgrade into it to raise its throughput, and filter what may pass. Nothing is ever visible inside a
cable, which is what makes them cheap enough to run in bulk.

**Pipes** carry items you can watch travelling. They are slower and more decorative, and they route
themselves: an item leaving a pipe looks for the nearest inventory that will take it, preferring one
that already holds that item, so a plain run of pipes sorts itself with no configuration at all.

## Cables

| Cable     | Carries                                                |
| --------- | ------------------------------------------------------ |
| Energy    | Forge Energy                                           |
| Fluid     | any NeoForge fluid handler                             |
| Item      | any NeoForge item handler                              |
| Gas       | Mekanism chemicals; craftable only with Mekanism loaded |
| Universal | all of the above, and joins every network it touches   |

### Faces

Every face of a cable is one of four things. Right-click a face with the **Configurator** to cycle it,
sneak to cycle backwards:

- **Disconnected** - no arm, no transfer.
- **Connected** - the network passes through, but nothing enters or leaves here.
- **Insert** - the network delivers into the block on this side. This is the default for any face that
  touches a machine.
- **Extract** - the cable pulls out of the block on this side and feeds the network.

Right-click a face with an empty hand to open its settings: redstone mode, distribution mode, priority,
one upgrade slot and up to fifteen filter slots.

Distribution decides how one extracting face spreads what it pulls: **round robin** (an even split that
resumes where it left off), **nearest first**, **furthest first**, or **random**. Priority beats
distance, so a face at priority 5 fills before a face at priority 0 no matter where it sits.

Filters hold item stacks whatever the cable carries: a fluid cable filters on the fluid inside a bucket
or tank you put in the slot, a gas cable on the chemical inside a Mekanism tank. Switch the filter
between whitelist and blacklist, and choose whether components and mod id are part of the match.

### Upgrades

An upgrade goes into one face and raises everything that face does.

| Upgrade  | Energy      | Fluid and gas | Items     | Filter slots |
| -------- | ----------- | ------------- | --------- | ------------ |
| none     | 256 FE/t    | 50 mB/t       | 1 / 20t   | 3            |
| Basic    | 1,024 FE/t  | 200 mB/t      | 4 / 20t   | 5            |
| Improved | 8,192 FE/t  | 1,000 mB/t    | 8 / 10t   | 7            |
| Advanced | 65,536 FE/t | 5,000 mB/t    | 16 / 5t   | 9            |
| Ultimate | 524,288 FE/t| 25,000 mB/t   | 32 / 2t   | 12           |
| Infinity | unlimited   | unlimited     | 64 / 1t   | 15           |

Cables also accept what is pushed into them. A generator that shoves power outwards, or another mod's
pipe pushing items, hands its cargo to the cable and the cable places it the same tick; a cable never
buffers anything, so what the network cannot place is refused rather than stored.

### Facades

A **Facade** covers a cable face with any full block. Use a blank facade on a block to copy it - the
block is not consumed - then use the filled facade on a cable face. The face then renders and behaves
as that block, and the Configurator takes it back off. Breaking the cable returns every facade and
every upgrade it was carrying.

## Pipes

| Pipe       | What it does                                                           |
| ---------- | ---------------------------------------------------------------------- |
| Transport  | carries items, one block per second                                    |
| Extraction | pulls from the inventories it touches; a redstone signal stops it       |
| Speed      | four times faster                                                      |
| Void       | destroys what reaches it, and only accepts what nothing else will take  |

Routing looks ahead: when an item arrives at a pipe, the network is searched outward for inventories
that will accept it, and the item is sent toward the closest one, preferring an inventory that already
holds that item. An item with nowhere to go turns around, and is dropped on the floor if it has been
circling for thirty seconds. Breaking a pipe drops whatever was inside it.

## Player tubes

Walk into a **Player Tube** and it carries you along at about four times sprinting speed. Tubes have no
collision, so you fall in simply by touching one; inside, the tube holds you on its axis. At a junction
you steer by looking the way you want to go, and you leave through any open end.

## Energy cells

Ten tiers. Capacity quadruples each step, and throughput is a hundredth of capacity, so a bigger cell is
also a faster one.

| Tier | Capacity        | Transfer        |
| ---- | --------------- | --------------- |
| 1    | 1,000,000 FE    | 10,000 FE/t     |
| 2    | 4,000,000 FE    | 40,000 FE/t     |
| 3    | 16,000,000 FE   | 160,000 FE/t    |
| 4    | 64,000,000 FE   | 640,000 FE/t    |
| 5    | 256,000,000 FE  | 2,560,000 FE/t  |
| 6    | 1.024 GFE       | 10,240,000 FE/t |
| 7    | 4.096 GFE       | 40,960,000 FE/t |
| 8    | 16.384 GFE      | 163,840,000 FE/t|
| 9    | 65.536 GFE      | 655,360,000 FE/t|
| 10   | 262.144 GFE     | 2,147,483,647 FE/t |

Each face is set with the Configurator to input, output, both or disabled, and defaults to both. Cells
push into whatever is next to them, but never into another cell that is fuller than they are, so a bank
of cells levels out instead of passing the same energy back and forth. A cell keeps its charge when
broken, emits a comparator signal for its fill level, and works from your inventory, so it can charge a
tool without being placed.

Above tier 6 a cell holds more than an `int` can express. The Forge Energy view saturates at
2,147,483,647; the tooltip and Flux Networks both report the real figure.

## Integrations

**Mekanism** (optional). The gas cable is built on Mekanism's chemical API and is craftable only when
Mekanism is installed. Every call into Mekanism lives in `compat/mekanism`, and nothing outside that
package names a Mekanism type, so the mod loads and runs unchanged without it.

**Flux Networks** (optional, 1.21.1). Plugs and points already reach this mod through Forge Energy with
no code at all. What the integration adds is Flux's own long-precision energy interface on the cells,
the cell item and the energy cable, so a tier 10 cell in a Flux network moves its real capacity instead
of the int-clamped figure.

**JEI** (optional). Recipes show up through the normal recipe types; no plugin needed.

## Adding generator fuels

What a generator burns is a recipe, so a datapack can add fuels or replace the built-in ones. No mod
and no KubeJS needed. Drop a file at `data/<your pack>/recipe/anything.json`:

```json
{
  "type": "ultimatetransport:generator_fuel",
  "generator": "fuel_generator",
  "fuel": { "item": "minecraft:coal" },
  "burn_time": 1600
}
```

- `generator` -- `fuel_generator`, `lava_generator` or `nether_star_generator`.
- `fuel` -- any ingredient, so `{ "tag": "c:coals" }` works.
- `burn_time` -- ticks it burns for.
- `rate` -- optional FE/t for this fuel alone; omit it to use the generator's own rate.

This mod ships a KubeJS plugin, so the type has a proper builder and needs no `event.custom`:

```js
ServerEvents.recipes(event => {
  event.recipes.ultimatetransport.generator_fuel('lava_generator', 'minecraft:magma_block', 700)

  // fourth argument is the optional rate
  event.recipes.ultimatetransport.generator_fuel('fuel_generator', '#c:storage_blocks/coal', 16000, 80)
})
```

The mod ships its own lava and nether star fuels this way, at
`data/ultimatetransport/recipe/fuel/`, so there is a working example to copy. The fuel generator also
accepts anything with a vanilla furnace burn time without a recipe at all.

## Building

```bash
./gradlew build
```

JDK 21. The dev runtime always pulls in the integration mods: Mekanism, Flux Networks, JEI, Jade,
Patchouli, Ars Nouveau, Ender IO, Camol, Ore Excavation, FTB Ultimine, AE2 and Ultimate Avaritia
Additions.

Every texture in this mod is generated from scratch by `tools/GenTextures.java`
and `tools/GenGui.java`; run them with `java tools/GenTextures.java .` from the project root to
rebuild them. The cable geometry comes from Pretty Pipez; see the license section below.

## License

All code and all assets are licensed under [GPLv3](LICENSE), in adherence to the license used by
[Pretty Pipez](https://www.curseforge.com/minecraft/texture-packs/pretty-pipez) by pyrox645, from
which the cable textures and cable models derive.

[THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) lists every file that came from Pretty Pipez and
says which of them were modified.
