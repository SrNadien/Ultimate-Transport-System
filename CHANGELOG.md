# Changelog
## [1.0.5]

### Added

- **Battery faces carry a display.** Sneak and right click a battery on one of its four upright
  faces with an empty hand and that face steps through nothing, the charge bar, and an input and
  output readout. A face left on nothing is plain casing, so a wall of batteries shows one bar where
  you put it instead of a bar on every block. Faces set to the bar and stacked on top of each other
  are read as one column: the charge fills the run from its floor upwards, so a stack carries a
  single bar through it whatever else is joined to it.
- **The input and output face names what the whole bank is taking in and giving out**, averaged over
  the last half second, in green when it is gaining and red when it is losing.

- **Filter rules live on the upgrade, not in the cable.** Pulling an upgrade out of a face takes its
  rules with it, and putting it anywhere else brings them back, so a filter worked out once can be
  copied around the network by moving the item. The upgrade says in its tooltip how many rules it
  carries and whether they let things through or hold them back. Cables from an older world hand
  their rules to the upgrade already sitting in the face the first time they load.
- **Upgrades go in with a sneaking right click, as in Pipez.** Holding an upgrade and sneak-clicking
  the face of a cable that touches a container fits it there, without opening anything. A face that
  already holds a different upgrade gives the old one back to the hand that swapped it, and one that
  already holds the same is left alone. On a universal cable a single click fits the upgrade to every
  cargo that face carries, one item per cargo, for as long as the hand has them.

### Fixed

- **A machine that only pushes can now feed a cable.** A cable offered itself to its neighbours only
  after it had found something to read on the other side, so a machine that exposes no inventory of
  its own and simply shoves what it makes into whatever is next to it found nothing to shove into.
  Advanced AE's quantum crafter is one: it never registers an item handler, so nothing can reach into
  it, and pushing to its neighbours is the only way out. A cable now offers itself on every face
  except the ones set to be left alone, whatever the block on the other side does.

- **Jade shows what a battery bank really holds.** Its own energy line reads the battery through the
  same int-bound capability, so it was naming figures cut down to fit rather than the real ones. That
  line is now taken out and replaced with one carrying the true charge and the true room, filled to
  the real proportion.
- **A meter reading a large bank no longer shows it nearly full when it is nearly empty.** Energy is
  handed around in ints, and a bank past the sixth rung holds more than an int can count, so the
  charge and the room were each cut down to the largest int separately: 1.98G held in 5.24G of room
  was reported as 1.98G of 2.14G, which reads as ninety per cent rather than thirty eight. Both
  figures are now reported as the same share of that limit that the real ones are of each other, so
  anything reading the battery from outside sees the right proportion. The exact numbers were always
  right in the tooltip and still are.

- **A rider no longer gets caught partway down a tube.** The walls a tube puts up against a rider
  were being decided by the same rule that decides which faces to draw, so a tube standing next to
  one pointing another way dropped its wall on that side. The wall in the tube below kept its own,
  and its top edge became a ledge: anyone drifting into that corner on the way down landed on it and
  stopped, short of the station and with the tube still pushing at them. A tube now walls every side
  it does not carry along, whatever its neighbours point at, so a shaft is smooth from top to bottom.
  Nothing that could be ridden through before is closed: a tube is only ever entered and left along
  the way it points, and the floor a run drops through is unchanged.

## [1.0.4]

### Changed

- **A cable face that keeps coming up empty goes quiet for two seconds.** With nowhere to put what
  it carries, a cable walked its whole list of destinations on every attempt and got the same
  answer every time. On a server where a mod hands out extra ticks that pointless walk ran thousands
  of times a second and the tick loop drowned in it. Ten empty attempts in a row on a face now put
  that face to sleep, and the wait is counted against the clock on the wall, not against ticks, so
  no amount of extra ticks can shorten it. The first thing actually carried wakes the face and
  clears the count, and so does any block placed or broken on the network, so a chest set down next
  to a sleeping cable is served at once instead of after the wait.

## [1.0.3]

### Fixed

- **A tube coming down into a station no longer ends at a ceiling.** Stations refuse to throw a
  rider up into a tube that points back down, because the tube would only push them in again. That
  refusal was also deciding whether the station had a roof, so a descending tube ran into solid
  block and nobody, and nothing, came out of it. The roof is now open under any tube at all, and the
  refusal only decides whether the station pushes, which is the one thing it was meant to stop. The
  floor is unchanged: it still opens only for a tube heading down, so a rider standing in a station
  is not bounced by a tube pushing up from below.

## [1.0.2]

### Fixed

- **Cables no longer crash the server passing a stack back and forth with another mod’s pipe.** A
  Mekanism logistical transporter answers an insert by asking its own network which neighbours can
  take the stack, and a cable is one of those neighbours: the cable offered the stack to the
  transporter, the transporter turned around and offered it to the cable, and the two handed it back
  and forth until the thread ran out of room and the server went down. A cable now refuses anything
  that reaches it while it is already pushing, and a single transfer gives up after eight pipes, so
  the loop ends on the first bounce. The same guard covers energy, fluids and chemicals, which could
  circle the same way.

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
