package nadiendev.ultimatetransport.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import nadiendev.ultimatetransport.client.FilterScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Lets a rule be filled by dragging an item straight out of JEI onto the editor's item slot. */
public class FilterGhostHandler implements IGhostIngredientHandler<FilterScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(FilterScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        if (ingredient.getIngredient() instanceof ItemStack) {
            targets.add(new SlotTarget<>(screen));
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }

    private record SlotTarget<I>(FilterScreen screen) implements Target<I> {

        @Override
        public Rect2i getArea() {
            return screen.itemSlotArea();
        }

        @Override
        public void accept(I ingredient) {
            if (ingredient instanceof ItemStack stack) {
                screen.fillFromGhost(stack);
            }
        }
    }
}
