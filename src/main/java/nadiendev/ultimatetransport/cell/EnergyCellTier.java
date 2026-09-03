package nadiendev.ultimatetransport.cell;

import net.minecraft.util.StringRepresentable;

/**
 * The storage ladder. Capacity quadruples each step and throughput is a hundredth of capacity, so a
 * bigger cell is also a faster one, up to two steps that break the pattern on purpose: the eleventh
 * never fills, and the creative one never empties.
 */
public enum EnergyCellTier implements StringRepresentable {

    TIER_1(1, "energy_cell_1", 1_000_000L, 0xB87333, Kind.NORMAL),
    TIER_2(2, "energy_cell_2", 4_000_000L, 0xD8D8D8, Kind.NORMAL),
    TIER_3(3, "energy_cell_3", 16_000_000L, 0xFFD24A, Kind.NORMAL),
    TIER_4(4, "energy_cell_4", 64_000_000L, 0xE03030, Kind.NORMAL),
    TIER_5(5, "energy_cell_5", 256_000_000L, 0x4FC3F7, Kind.NORMAL),
    TIER_6(6, "energy_cell_6", 1_024_000_000L, 0x3ED66A, Kind.NORMAL),
    TIER_7(7, "energy_cell_7", 4_096_000_000L, 0xB07A4A, Kind.NORMAL),
    TIER_8(8, "energy_cell_8", 16_384_000_000L, 0x7A5F86, Kind.NORMAL),
    TIER_9(9, "energy_cell_9", 65_536_000_000L, 0x1FD6D6, Kind.NORMAL),
    TIER_10(10, "energy_cell_10", 262_144_000_000L, 0xFFF2A0, Kind.NORMAL),
    /** Takes everything it is given and never runs out of room. */
    TIER_11(11, "energy_cell_11", Long.MAX_VALUE, 0xE8F0FF, Kind.BOTTOMLESS),
    /** Always full, whatever you take. Creative only. */
    CREATIVE(12, "creative_energy_cell", Long.MAX_VALUE, 0xFF5AE0, Kind.CREATIVE);

    public static final EnergyCellTier[] VALUES = values();

    private enum Kind {
        NORMAL, BOTTOMLESS, CREATIVE
    }

    private final int level;
    private final String name;
    private final long capacity;
    private final int transfer;
    private final int colour;
    private final Kind kind;

    EnergyCellTier(int level, String name, long capacity, int colour, Kind kind) {
        this.level = level;
        this.name = name;
        this.capacity = capacity;
        this.transfer = kind == Kind.NORMAL
                ? (int) Math.min(capacity / 100L, Integer.MAX_VALUE)
                : Integer.MAX_VALUE;
        this.colour = colour;
        this.kind = kind;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int level() {
        return level;
    }

    public long capacity() {
        return capacity;
    }

    public int transfer() {
        return transfer;
    }

    public int colour() {
        return colour;
    }

    public String blockName() {
        return name;
    }

    /** True when the cell has no ceiling: it accepts whatever arrives. */
    public boolean bottomless() {
        return kind != Kind.NORMAL;
    }

    /** True when the cell is an endless source and taking from it costs nothing. */
    public boolean creative() {
        return kind == Kind.CREATIVE;
    }

    /** The rungs a player crafts through, which is everything below the creative cell. */
    public static EnergyCellTier byLevel(int level) {
        return VALUES[Math.clamp(level - 1, 0, VALUES.length - 1)];
    }
}
