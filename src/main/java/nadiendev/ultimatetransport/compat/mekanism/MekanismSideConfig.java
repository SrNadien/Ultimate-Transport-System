package nadiendev.ultimatetransport.compat.mekanism;

import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.menu.SideConfigTarget.Option;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MekanismSideConfig {

    private static final Map<DataType, Integer> COLOURS = new EnumMap<>(DataType.class);
    private static final Map<TransmissionType, TransferType> CARGO = new EnumMap<>(TransmissionType.class);

    static {
        COLOURS.put(DataType.NONE, 0x555A62);
        COLOURS.put(DataType.INPUT, 0x4C8FE0);
        COLOURS.put(DataType.INPUT_1, 0x4CC7E0);
        COLOURS.put(DataType.INPUT_2, 0x4C6FE0);
        COLOURS.put(DataType.OUTPUT, 0xE0752D);
        COLOURS.put(DataType.OUTPUT_1, 0xE0A82D);
        COLOURS.put(DataType.OUTPUT_2, 0xE05A2D);
        COLOURS.put(DataType.INPUT_OUTPUT, 0x9C4CE0);
        COLOURS.put(DataType.ENERGY, 0x00E6B4);
        COLOURS.put(DataType.EXTRA, 0xE04C9C);

        CARGO.put(TransmissionType.ENERGY, TransferType.ENERGY);
        CARGO.put(TransmissionType.FLUID, TransferType.FLUID);
        CARGO.put(TransmissionType.CHEMICAL, TransferType.GAS);
        CARGO.put(TransmissionType.ITEM, TransferType.ITEM);
    }

    private MekanismSideConfig() {
    }

    public static boolean supports(BlockEntity blockEntity) {
        return blockEntity instanceof ISideConfiguration machine && !transmissions(machine).isEmpty();
    }

    private static List<TransmissionType> transmissions(ISideConfiguration machine) {
        List<TransmissionType> usable = new ArrayList<>();
        TileComponentConfig config = machine.getConfig();
        if (config == null) {
            return usable;
        }
        for (TransmissionType transmission : config.getTransmissions()) {
            if (CARGO.containsKey(transmission) && config.getConfig(transmission) != null) {
                usable.add(transmission);
            }
        }
        return usable;
    }

    public static TransferType[] cargoes(BlockEntity blockEntity) {
        if (!(blockEntity instanceof ISideConfiguration machine)) {
            return new TransferType[0];
        }
        List<TransmissionType> usable = transmissions(machine);
        TransferType[] cargoes = new TransferType[usable.size()];
        for (int index = 0; index < usable.size(); index++) {
            cargoes[index] = CARGO.get(usable.get(index));
        }
        return cargoes;
    }

    @Nullable
    private static ConfigInfo info(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (!(blockEntity instanceof ISideConfiguration machine)) {
            return null;
        }
        List<TransmissionType> usable = transmissions(machine);
        if (usable.isEmpty()) {
            return null;
        }
        TransmissionType wanted = usable.get(0);
        for (TransmissionType transmission : usable) {
            if (CARGO.get(transmission) == cargo) {
                wanted = transmission;
                break;
            }
        }
        return machine.getConfig().getConfig(wanted);
    }

    @Nullable
    private static TransmissionType transmission(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (!(blockEntity instanceof ISideConfiguration machine)) {
            return null;
        }
        List<TransmissionType> usable = transmissions(machine);
        if (usable.isEmpty()) {
            return null;
        }
        for (TransmissionType candidate : usable) {
            if (CARGO.get(candidate) == cargo) {
                return candidate;
            }
        }
        return usable.get(0);
    }

    private static List<DataType> types(@Nullable ConfigInfo info) {
        List<DataType> supported = new ArrayList<>();
        if (info == null) {
            return supported;
        }
        for (DataType type : DataType.values()) {
            if (info.supports(type)) {
                supported.add(type);
            }
        }
        return supported;
    }

    public static List<Option> options(BlockEntity blockEntity, @Nullable TransferType cargo) {
        List<Option> options = new ArrayList<>();
        for (DataType type : types(info(blockEntity, cargo))) {
            options.add(new Option(type.getTranslationKey(), COLOURS.getOrDefault(type, 0x9E9E9E)));
        }
        return options;
    }

    public static boolean canEject(BlockEntity blockEntity, @Nullable TransferType cargo) {
        ConfigInfo info = info(blockEntity, cargo);
        return info != null && info.canEject();
    }

    public static boolean ejecting(BlockEntity blockEntity, @Nullable TransferType cargo) {
        ConfigInfo info = info(blockEntity, cargo);
        return info != null && info.isEjecting();
    }

    public static void toggleEject(BlockEntity blockEntity, @Nullable TransferType cargo) {
        ConfigInfo info = info(blockEntity, cargo);
        if (info == null || !info.canEject()) {
            return;
        }
        info.setEjecting(!info.isEjecting());
        blockEntity.setChanged();
    }

    private static RelativeSide relative(BlockEntity blockEntity, Direction side) {
        Direction facing = blockEntity instanceof ISideConfiguration machine
                ? machine.getDirection()
                : Direction.NORTH;
        return RelativeSide.fromDirections(facing, side);
    }

    public static boolean enabled(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo) {
        ConfigInfo info = info(blockEntity, cargo);
        return info != null && info.isSideEnabled(relative(blockEntity, side));
    }

    public static int index(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo) {
        ConfigInfo info = info(blockEntity, cargo);
        if (info == null) {
            return 0;
        }
        DataType current = info.getDataType(relative(blockEntity, side));
        List<DataType> supported = types(info);
        int found = supported.indexOf(current);
        return found < 0 ? 0 : found;
    }

    /**
     * Steps the face with the machine's own increment, which is what Mekanism's configurator calls.
     * Writing the type straight in goes through a different guard and can quietly refuse.
     */
    public static boolean cycle(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo,
                                boolean forward) {
        ConfigInfo info = info(blockEntity, cargo);
        TransmissionType transmission = transmission(blockEntity, cargo);
        if (info == null || transmission == null) {
            return false;
        }
        RelativeSide relative = relative(blockEntity, side);
        if (!info.isSideEnabled(relative)) {
            return false;
        }
        DataType before = info.getDataType(relative);
        DataType after = forward ? info.incrementDataType(relative) : info.decrementDataType(relative);
        if (after == before) {
            return false;
        }
        ((ISideConfiguration) blockEntity).getConfig().sideChanged(transmission, relative);
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(),
                    blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
        return true;
    }

    public static void apply(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo, int option) {
        ConfigInfo info = info(blockEntity, cargo);
        TransmissionType transmission = transmission(blockEntity, cargo);
        if (info == null || transmission == null) {
            return;
        }
        List<DataType> supported = types(info);
        if (option < 0 || option >= supported.size()) {
            return;
        }
        RelativeSide relative = relative(blockEntity, side);
        if (!info.isSideEnabled(relative) || !info.setDataType(supported.get(option), relative)) {
            return;
        }
        ((ISideConfiguration) blockEntity).getConfig().sideChanged(transmission, relative);
        blockEntity.setChanged();
    }
}
