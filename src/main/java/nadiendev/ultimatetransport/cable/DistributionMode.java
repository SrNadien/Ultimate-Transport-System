package nadiendev.ultimatetransport.cable;

import net.minecraft.util.StringRepresentable;

public enum DistributionMode implements StringRepresentable {

    /** Fill the closest destination first. */
    NEAREST("nearest"),
    /** Fill the furthest destination first. */
    FURTHEST("furthest"),
    /** Spread evenly, resuming after the destination that was served last. */
    ROUND_ROBIN("round_robin"),
    /** Pick a destination at random each operation. */
    RANDOM("random");

    public static final DistributionMode[] VALUES = values();

    private final String name;

    DistributionMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return "ultimatetransport.distribution_mode." + name;
    }

    public DistributionMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static DistributionMode byName(String name) {
        for (DistributionMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return NEAREST;
    }
}
