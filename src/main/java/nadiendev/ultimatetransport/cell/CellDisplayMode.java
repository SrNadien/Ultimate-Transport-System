package nadiendev.ultimatetransport.cell;

import net.minecraft.util.StringRepresentable;

public enum CellDisplayMode implements StringRepresentable {

    NONE("none"),
    BAR("bar"),
    IO("io");

    public static final CellDisplayMode[] VALUES = values();

    private final String name;

    CellDisplayMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return "ultimatetransport.cell_display." + name;
    }

    public CellDisplayMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static CellDisplayMode byName(String name) {
        for (CellDisplayMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return NONE;
    }
}
