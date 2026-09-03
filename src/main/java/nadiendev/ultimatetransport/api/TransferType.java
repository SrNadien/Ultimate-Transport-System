package nadiendev.ultimatetransport.api;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TransferType implements StringRepresentable {

    ENERGY("energy", 0x00E6B4),
    FLUID("fluid", 0x3A7BD5),
    ITEM("item", 0xC8AA46),
    GAS("gas", 0xB45AD2),
    SOURCE("source", 0x8CD9FF),
    UNIVERSAL("universal", 0xE0E0E0);

    public static final TransferType[] VALUES = values();

    public static final Codec<TransferType> CODEC = StringRepresentable.fromEnum(TransferType::values);

    private final String name;
    private final int tint;

    TransferType(String name, int tint) {
        this.name = name;
        this.tint = tint;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int tint() {
        return tint;
    }

    public String cableName() {
        return name + "_cable";
    }

    public String translationKey() {
        return "ultimatetransport.transfer_type." + name;
    }

    /**
     * Two cables join into the same network when they move the same thing, and a universal cable
     * joins every network it touches.
     */
    public boolean connectsTo(TransferType other) {
        return this == other || this == UNIVERSAL || other == UNIVERSAL;
    }

    public boolean carries(TransferType content) {
        return this == UNIVERSAL || this == content;
    }

    private static final TransferType[] CARGO = {ENERGY, FLUID, ITEM, GAS, SOURCE};

    /**
     * The kinds this cable moves, each of which gets its own face settings. A universal cable answers
     * with all of them, so one face can insert power and pull items at the same time.
     */
    public TransferType[] carried() {
        return this == UNIVERSAL ? CARGO : new TransferType[]{this};
    }
}
