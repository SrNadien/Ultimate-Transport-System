package nadiendev.ultimatetransport.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** A block face somewhere in the world, used to pin a filter to one destination. */
public record DirectionalPosition(BlockPos pos, Direction direction) {

    public static final Codec<DirectionalPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("position").forGetter(DirectionalPosition::pos),
            Direction.CODEC.fieldOf("direction").forGetter(DirectionalPosition::direction)
    ).apply(instance, DirectionalPosition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DirectionalPosition> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DirectionalPosition::pos,
            Direction.STREAM_CODEC, DirectionalPosition::direction,
            DirectionalPosition::new);

    /** True when the given face of the given block is the one this points at. */
    public boolean is(BlockPos target, Direction face) {
        return pos.equals(target) && direction == face;
    }
}
