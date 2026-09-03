package nadiendev.ultimatetransport.cable;

import net.minecraft.util.StringRepresentable;

public enum ConnectionMode implements StringRepresentable {

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
        return this == INSERT ? EXTRACT : INSERT;
    }

    public ConnectionMode previous() {
        return next();
    }

    public static ConnectionMode byName(String name) {
        return EXTRACT.name.equals(name) ? EXTRACT : INSERT;
    }
}
