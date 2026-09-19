package nadiendev.ultimatetransport.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public record FilterData(FilterMode mode, List<FilterEntry> entries) {

    public static final FilterData EMPTY = new FilterData(FilterMode.WHITELIST, List.of());

    public static final Codec<FilterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StringRepresentable.fromEnum(() -> FilterMode.VALUES)
                    .optionalFieldOf("Mode", FilterMode.WHITELIST).forGetter(FilterData::mode),
            FilterEntry.CODEC.listOf().optionalFieldOf("Entries", List.of()).forGetter(FilterData::entries)
    ).apply(instance, FilterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(id -> FilterMode.VALUES[id], FilterMode::ordinal), FilterData::mode,
            FilterEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), FilterData::entries,
            FilterData::new);

    public boolean isEmpty() {
        return entries.isEmpty() && mode == FilterMode.WHITELIST;
    }
}
