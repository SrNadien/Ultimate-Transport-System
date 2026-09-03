package nadiendev.ultimatetransport.compat.mekanism;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.compat.ModIds;
import nadiendev.ultimatetransport.menu.SideConfigTarget.Option;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ForeignSideConfig {

    private static Boolean available;

    private ForeignSideConfig() {
    }

    private static boolean available() {
        if (available == null) {
            available = ModIds.loaded(ModIds.MEKANISM) && present();
        }
        return available;
    }

    private static boolean present() {
        try {
            Class.forName("mekanism.common.tile.interfaces.ISideConfiguration");
            return true;
        } catch (Throwable missing) {
            return false;
        }
    }

    public static boolean supports(@Nullable BlockEntity blockEntity) {
        return blockEntity != null && available() && MekanismSideConfig.supports(blockEntity);
    }

    public static TransferType[] cargoes(BlockEntity blockEntity) {
        return available() ? MekanismSideConfig.cargoes(blockEntity) : new TransferType[0];
    }

    public static List<Option> options(BlockEntity blockEntity, @Nullable TransferType cargo) {
        return available() ? MekanismSideConfig.options(blockEntity, cargo) : List.of();
    }

    public static boolean enabled(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo) {
        return available() && MekanismSideConfig.enabled(blockEntity, side, cargo);
    }

    public static int index(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo) {
        return available() ? MekanismSideConfig.index(blockEntity, side, cargo) : 0;
    }

    public static void apply(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo, int option) {
        if (available()) {
            MekanismSideConfig.apply(blockEntity, side, cargo, option);
        }
    }

    public static boolean cycle(BlockEntity blockEntity, Direction side, @Nullable TransferType cargo,
                                boolean forward) {
        return available() && MekanismSideConfig.cycle(blockEntity, side, cargo, forward);
    }

    public static boolean canEject(BlockEntity blockEntity, @Nullable TransferType cargo) {
        return available() && MekanismSideConfig.canEject(blockEntity, cargo);
    }

    public static boolean ejecting(BlockEntity blockEntity, @Nullable TransferType cargo) {
        return available() && MekanismSideConfig.ejecting(blockEntity, cargo);
    }

    public static void toggleEject(BlockEntity blockEntity, @Nullable TransferType cargo) {
        if (available()) {
            MekanismSideConfig.toggleEject(blockEntity, cargo);
        }
    }
}
