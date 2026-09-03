package nadiendev.ultimatetransport.network;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.menu.CellMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Steps one face of a cell to its next mode. */
public record CellActionPayload(BlockPos pos, int side) implements CustomPacketPayload {

    public static final Type<CellActionPayload> TYPE = new Type<>(UltimateTransport.id("cell_action"));

    public static final StreamCodec<FriendlyByteBuf, CellActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CellActionPayload::pos,
            ByteBufCodecs.VAR_INT, CellActionPayload::side,
            CellActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CellActionPayload payload, IPayloadContext context) {
        if (payload.side() < 0 || payload.side() >= Direction.values().length) {
            return;
        }
        Player player = context.player();
        if (!(player.containerMenu instanceof CellMenu menu)
                || !menu.stillValid(player)
                || !menu.cell().getBlockPos().equals(payload.pos())) {
            return;
        }
        EnergyCellBlockEntity cell = menu.cell();
        Direction side = Direction.values()[payload.side()];
        cell.setSide(side, cell.side(side).next());
    }
}
