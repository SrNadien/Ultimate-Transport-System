package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.ConnectionMode;
import nadiendev.ultimatetransport.cell.CellSideMode;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.compat.mekanism.ForeignSideConfig;
import nadiendev.ultimatetransport.generator.GeneratorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SideConfigTarget {
    public record Option(String translationKey, int colour) {
    }

    private static final List<Option> CELL_OPTIONS = List.of(
            new Option(CellSideMode.DISABLED.translationKey(), CellSideMode.DISABLED.colour()),
            new Option(CellSideMode.INPUT.translationKey(), CellSideMode.INPUT.colour()),
            new Option(CellSideMode.OUTPUT.translationKey(), CellSideMode.OUTPUT.colour()),
            new Option(CellSideMode.BOTH.translationKey(), CellSideMode.BOTH.colour()));

    private static final List<Option> GENERATOR_OPTIONS = List.of(
            new Option(CellSideMode.DISABLED.translationKey(), CellSideMode.DISABLED.colour()),
            new Option(CellSideMode.OUTPUT.translationKey(), CellSideMode.OUTPUT.colour()));

    private static final List<Option> CABLE_OPTIONS = List.of(
            new Option(ConnectionMode.INSERT.translationKey(), ConnectionMode.INSERT.colour()),
            new Option(ConnectionMode.EXTRACT.translationKey(), ConnectionMode.EXTRACT.colour()));

    private SideConfigTarget() {
    }

    public static boolean supports(BlockEntity blockEntity) {
        return blockEntity instanceof EnergyCellBlockEntity
                || blockEntity instanceof GeneratorBlockEntity
                || blockEntity instanceof CableBlockEntity
                || ForeignSideConfig.supports(blockEntity);
    }

    public static boolean foreign(BlockEntity blockEntity) {
        return !(blockEntity instanceof EnergyCellBlockEntity)
                && !(blockEntity instanceof GeneratorBlockEntity)
                && !(blockEntity instanceof CableBlockEntity)
                && ForeignSideConfig.supports(blockEntity);
    }

    public static TransferType[] cargoes(BlockEntity blockEntity) {
        if (blockEntity instanceof CableBlockEntity cable) {
            return cable.type().carried();
        }
        if (foreign(blockEntity)) {
            return ForeignSideConfig.cargoes(blockEntity);
        }
        return new TransferType[0];
    }

    @Nullable
    private static TransferType resolve(CableBlockEntity cable, @Nullable TransferType cargo) {
        return cargo != null && cable.type().carries(cargo) ? cargo : cable.primary();
    }

    public static List<Option> options(BlockEntity blockEntity) {
        return options(blockEntity, null);
    }

    public static List<Option> options(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (blockEntity instanceof GeneratorBlockEntity) {
            return GENERATOR_OPTIONS;
        }
        if (blockEntity instanceof CableBlockEntity) {
            return CABLE_OPTIONS;
        }
        if (blockEntity instanceof EnergyCellBlockEntity) {
            return CELL_OPTIONS;
        }
        return ForeignSideConfig.options(blockEntity, cargo);
    }

    /** Whether this block has anything to say about that cargo at all. */
    public static boolean handles(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (blockEntity instanceof CableBlockEntity cable) {
            return cargo == null || cable.type().carries(cargo);
        }
        if (blockEntity instanceof EnergyCellBlockEntity || blockEntity instanceof GeneratorBlockEntity) {
            return cargo == null || cargo == TransferType.ENERGY;
        }
        if (!foreign(blockEntity)) {
            return false;
        }
        for (TransferType carried : ForeignSideConfig.cargoes(blockEntity)) {
            if (carried == cargo) {
                return true;
            }
        }
        return false;
    }

    public static boolean enabled(BlockEntity blockEntity, Direction side) {
        return enabled(blockEntity, side, null);
    }

    public static boolean enabled(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo) {
        if (blockEntity instanceof CableBlockEntity cable) {
            return cable.container(side);
        }
        if (foreign(blockEntity)) {
            return ForeignSideConfig.enabled(blockEntity, side, cargo);
        }
        return true;
    }

    public static int index(BlockEntity blockEntity, Direction side) {
        return index(blockEntity, side, null);
    }

    public static int index(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo) {
        if (blockEntity instanceof GeneratorBlockEntity generator) {
            return generator.side(side).output() ? 1 : 0;
        }
        if (blockEntity instanceof CableBlockEntity cable) {
            return cable.config(side, resolve(cable, cargo)).mode().ordinal();
        }
        if (blockEntity instanceof EnergyCellBlockEntity cell) {
            return cell.side(side).ordinal();
        }
        return ForeignSideConfig.index(blockEntity, side, cargo);
    }

    /** Whether the block can be told to push what it holds into its neighbours on its own. */
    public static boolean ejects(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (blockEntity instanceof EnergyCellBlockEntity || blockEntity instanceof GeneratorBlockEntity) {
            return true;
        }
        return foreign(blockEntity) && ForeignSideConfig.canEject(blockEntity, cargo);
    }

    public static boolean ejecting(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (blockEntity instanceof EnergyCellBlockEntity cell) {
            return cell.autoEject();
        }
        if (blockEntity instanceof GeneratorBlockEntity generator) {
            return generator.autoEject();
        }
        return foreign(blockEntity) && ForeignSideConfig.ejecting(blockEntity, cargo);
    }

    public static void toggleEject(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (blockEntity instanceof EnergyCellBlockEntity cell) {
            cell.setAutoEject(!cell.autoEject());
        } else if (blockEntity instanceof GeneratorBlockEntity generator) {
            generator.setAutoEject(!generator.autoEject());
        } else if (foreign(blockEntity)) {
            ForeignSideConfig.toggleEject(blockEntity, cargo);
        }
    }

    public static void cycle(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo, boolean forward) {
        if (!enabled(blockEntity, side, cargo)) {
            return;
        }
        if (foreign(blockEntity)) {
            ForeignSideConfig.cycle(blockEntity, side, cargo, forward);
            return;
        }
        int count = options(blockEntity, cargo).size();
        if (count <= 0) {
            return;
        }
        int next = Math.floorMod(index(blockEntity, side, cargo) + (forward ? 1 : -1), count);
        apply(blockEntity, side, cargo, next);
    }

    public static void cycleAll(BlockEntity blockEntity, @Nullable TransferType cargo, boolean forward) {
        for (Direction side : Direction.values()) {
            cycle(blockEntity, side, cargo, forward);
        }
    }

    public static void clearAll(BlockEntity blockEntity, @Nullable TransferType cargo) {
        for (Direction side : Direction.values()) {
            if (enabled(blockEntity, side, cargo)) {
                apply(blockEntity, side, cargo, 0);
            }
        }
    }

    public static void apply(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo, int option) {
        if (blockEntity instanceof GeneratorBlockEntity generator) {
            generator.setSide(side, option == 1 ? CellSideMode.OUTPUT : CellSideMode.DISABLED);
        } else if (blockEntity instanceof CableBlockEntity cable) {
            cable.setSideMode(side, resolve(cable, cargo), ConnectionMode.VALUES[option]);
            cable.onConfigChanged();
        } else if (blockEntity instanceof EnergyCellBlockEntity cell) {
            cell.setSide(side, CellSideMode.VALUES[option]);
        } else {
            ForeignSideConfig.apply(blockEntity, side, cargo, option);
        }
    }
}
