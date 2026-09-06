package nadiendev.ultimatetransport.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Someone with a wrench already in hand should not have to swap tools to open a pipe. Most mods now
 * agree on {@code #c:tools/wrench}; the rest are named individually, and an id that no installed mod
 * defines simply never matches.
 */
public final class Wrenches {

    private static final List<TagKey<Item>> TAGS = List.of(
            tag("c", "tools/wrench"),
            tag("c", "wrench"),
            tag("forge", "tools/wrench"),
            tag("create", "wrench"),
            tag("mekanism", "configurators"),
            tag("enderio", "wrench"),
            tag("ae2", "wrench"),
            tag("immersiveengineering", "tools/wrench"),
            tag("pipez", "wrench"));

    private Wrenches() {
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static boolean isWrench(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ConfiguratorItem) {
            return false;
        }
        for (TagKey<Item> tag : TAGS) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return namedLikeOne(stack);
    }

    /**
     * Not every wrench is tagged. Create's, Ender IO's and a few others sit in no common tag at all,
     * and a pack cannot be asked to write one, so the name is the last resort: an item called a wrench
     * is treated as a wrench, and this only ever decides what happens on our own blocks.
     */
    private static boolean namedLikeOne(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id.getPath();
        return path.contains("wrench") || path.contains("configurator") || path.contains("spanner");
    }
}
