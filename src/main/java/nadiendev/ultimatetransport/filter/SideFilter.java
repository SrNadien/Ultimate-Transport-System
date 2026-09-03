package nadiendev.ultimatetransport.filter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/** The rules on one cable face, plus whether they are a whitelist or a blacklist. */
public class SideFilter {

    private final List<FilterEntry> entries = new ArrayList<>();
    private FilterMode mode = FilterMode.WHITELIST;

    public List<FilterEntry> entries() {
        return entries;
    }

    public FilterMode mode() {
        return mode;
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public void cycleMode() {
        mode = mode.next();
    }

    /** Adds a rule, or replaces the one with the same id when the editor sends an edit back. */
    public void put(FilterEntry entry) {
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).id().equals(entry.id())) {
                entries.set(index, entry);
                return;
            }
        }
        entries.add(entry);
    }

    public void remove(UUID id) {
        entries.removeIf(entry -> entry.id().equals(id));
    }

    @Nullable
    public FilterEntry byId(UUID id) {
        for (FilterEntry entry : entries) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ decisions

    public boolean allowsItem(ItemStack stack) {
        return allows(entry -> entry.matchesItem(stack));
    }

    public boolean allowsFluid(FluidStack stack) {
        return allows(entry -> entry.matchesFluid(stack));
    }

    public boolean allowsChemical(ResourceLocation chemical, Predicate<ResourceLocation> inTag) {
        return allows(entry -> entry.matchesChemical(chemical, inTag));
    }

    private boolean allows(Predicate<FilterEntry> matcher) {
        if (entries.isEmpty()) {
            return true;
        }
        boolean matched = false;
        for (FilterEntry entry : entries) {
            if (matcher.test(entry)) {
                matched = true;
                break;
            }
        }
        return mode == FilterMode.WHITELIST == matched;
    }

    /**
     * The face a matching rule pins this cargo to, if any. A rule with no destination leaves the cargo
     * free to go wherever the network wants it.
     */
    @Nullable
    public DirectionalPosition destinationForItem(ItemStack stack) {
        return destination(entry -> entry.matchesItem(stack));
    }

    @Nullable
    public DirectionalPosition destinationForFluid(FluidStack stack) {
        return destination(entry -> entry.matchesFluid(stack));
    }

    @Nullable
    private DirectionalPosition destination(Predicate<FilterEntry> matcher) {
        for (FilterEntry entry : entries) {
            if (entry.destination() != null && matcher.test(entry)) {
                return entry.destination();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ serialisation

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Mode", mode.getSerializedName());
        ListTag list = new ListTag();
        for (FilterEntry entry : entries) {
            FilterEntry.CODEC.encodeStart(NbtOps.INSTANCE, entry).result().ifPresent(list::add);
        }
        tag.put("Entries", list);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        mode = FilterMode.byName(tag.getString("Mode"));
        entries.clear();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            FilterEntry.CODEC.parse(NbtOps.INSTANCE, list.getCompound(index)).result().ifPresent(entries::add);
        }
    }
}
