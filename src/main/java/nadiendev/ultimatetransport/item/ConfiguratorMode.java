package nadiendev.ultimatetransport.item;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.compat.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum ConfiguratorMode implements StringRepresentable {

    CONFIGURATE_ITEM("configurate_item", TransferType.ITEM),
    CONFIGURATE_FLUID("configurate_fluid", TransferType.FLUID),
    CONFIGURATE_GAS("configurate_gas", TransferType.GAS),
    CONFIGURATE_ENERGY("configurate_energy", TransferType.ENERGY),
    CONFIGURATE_SOURCE("configurate_source", TransferType.SOURCE),
    EMPTY("empty", null, 0xE04C4C),
    ROTATE("rotate", null, 0x4C8FE0),
    WRENCH("wrench", null, 0xE0A82D);

    public static final ConfiguratorMode[] VALUES = values();

    private final String name;
    private final TransferType cargo;
    private final int colour;

    ConfiguratorMode(String name, TransferType cargo) {
        this(name, cargo, cargo.tint());
    }

    ConfiguratorMode(String name, @Nullable TransferType cargo, int colour) {
        this.name = name;
        this.cargo = cargo;
        this.colour = colour;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Nullable
    public TransferType cargo() {
        return cargo;
    }

    public boolean configurating() {
        return cargo != null;
    }

    public int colour() {
        return colour;
    }

    /** A mode only turns up in the cycle when the mod that gives it meaning is installed. */
    public boolean available() {
        if (cargo == TransferType.SOURCE) {
            return ModIds.loaded(ModIds.ARS_NOUVEAU);
        }
        if (cargo == TransferType.GAS) {
            return ModIds.loaded(ModIds.MEKANISM);
        }
        return true;
    }

    public int tint() {
        if (this == CONFIGURATE_ITEM) {
            return 0xFFFFFFFF;
        }
        int r = 0xFF - (0xFF - ((colour >> 16) & 0xFF)) * 7 / 10;
        int g = 0xFF - (0xFF - ((colour >> 8) & 0xFF)) * 7 / 10;
        int b = 0xFF - (0xFF - (colour & 0xFF)) * 7 / 10;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public String translationKey() {
        return "ultimatetransport.configurator_mode." + name;
    }

    public Component label() {
        return Component.translatable(translationKey()).withStyle(style -> style.withColor(colour));
    }

    public ConfiguratorMode next() {
        return step(1);
    }

    public ConfiguratorMode previous() {
        return step(-1);
    }

    private ConfiguratorMode step(int direction) {
        ConfiguratorMode mode = this;
        for (int guard = 0; guard < VALUES.length; guard++) {
            mode = VALUES[Math.floorMod(mode.ordinal() + direction, VALUES.length)];
            if (mode.available()) {
                return mode;
            }
        }
        return this;
    }

    public static ConfiguratorMode byName(String name) {
        for (ConfiguratorMode mode : VALUES) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return CONFIGURATE_ITEM;
    }
}
