package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.SideConfig;
import nadiendev.ultimatetransport.menu.CableMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The buttons on the face screen that are not about a single rule. */
public record CableActionPayload(BlockPos pos, int side, int action, int value) implements CustomPacketPayload {

    public static final int CYCLE_REDSTONE = 0;
    public static final int CYCLE_DISTRIBUTION = 1;
    public static final int CYCLE_FILTER_MODE = 2;
    public static final int PRIORITY = 3;
    public static final int OPEN_CABLE = 4;
    public static final int TOGGLE_RETRIEVE = 5;
    public static final int SWITCH_CARGO = 6;

    public static final Type<CableActionPayload> TYPE = new Type<>(UltimateTransport.id("cable_action"));

    public static final StreamCodec<FriendlyByteBuf, CableActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CableActionPayload::pos,
            ByteBufCodecs.VAR_INT, CableActionPayload::side,
            ByteBufCodecs.VAR_INT, CableActionPayload::action,
            ByteBufCodecs.VAR_INT, CableActionPayload::value,
            CableActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CableActionPayload payload, IPayloadContext context) {
        CableBlockEntity cable = UTNetwork.verify(context, payload.pos(), payload.side());
        if (cable == null) {
            return;
        }
        Direction direction = Direction.values()[payload.side()];
        SideConfig config = cable.config(direction, UTNetwork.cargoOf(context));

        if (payload.action() == SWITCH_CARGO && context.player() instanceof ServerPlayer viewer) {
            TransferType[] carried = cable.type().carried();
            int index = Math.clamp(payload.value(), 0, carried.length - 1);
            CableMenus.openCable(viewer, cable, direction, carried[index]);
            return;
        }
        if (payload.action() == OPEN_CABLE) {
            if (context.player() instanceof ServerPlayer player) {
                CableMenus.openCable(player, cable, direction, UTNetwork.cargoOf(context));
            }
            return;
        }

        // Everything below is a setting, and a bare face has no settings to change.
        if (!config.tier().canConfigure()) {
            return;
        }
        switch (payload.action()) {
            case CYCLE_REDSTONE -> config.setRedstone(config.redstone().next());
            case CYCLE_DISTRIBUTION -> config.setDistribution(config.distribution().next());
            case CYCLE_FILTER_MODE -> {
                config.filter().cycleMode();
                config.writeFilter();
            }
            case PRIORITY -> config.setPriority(Math.clamp(payload.value(), -128, 128));
            case TOGGLE_RETRIEVE -> config.setRetrieve(!config.retrieve());
            default -> {
                return;
            }
        }
        cable.onConfigChanged();
    }
}
