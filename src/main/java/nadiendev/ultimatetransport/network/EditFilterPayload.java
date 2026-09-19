package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.SideConfig;
import nadiendev.ultimatetransport.filter.FilterEntry;
import nadiendev.ultimatetransport.menu.CableMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Carries one rule between the two screens. {@code save} false opens the editor on it; true stores it
 * and takes the player back to the face screen.
 */
public record EditFilterPayload(BlockPos pos, int side, FilterEntry entry, boolean save)
        implements CustomPacketPayload {

    public static final Type<EditFilterPayload> TYPE = new Type<>(UltimateTransport.id("edit_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EditFilterPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, EditFilterPayload::pos,
            ByteBufCodecs.VAR_INT, EditFilterPayload::side,
            FilterEntry.STREAM_CODEC, EditFilterPayload::entry,
            ByteBufCodecs.BOOL, EditFilterPayload::save,
            EditFilterPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EditFilterPayload payload, IPayloadContext context) {
        CableBlockEntity cable = UTNetwork.verify(context, payload.pos(), payload.side());
        if (cable == null || !(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Direction direction = Direction.values()[payload.side()];
        SideConfig config = cable.config(direction, UTNetwork.cargoOf(context));
        if (!config.tier().canConfigure()) {
            return;
        }

        if (payload.save()) {
            if (!payload.entry().isBlank()) {
                if (config.canFilter()) {
                    config.filter().put(payload.entry());
                    config.writeFilter();
                }
                cable.onConfigChanged();
            }
            CableMenus.openCable(player, cable, direction);
        } else {
            CableMenus.openFilter(player, cable, direction, payload.entry(), UTNetwork.cargoOf(context));
        }
    }
}
