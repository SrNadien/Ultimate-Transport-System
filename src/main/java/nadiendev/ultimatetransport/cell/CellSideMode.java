package nadiendev.ultimatetransport.cell;

import net.minecraft.util.StringRepresentable;

public enum CellSideMode implements StringRepresentable {

    DISABLED("disabled", 0x808080),
    INPUT("input", 0x4CAF50),
    OUTPUT("output", 0xE0752D),
    BOTH("both", 0x4AA3E0);

    public static final CellSideMode[] VALUES = values();

    private final String name;
    private final int colour;

    CellSideMode(String name, int colour) {
        this.name = name;
        this.colour = colour;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int colour() {
        return colour;
    }

    public String translationKey() {
        return "ultimatetransport.cell_side." + name;
    }

    public boolean input() {
        return this == INPUT || this == BOTH;
    }

    public boolean output() {
        return this == OUTPUT || this == BOTH;
    }

    public CellSideMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public CellSideMode previous() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }

    public static CellSideMode byName(String name) {
        for (CellSideMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return BOTH;
    }
}
