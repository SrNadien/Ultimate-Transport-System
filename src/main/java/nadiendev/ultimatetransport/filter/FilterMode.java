package nadiendev.ultimatetransport.filter;

import net.minecraft.util.StringRepresentable;

public enum FilterMode implements StringRepresentable {

    /** Only what a filter matches may pass. */
    WHITELIST("whitelist"),
    /** Everything may pass except what a filter matches. */
    BLACKLIST("blacklist");

    public static final FilterMode[] VALUES = values();

    private final String name;

    FilterMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return "ultimatetransport.filter_mode." + name;
    }

    public FilterMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static FilterMode byName(String name) {
        for (FilterMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return WHITELIST;
    }
}
