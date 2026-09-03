package nadiendev.ultimatetransport.cable;

import net.minecraft.util.StringRepresentable;

public enum RedstoneMode implements StringRepresentable {

    IGNORED("ignored"),
    OFF_WHEN_POWERED("off_when_powered"),
    ON_WHEN_POWERED("on_when_powered"),
    ALWAYS_OFF("always_off");

    public static final RedstoneMode[] VALUES = values();

    private final String name;

    RedstoneMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return "ultimatetransport.redstone_mode." + name;
    }

    public boolean allows(boolean powered) {
        return switch (this) {
            case IGNORED -> true;
            case OFF_WHEN_POWERED -> !powered;
            case ON_WHEN_POWERED -> powered;
            case ALWAYS_OFF -> false;
        };
    }

    public RedstoneMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static RedstoneMode byName(String name) {
        for (RedstoneMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return IGNORED;
    }
}
