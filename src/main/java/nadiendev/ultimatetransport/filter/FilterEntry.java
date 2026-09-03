package nadiendev.ultimatetransport.filter;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * One rule on one cable face.
 *
 * <p>A rule names either a single entry ({@code minecraft:iron_ingot}) or a tag ({@code #c:ingots}),
 * optionally narrowed by an NBT snippet, and can be pinned to a single destination so that whatever it
 * matches goes only there. Inverting a rule makes it mean "anything but this".
 */
public class FilterEntry {

    // Key and tag flag travel as one string, "minecraft:iron_ingot" or "#c:ingots", which is both the
    // form the editor types and one field fewer -- a stream codec composes at most six.
    public static final Codec<FilterEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("Id").forGetter(FilterEntry::id),
            Codec.STRING.optionalFieldOf("Key", "").forGetter(FilterEntry::displayKey),
            CompoundTag.CODEC.optionalFieldOf("Nbt").forGetter(entry -> Optional.ofNullable(entry.nbt)),
            Codec.BOOL.optionalFieldOf("ExactNbt", false).forGetter(FilterEntry::exactNbt),
            DirectionalPosition.CODEC.optionalFieldOf("Destination").forGetter(entry -> Optional.ofNullable(entry.destination)),
            Codec.BOOL.optionalFieldOf("Invert", false).forGetter(FilterEntry::invert)
    ).apply(instance, (id, key, nbt, exactNbt, destination, invert) ->
            of(id, key, nbt.orElse(null), exactNbt, destination.orElse(null), invert)));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterEntry> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FilterEntry::id,
            ByteBufCodecs.STRING_UTF8, FilterEntry::displayKey,
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), entry -> Optional.ofNullable(entry.nbt),
            ByteBufCodecs.BOOL, FilterEntry::exactNbt,
            ByteBufCodecs.optional(DirectionalPosition.STREAM_CODEC), entry -> Optional.ofNullable(entry.destination),
            ByteBufCodecs.BOOL, FilterEntry::invert,
            (id, key, nbt, exactNbt, destination, invert) ->
                    of(id, key, nbt.orElse(null), exactNbt, destination.orElse(null), invert));

    private static FilterEntry of(UUID id, String key, @Nullable CompoundTag nbt, boolean exactNbt,
                                  @Nullable DirectionalPosition destination, boolean invert) {
        boolean tag = key.startsWith("#");
        ResourceLocation parsed = key.isEmpty() ? null : ResourceLocation.tryParse(tag ? key.substring(1) : key);
        return new FilterEntry(id, parsed, tag && parsed != null, nbt, exactNbt, destination, invert);
    }

    private final UUID id;
    @Nullable
    private ResourceLocation key;
    private boolean isTag;
    @Nullable
    private CompoundTag nbt;
    private boolean exactNbt;
    @Nullable
    private DirectionalPosition destination;
    private boolean invert;

    public FilterEntry(UUID id, @Nullable ResourceLocation key, boolean isTag, @Nullable CompoundTag nbt,
                       boolean exactNbt, @Nullable DirectionalPosition destination, boolean invert) {
        this.id = id;
        this.key = key;
        this.isTag = isTag;
        this.nbt = nbt;
        this.exactNbt = exactNbt;
        this.destination = destination;
        this.invert = invert;
    }

    public static FilterEntry empty() {
        return new FilterEntry(UUID.randomUUID(), null, false, null, false, null, false);
    }

    public UUID id() {
        return id;
    }

    @Nullable
    public ResourceLocation key() {
        return key;
    }

    public void setKey(@Nullable ResourceLocation key, boolean tag) {
        this.key = key;
        this.isTag = tag;
    }

    public boolean isTag() {
        return isTag;
    }

    @Nullable
    public CompoundTag nbt() {
        return nbt;
    }

    public void setNbt(@Nullable CompoundTag nbt) {
        this.nbt = nbt;
    }

    public boolean exactNbt() {
        return exactNbt;
    }

    public void setExactNbt(boolean exactNbt) {
        this.exactNbt = exactNbt;
    }

    @Nullable
    public DirectionalPosition destination() {
        return destination;
    }

    public void setDestination(@Nullable DirectionalPosition destination) {
        this.destination = destination;
    }

    public boolean invert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public boolean isBlank() {
        return key == null && nbt == null;
    }

    /** How the rule reads in the list and the editor: {@code #c:ingots} for a tag, the id otherwise. */
    public String displayKey() {
        if (key == null) {
            return "";
        }
        return isTag ? "#" + key : key.toString();
    }

    // ------------------------------------------------------------------ matching

    public boolean matchesItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean hit = matchesItemKey(stack) && matchesNbt(stack.getComponentsPatch());
        return hit != invert;
    }

    public boolean matchesFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean hit = matchesFluidKey(stack) && matchesNbt(stack.getComponentsPatch());
        return hit != invert;
    }

    /** Chemicals carry no components, so a gas rule is an id or a tag and nothing else. */
    public boolean matchesChemical(ResourceLocation chemical, java.util.function.Predicate<ResourceLocation> inTag) {
        if (key == null) {
            return !invert;
        }
        boolean hit = isTag ? inTag.test(key) : key.equals(chemical);
        return hit != invert;
    }

    private boolean matchesItemKey(ItemStack stack) {
        if (key == null) {
            return true;
        }
        if (isTag) {
            return stack.is(TagKey.create(Registries.ITEM, key));
        }
        return key.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private boolean matchesFluidKey(FluidStack stack) {
        if (key == null) {
            return true;
        }
        if (isTag) {
            return stack.is(TagKey.create(Registries.FLUID, key));
        }
        return key.equals(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
    }

    private boolean matchesNbt(DataComponentPatch patch) {
        if (nbt == null) {
            return true;
        }
        CompoundTag actual = toNbt(patch);
        if (actual == null) {
            return false;
        }
        return exactNbt ? actual.equals(nbt) : NbtUtils.compareNbt(nbt, actual, true);
    }

    @Nullable
    public static CompoundTag toNbt(DataComponentPatch patch) {
        if (patch.isEmpty()) {
            return null;
        }
        return DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, patch)
                .result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .orElse(null);
    }

    public FilterEntry copy() {
        return new FilterEntry(id, key, isTag, nbt == null ? null : nbt.copy(), exactNbt, destination, invert);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FilterEntry entry && id.equals(entry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
