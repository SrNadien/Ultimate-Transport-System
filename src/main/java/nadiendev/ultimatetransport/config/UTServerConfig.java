package nadiendev.ultimatetransport.config;

import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.generator.GeneratorType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.Map;

public final class UTServerConfig {
    public record Rates(ModConfigSpec.IntValue energy,
                        ModConfigSpec.IntValue fluid,
                        ModConfigSpec.IntValue gas,
                        ModConfigSpec.IntValue itemCount,
                        ModConfigSpec.IntValue itemInterval) {
    }

    public record Machine(ModConfigSpec.IntValue rate,
                          ModConfigSpec.IntValue capacity,
                          ModConfigSpec.IntValue transfer) {
    }

    public static final Map<UpgradeTier, Rates> RATES = new EnumMap<>(UpgradeTier.class);
    public static final Map<GeneratorType, Machine> GENERATORS = new EnumMap<>(GeneratorType.class);
    public static ModConfigSpec.IntValue WITHER_RADIUS;
    public static ModConfigSpec.IntValue WITHER_SECONDS;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("What each upgrade lets one cable face move. The defaults match Pipez.");
        builder.push("upgrades");
        for (UpgradeTier tier : UpgradeTier.VALUES) {
            builder.push(tier.getSerializedName());
            RATES.put(tier, new Rates(
                    builder.comment("FE per tick").defineInRange("energy", tier.defaultEnergyRate(), 1, Integer.MAX_VALUE),
                    builder.comment("Millibuckets per tick").defineInRange("fluid", tier.defaultFluidRate(), 1, Integer.MAX_VALUE),
                    builder.comment("Chemical units per tick").defineInRange("gas", tier.defaultGasRate(), 1, Integer.MAX_VALUE),
                    builder.comment("Items moved per operation").defineInRange("item_amount", tier.defaultItemCount(), 1, Integer.MAX_VALUE),
                    builder.comment("Ticks between item operations; 1 means every tick")
                            .defineInRange("item_speed", tier.defaultItemInterval(), 1, Integer.MAX_VALUE)));
            builder.pop();
        }
        builder.pop();

        builder.comment("What each generator makes, holds and hands out.");
        builder.push("generators");
        for (GeneratorType type : GeneratorType.VALUES) {
            builder.push(type.getSerializedName());
            GENERATORS.put(type, new Machine(
                    builder.comment("FE produced per tick while running")
                            .defineInRange("rate", type.defaultRate(), 0, Integer.MAX_VALUE),
                    builder.comment("FE the buffer holds")
                            .defineInRange("capacity", type.defaultCapacity(), 1, Integer.MAX_VALUE),
                    builder.comment("FE per tick pushed into each neighbour")
                            .defineInRange("transfer", type.defaultTransfer(), 1, Integer.MAX_VALUE)));
            builder.pop();
        }
        WITHER_RADIUS = builder
                .comment("How far the nether star generator withers things while it runs. 0 turns it off.")
                .defineInRange("nether_star_wither_radius", 4, 0, 32);
        WITHER_SECONDS = builder
                .comment("How long the wither it applies lasts, in seconds.")
                .defineInRange("nether_star_wither_seconds", 4, 1, 60);
        builder.pop();

        SPEC = builder.build();
    }

    private UTServerConfig() {
    }

    private static int read(UpgradeTier tier, java.util.function.Function<Rates, ModConfigSpec.IntValue> pick, int fallback) {
        Rates rates = RATES.get(tier);
        if (rates == null || !SPEC.isLoaded()) {
            return fallback;
        }
        return pick.apply(rates).get();
    }

    public static int energy(UpgradeTier tier) {
        return read(tier, Rates::energy, tier.defaultEnergyRate());
    }

    public static int fluid(UpgradeTier tier) {
        return read(tier, Rates::fluid, tier.defaultFluidRate());
    }

    public static int gas(UpgradeTier tier) {
        return read(tier, Rates::gas, tier.defaultGasRate());
    }

    public static int itemCount(UpgradeTier tier) {
        return read(tier, Rates::itemCount, tier.defaultItemCount());
    }

    public static int itemInterval(UpgradeTier tier) {
        return read(tier, Rates::itemInterval, tier.defaultItemInterval());
    }

    private static int machine(GeneratorType type,
                               java.util.function.Function<Machine, ModConfigSpec.IntValue> pick, int fallback) {
        Machine machine = GENERATORS.get(type);
        if (machine == null || !SPEC.isLoaded()) {
            return fallback;
        }
        return pick.apply(machine).get();
    }

    public static int rate(GeneratorType type) {
        return machine(type, Machine::rate, type.defaultRate());
    }

    public static int capacity(GeneratorType type) {
        return machine(type, Machine::capacity, type.defaultCapacity());
    }

    public static int transfer(GeneratorType type) {
        return machine(type, Machine::transfer, type.defaultTransfer());
    }

    public static int witherRadius() {
        return WITHER_RADIUS == null || !SPEC.isLoaded() ? 4 : WITHER_RADIUS.get();
    }

    public static int witherSeconds() {
        return WITHER_SECONDS == null || !SPEC.isLoaded() ? 4 : WITHER_SECONDS.get();
    }
}
