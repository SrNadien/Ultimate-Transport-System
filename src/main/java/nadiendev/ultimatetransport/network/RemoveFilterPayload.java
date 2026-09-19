package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.SideConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RemoveFilterPayload(BlockPos pos, int side, UUID id) implements CustomPacketPayload {

    public static final Type<RemoveFilterPayload> TYPE = new Type<>(UltimateTransport.id("remove_filter"));

    public static final StreamCodec<FriendlyByteBuf, RemoveFilterPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RemoveFilterPayload::pos,
            ByteBufCodecs.VAR_INT, RemoveFilterPayload::side,
            UUIDUtil.STREAM_CODEC, RemoveFilterPayload::id,
            RemoveFilterPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoveFilterPayload payload, IPayloadContext context) {
        CableBlockEntity cable = UTNetwork.verify(context, payload.pos(), payload.side());
        if (cable == null) {
            return;
        }
        SideConfig config = cable.config(Direction.values()[payload.side()], UTNetwork.cargoOf(context));
        if (!config.tier().canConfigure()) {
            return;
        }
        config.filter().remove(payload.id());
        config.writeFilter();
        cable.onConfigChanged();
    }
}
