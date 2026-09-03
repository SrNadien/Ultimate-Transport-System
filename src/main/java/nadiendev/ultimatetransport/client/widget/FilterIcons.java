package nadiendev.ultimatetransport.client.widget;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.filter.FilterEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** Turns a rule into something the list and the editor can draw. */
public final class FilterIcons {

    private FilterIcons() {
    }

    public static ItemStack stackFor(TransferType type, FilterEntry entry) {
        ResourceLocation key = entry.key();
        if (key == null) {
            return ItemStack.EMPTY;
        }
        return switch (type) {
            case FLUID -> fluid(key, entry.isTag());
            case ITEM -> item(key, entry.isTag());
            case GAS -> ItemStack.EMPTY;
            default -> {
                ItemStack stack = item(key, entry.isTag());
                yield stack.isEmpty() ? fluid(key, entry.isTag()) : stack;
            }
        };
    }

    private static ItemStack item(ResourceLocation key, boolean tag) {
        if (tag) {
            return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, key))
                    .flatMap(FilterIcons::first)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
        }
        return BuiltInRegistries.ITEM.getOptional(key).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private static ItemStack fluid(ResourceLocation key, boolean tag) {
        java.util.Optional<Fluid> fluid = tag
                ? BuiltInRegistries.FLUID.getTag(TagKey.create(Registries.FLUID, key)).flatMap(FilterIcons::first)
                : BuiltInRegistries.FLUID.getOptional(key);
        return fluid.map(f -> new ItemStack(f.getBucket())).orElse(ItemStack.EMPTY);
    }

    private static <T> java.util.Optional<T> first(HolderSet<T> set) {
        return set.size() == 0 ? java.util.Optional.empty() : java.util.Optional.of(set.get(0).value());
    }

    /** Used by the item lookup above; kept separate so the generic bound stays readable. */
    public static <T> java.util.Optional<Holder<T>> firstHolder(HolderSet<T> set) {
        return set.size() == 0 ? java.util.Optional.empty() : java.util.Optional.of(set.get(0));
    }

    public static Item itemOf(ResourceLocation key) {
        return BuiltInRegistries.ITEM.get(key);
    }
}
