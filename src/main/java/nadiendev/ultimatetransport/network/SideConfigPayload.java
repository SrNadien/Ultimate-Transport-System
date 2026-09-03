package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.menu.SideConfigMenu;
import nadiendev.ultimatetransport.menu.SideConfigTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SideConfigPayload(BlockPos pos, int side, int cargo, boolean forward) implements CustomPacketPayload {

    public static final int TOGGLE_EJECT = 6;
    public static final int CLEAR_ALL = 7;

    public static final Type<SideConfigPayload> TYPE = new Type<>(UltimateTransport.id("side_config"));

    public static final StreamCodec<FriendlyByteBuf, SideConfigPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SideConfigPayload::pos,
            ByteBufCodecs.VAR_INT, SideConfigPayload::side,
            ByteBufCodecs.VAR_INT, SideConfigPayload::cargo,
            ByteBufCodecs.BOOL, SideConfigPayload::forward,
            SideConfigPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SideConfigPayload payload, IPayloadContext context) {
        if (payload.side() < 0 || payload.side() > CLEAR_ALL) {
            return;
        }
        Player player = context.player();
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0
                || !player.level().isLoaded(pos)) {
            return;
        }
        BlockEntity target = player.level().getBlockEntity(pos);
        if (!SideConfigTarget.supports(target)) {
            return;
        }
        TransferType cargo = payload.cargo() >= 0 && payload.cargo() < TransferType.VALUES.length
                ? TransferType.VALUES[payload.cargo()]
                : null;
        switch (payload.side()) {
            case TOGGLE_EJECT -> SideConfigTarget.toggleEject(target, cargo);
            case CLEAR_ALL -> SideConfigTarget.clearAll(target, cargo);
            default -> SideConfigTarget.cycle(target, Direction.values()[payload.side()], cargo, payload.forward());
        }
    }
}
