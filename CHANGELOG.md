# Changelog
## [1.0.1]

### Added

- **Disconnected faces.** Every face that touches a container now has a third setting beside insert
  and extract: leave it alone. A cable set that way stops reaching for the block and drops the arm it
  was drawing, so it comes away from the chest instead of sitting against it doing nothing.
- **Wrenches from other mods work on this mod's blocks.** Create, Ender IO, Mekanism, AE2, Immersive
  Engineering, Pipez and anything in the common wrench tags: a click steps the clicked face, a
  sneaking click takes the block back with everything in it. Wrenches that belong to no shared tag
  are recognised by name, and none of this changes what they do anywhere else.
- On a **universal cable** a foreign wrench moves every cargo at once, since it carries no mode dial
  of its own. The mod's configurator and the cable's own screen still set the five apart.

### Fixed

- **Stations no longer trap a rider in a loop.** A station lifted anyone standing in it into whatever
  tube sat above, without looking at which way that tube pointed. A tube pointing back down pushed
  the rider straight into the station again, and the two handed them back and forth for as long as
  they stood there. A station now refuses to lift into a downward tube and keeps its floor solid.
- **Tubes are transparent again.** Their models asked for the cutout pass, which keeps only what is
  fully opaque or fully clear and turned the tinted glass into a solid wall. They now draw in the
  translucent pass, and the `TubeRenderPass` setting that was meant to choose between the two finally
  does.

## [1.0.0] — first release

### Cables

- Six cables: energy, fluid, item, chemical (Mekanism), source (Ars Nouveau) and a universal cable
  that carries all five, each cargo keeping its own settings behind its own tab.
- Only the faces that touch a container can be configured; the rest of a run has nothing to set.
- Per face: insert, extract and **retrieve**, which pulls from the far end of the network back to
  that face.
- Filters by item, tag, fluid and NBT, with a destination slot. How many a face holds depends on the
  upgrade.
- Redstone modes: ignored, off when powered, on when powered, always off.
- Distribution modes: nearest, furthest, round robin, random.
- Six upgrades — none, basic, improved, advanced, ultimate, infinity — setting throughput, filter
  count and how often items move. Every rate is in the server config.
- **Facades**: cover a cable with any full block and the run disappears into the wall. Crafted from a
  blank facade beside the block to copy, which consumes the block, not the facade. The configurator
  takes them back off.
- Cables can be **severed** with the wrench, so two networks run side by side without joining.
- Screen with a search box, a dark and a light theme, and numbers in compact units.

### Batteries

- Eleven rungs, Basic through Infinite, plus a Creative battery that never empties.
- Batteries of the same rung that touch form one **bank**: capacity, charge and throughput add up,
  what arrives lands in the emptiest block, what leaves comes from the fullest, and the whole group
  levels itself out on a timer.
- A single gauge runs the length of the bank instead of one per block, and the frame closes around
  the outside of the group.
- A slot that charges an item and a slot that drains one into the bank, both marked.
- Face configuration behind its own tab, plus an auto-eject switch.
- A broken battery keeps its own charge.

### Generators

- Fuel, lava, solar and nether star. The nether star generator withers what stands near it while it
  runs, with radius and duration in the config.
- Output, buffer and transfer rate are configurable per generator.
- A charge slot on every generator.
- Fuels can be added from a datapack with the `ultimatetransport:generator_fuel` recipe type, or from
  KubeJS. Each fuel may override the generator's output rate.
- JEI shows every generator its own fuel list.

### Configurator

- Modes follow Mekanism's: items, fluids, chemicals, energy, source, empty, rotate and wrench.
  Chemicals appear only with Mekanism installed, source only with Ars Nouveau.
- Sneak and scroll to change mode, or hold the mode key for the wheel.
- A click steps the clicked face, a sneaking click steps it back.
- Holding a configuration mode paints the block you are looking at with its own settings, one
  translucent pane per face.
- Rotate points a block at the face you clicked; wrench turns a block on a click and takes it back,
  contents and all, when you sneak.
- Configures **Mekanism machines** through their own side configuration.
- Registered under `#c:tools/wrench`, so machines that ask for a wrench by tag accept it.

### Tubes

- Transport tubes and stations, ported from Tube Transport System: climb in and ride.
- A tube for every direction, coloured by orientation so a junction can be read at a glance.
- Placement preview showing which way the tube you are holding will carry.
- Top speed, render pass, face culling, footstep suppression and the preview are all configurable.

### Compatibility

Mekanism, Ars Nouveau, Flux Networks, Ender IO, AE2, Camol, FTB Ultimine, Ore Excavation, Ultimate
Avaritia Additions, JEI, Jade, Patchouli and KubeJS. All optional.

### Guide

- A Patchouli guide book covering the whole mod, in English and in Spanish for Spain, Argentina and
  Mexico.

### Fixed before release

- **Facades appear the moment they are placed.** They live in the block entity rather than the block
  state, so the chunk has to be told to redraw; it was being asked in a way the model manager decided
  was not worth acting on, and the facade only showed after a manual chunk reload.
- **Cells could not be overfilled by a creative source.** A bottomless cell always reported itself
  full, so the anti-ping-pong check stopped feeding it after a single tick.
- **The configurator reaches the block before the block does.** Machines that accept wrenches by tag
  were rotating themselves on the first click; the tool now takes the interaction first.
- **Mekanism faces cycle properly.** Writing the data type straight in went through a guard that could
  refuse it silently; the bridge now uses the machine's own increment, as Mekanism's configurator does.
