package nadiendev.ultimatetransport.compat.patchouli;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.compat.ModIds;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.PatchouliAPI;

public final class GuideBook {

    public static final ResourceLocation ID = UltimateTransport.id("guide");

    private GuideBook() {
    }

    public static ItemStack recipeResult() {
        ItemStack book = new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book")));
        @SuppressWarnings("unchecked")
        DataComponentType<ResourceLocation> component = (DataComponentType<ResourceLocation>)
                BuiltInRegistries.DATA_COMPONENT_TYPE.get(
                        ResourceLocation.fromNamespaceAndPath("patchouli", "book"));
        book.set(component, ID);
        return book;
    }

    public static ItemStack stack() {
        if (!ModIds.loaded(ModIds.PATCHOULI)
                || !BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book"))) {
            return ItemStack.EMPTY;
        }
        return PatchouliAPI.get().getBookStack(ID);
    }
}
