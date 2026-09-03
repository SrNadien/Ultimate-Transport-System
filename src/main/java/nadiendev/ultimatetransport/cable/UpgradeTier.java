package nadiendev.ultimatetransport.cable;

import nadiendev.ultimatetransport.config.UTServerConfig;
import net.minecraft.util.StringRepresentable;

/**
 * What a side can move per operation. A side with no upgrade runs at {@link #NONE}; slotting an
 * upgrade item into that side raises every rate at once.
 */
public enum UpgradeTier implements StringRepresentable {

    NONE("none", 256, 50, 200, 4, 20, false, false, false, 0x9E9E9E),
    BASIC("basic", 1_024, 100, 400, 8, 15, true, false, false, 0xB87333),
    IMPROVED("improved", 8_192, 500, 2_000, 16, 10, true, true, false, 0xC0C0C0),
    ADVANCED("advanced", 32_768, 2_000, 8_000, 32, 5, true, true, true, 0xFFD24A),
    ULTIMATE("ultimate", 131_072, 10_000, 40_000, 64, 1, true, true, true, 0x37D5D5),
    INFINITY("infinity", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 64, 1, true, true, true, 0xF06BD8);

    public static final UpgradeTier[] VALUES = values();

    private final String name;
    private final int energyRate;
    private final int fluidRate;
    private final int gasRate;
    private final int itemCount;
    private final int itemInterval;
    private final boolean redstone;
    private final boolean distribution;
    private final boolean filters;
    private final int colour;

    UpgradeTier(String name, int energyRate, int fluidRate, int gasRate, int itemCount, int itemInterval,
                boolean redstone, boolean distribution, boolean filters, int colour) {
        this.name = name;
        this.energyRate = energyRate;
        this.fluidRate = fluidRate;
        this.gasRate = gasRate;
        this.itemCount = itemCount;
        this.itemInterval = itemInterval;
        this.redstone = redstone;
        this.distribution = distribution;
        this.filters = filters;
        this.colour = colour;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int defaultEnergyRate() {
        return energyRate;
    }

    public int defaultFluidRate() {
        return fluidRate;
    }

    public int defaultGasRate() {
        return gasRate;
    }

    public int defaultItemCount() {
        return itemCount;
    }

    public int defaultItemInterval() {
        return itemInterval;
    }

    /** FE moved per tick, per extracting side. */
    public int energyRate() {
        return UTServerConfig.energy(this);
    }

    /** Millibuckets moved per tick, per extracting side. */
    public int fluidRate() {
        return UTServerConfig.fluid(this);
    }

    /** Chemical units moved per tick, which run on their own scale. */
    public int gasRate() {
        return UTServerConfig.gas(this);
    }

    /** Items moved per operation. */
    public int itemCount() {
        return UTServerConfig.itemCount(this);
    }

    /** Ticks between item operations. */
    public int itemInterval() {
        return UTServerConfig.itemInterval(this);
    }

    /** Whether the face may be given a redstone mode. */
    public boolean canRedstone() {
        return redstone;
    }

    /** Whether the face may be given a distribution mode. */
    public boolean canDistribute() {
        return distribution;
    }

    /** Whether the face may be given filter rules. There is no cap on how many. */
    public boolean canFilter() {
        return filters;
    }

    public boolean canConfigure() {
        return this != NONE;
    }

    public int colour() {
        return colour;
    }

    public String itemName() {
        return name + "_upgrade";
    }

    public String translationKey() {
        return "ultimatetransport.upgrade." + name;
    }

    public static UpgradeTier byName(String name) {
        for (UpgradeTier tier : VALUES) {
            if (tier.name.equals(name)) {
                return tier;
            }
        }
        return NONE;
    }
}
