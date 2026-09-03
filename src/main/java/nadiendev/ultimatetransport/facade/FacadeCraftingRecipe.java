package nadiendev.ultimatetransport.facade;

import nadiendev.ultimatetransport.registry.UTItems;
import nadiendev.ultimatetransport.registry.UTRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeCraftingRecipe extends CustomRecipe {

    public FacadeCraftingRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static BlockState covering(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem block)) {
            return null;
        }
        BlockState state = block.getBlock().defaultBlockState();
        return FacadeSupport.canBeFacade(state) ? state : null;
    }

    private static BlockState resolve(CraftingInput input) {
        ItemStack blank = ItemStack.EMPTY;
        BlockState covering = null;
        int extras = 0;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == UTItems.FACADE.get()) {
                if (!blank.isEmpty() || FacadeItem.facadeOf(stack) != null) {
                    return null;
                }
                blank = stack;
                continue;
            }
            BlockState state = covering(stack);
            if (state == null || covering != null) {
                return null;
            }
            covering = state;
            extras++;
        }
        return blank.isEmpty() || extras != 1 ? null : covering;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return resolve(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        BlockState covering = resolve(input);
        return covering == null ? ItemStack.EMPTY : FacadeItem.of(UTItems.FACADE.get(), covering);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return UTRecipes.FACADE_CRAFTING.get();
    }
}
