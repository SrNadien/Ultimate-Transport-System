# CurseForge copy

## Summary (short description field, 255 characters max)

> Cables, batteries, generators and player tubes. Filtered per-face routing, facades, capacitor banks that merge, a Mekanism-style configurator, and fuels you can add from a datapack.

*(198 characters.)*

Alternative, shorter:

> Move energy, fluids, items, chemicals and mana through one cable system. Filtered faces, facades, merging batteries, configurable generators and tubes you ride.

*(159 characters.)*

---

## Description (project page)

# Ultimate Transport System

Everything that moves through a base, in one mod: **energy, fluids, items, Mekanism chemicals and Ars Nouveau source**, plus batteries that grow as you stack them, generators you can feed anything, and glass tubes you ride yourself.

---

## Cables

Six cables — energy, fluid, item, chemical, source and a **universal** cable that carries all five at once, each cargo with its own settings behind its own tab.

Only the faces that actually touch a machine get configured; the rest of a run has nothing to set, so a long line stays a long line instead of a hundred decisions.

**Per face you get:**

- **Insert or extract**, and **retrieve**, which pulls from the far end of the network back to that face
- **Filters** — by item, by tag, by fluid, by NBT, with a destination slot
- **Redstone** — ignore it, off when powered, on when powered, or always off
- **Distribution** — nearest, furthest, round robin or random

**Six upgrades** — none, basic, improved, advanced, ultimate and infinity — set the throughput, how many filters a face holds and how often items move. Every rate is in the config, so a pack can retune the whole ladder without touching the mod.

**Facades** cover a cable with any full block: the run disappears into your wall and keeps working. Craft one by putting a blank facade next to the block you want; the block is consumed, the facade is not, so one block gives you as many facades as you need. The configurator takes them back off.

The wrench also **severs** two cables that touch, so two networks can run side by side without joining.

---

## Batteries

Eleven rungs from **Basic** to **Infinite**, plus a **Creative** battery that never empties.

Put two of the same rung against each other and they become one bank: **capacity, charge and throughput all add up**, the charge levels itself out across the whole group, and a single gauge runs the length of the stack instead of one per block. The frame closes around the outside of the bank and the seams between blocks disappear.

Each battery has a slot that charges an item and a slot that drains one into the bank, a face configuration you can open from its own tab, and an auto-eject switch. Break one and it keeps its charge.

---

## Generators

Four to start with:

| Generator | Notes |
|---|---|
| **Fuel** | Burns anything with a burn time |
| **Lava** | Long, steady burn |
| **Solar** | Quiet daytime trickle |
| **Nether Star** | Enormous output, and it withers everything around it while it runs |

Output, buffer and transfer rate are all configurable, and each generator charges an item in its own slot.

**New fuels need no code.** Add a recipe from a datapack:

```json
{
  "type": "ultimatetransport:generator_fuel",
  "generator": "fuel_generator",
  "fuel": { "item": "minecraft:blaze_rod" },
  "burn_time": 2400,
  "rate": 60
}
```

Or from KubeJS, if you would rather:

```js
ServerEvents.recipes(event => {
  event.recipes.ultimatetransport.generator_fuel('fuel_generator', 'minecraft:blaze_rod', 2400, 60)
})
```

JEI shows each generator its own fuel list, so a nether star only ever points at the nether star generator.

---

## Configurator

One tool for the whole mod, and for everyone else's machines too.

- **Modes**: items, fluids, chemicals, energy, source, empty, rotate and wrench. Chemicals only appear with Mekanism installed, source only with Ars Nouveau.
- **Sneak and scroll** to change mode, or hold the mode key for the wheel.
- **Click a face to step it, sneak-click to step it back.** While a configuration mode is in hand, the block you are looking at shows its own settings as a coloured pane on every face — no need to open anything.
- **Rotate** points a block at the face you clicked. **Wrench** turns a block on a plain click and takes it back, contents and all, when you sneak.
- It sits in `#c:tools/wrench`, so machines that ask for a wrench by tag accept it — and it configures **Mekanism machines** through their own side configuration.

---

## Tubes

A port of **Tube Transport System**: glass tubes you climb into and ride, with stations to enter and leave, tubes for every direction, and a colour per orientation so a junction is readable. Top speed, render pass and the placement preview are all configurable.

---

## Works with

**Mekanism** (chemical cable, machine side configuration), **Ars Nouveau** (source cable), **Flux Networks**, **Ender IO**, **AE2**, **Camol**, **FTB Ultimine**, **Ore Excavation**, **Ultimate Avaritia Additions**, **JEI**, **Jade**, **Patchouli** and **KubeJS**. Every one of them is optional; the mod runs fine on its own and simply grows when they are present.

An in-game **Patchouli guide book** covers all of it, in English and in Spanish (Spain, Argentina and Mexico).

---

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.0** or newer
