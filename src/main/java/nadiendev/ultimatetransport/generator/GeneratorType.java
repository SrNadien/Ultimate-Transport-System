package nadiendev.ultimatetransport.generator;

import nadiendev.ultimatetransport.config.UTServerConfig;
import nadiendev.ultimatetransport.config.UTServerConfig;
import net.minecraft.util.StringRepresentable;

public enum GeneratorType implements StringRepresentable {
    FUEL("fuel_generator", 40, 80_000, 400, 0xE07A2D),
    LAVA("lava_generator", 90, 120_000, 800, 0xE04A1E),
    SOLAR("solar_generator", 12, 40_000, 200, 0x3FA9E0),
    NETHER_STAR("nether_star_generator", 2_000, 4_000_000, 20_000, 0xF2F0D8);

    public static final GeneratorType[] VALUES = values();

    private final String name;
    private final int rate;
    private final int capacity;
    private final int transfer;
    private final int colour;

    GeneratorType(String name, int rate, int capacity, int transfer, int colour) {
        this.name = name;
        this.rate = rate;
        this.capacity = capacity;
        this.transfer = transfer;
        this.colour = colour;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String blockName() {
        return name;
    }

    public int defaultRate() {
        return rate;
    }

    public int defaultCapacity() {
        return capacity;
    }

    public int defaultTransfer() {
        return transfer;
    }

    public int rate() {
        return UTServerConfig.rate(this);
    }

    public int capacity() {
        return UTServerConfig.capacity(this);
    }

    public int transfer() {
        return UTServerConfig.transfer(this);
    }

    public int colour() {
        return colour;
    }

    public boolean consumesItems() {
        return this != SOLAR;
    }

    public int burnTicks() {
        return switch (this) {
            case LAVA -> 1_400;
            case NETHER_STAR -> 20_000;
            default -> 0;
        };
    }

    public String translationKey() {
        return "block.ultimatetransport." + name;
    }

    public static GeneratorType byName(String name) {
        for (GeneratorType type : VALUES) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return FUEL;
    }
}
