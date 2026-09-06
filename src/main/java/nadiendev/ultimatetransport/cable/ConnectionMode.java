package nadiendev.ultimatetransport.cable;

import net.minecraft.util.StringRepresentable;

public enum ConnectionMode implements StringRepresentable {

    /** The face is left alone: the cable does not reach for the block on it at all. */
    NONE("none", 0x9E9E9E),
    INSERT("insert", 0x4CAF50),
    EXTRACT("extract", 0xE0752D);

    public static final ConnectionMode[] VALUES = values();

    private final String name;
    private final int colour;

    ConnectionMode(String name, int colour) {
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
        return "ultimatetransport.connection_mode." + name;
    }

    public ConnectionMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public ConnectionMode previous() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }

    public static ConnectionMode byName(String name) {
        for (ConnectionMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return INSERT;
    }
}
