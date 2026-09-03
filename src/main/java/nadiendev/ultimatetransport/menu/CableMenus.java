package nadiendev.ultimatetransport.menu;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.filter.FilterEntry;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/** Opening either cable screen, from the block or from a button on the other screen. */
public final class CableMenus {

    private CableMenus() {
    }

    public static Component title(CableBlockEntity cable, Direction side) {
        return Component.translatable("screen.ultimatetransport.cable",
                cable.getBlockState().getBlock().getName(),
                Component.translatable("ultimatetransport.direction." + side.getSerializedName()));
    }

    public static void openCable(ServerPlayer player, CableBlockEntity cable, Direction side) {
        openCable(player, cable, side, cable.primary());
    }

    public static void openCable(ServerPlayer player, CableBlockEntity cable, Direction side, TransferType cargo) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, viewer) -> new CableMenu(id, inventory, cable, side, cargo),
                title(cable, side)), buffer -> {
            buffer.writeBlockPos(cable.getBlockPos());
            buffer.writeByte(side.ordinal());
            buffer.writeByte(cargo.ordinal());
        });
    }

    public static void openFilter(ServerPlayer player, CableBlockEntity cable, Direction side, FilterEntry entry) {
        openFilter(player, cable, side, entry, cable.primary());
    }

    public static void openFilter(ServerPlayer player, CableBlockEntity cable, Direction side, FilterEntry entry,
                                  TransferType cargo) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, viewer) -> new FilterMenu(id, inventory, cable, side, entry, cargo),
                Component.translatable("screen.ultimatetransport.filter")), buffer -> {
            buffer.writeBlockPos(cable.getBlockPos());
            buffer.writeByte(side.ordinal());
            FilterEntry.STREAM_CODEC.encode(buffer, entry);
        });
    }
}
