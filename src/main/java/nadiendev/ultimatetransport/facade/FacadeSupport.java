package nadiendev.ultimatetransport.facade;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class FacadeSupport {

    private static Function<ItemStack, BlockState> foreign = stack -> null;

    public static void setForeignReader(Function<ItemStack, BlockState> reader) {
        foreign = reader;
    }

    @Nullable
    public static BlockState foreignFacade(ItemStack stack) {
        return stack.isEmpty() ? null : foreign.apply(stack);
    }

    private FacadeSupport() {
    }

    /**
     * Only plain full cubes make sense as a cover: anything with a block entity, a non-cube shape or
     * its own tick would either render wrong or lose state the moment it became a facade.
     */
    public static boolean canBeFacade(BlockState state) {
        return !state.isAir()
                && !state.hasBlockEntity()
                && state.getBlock().defaultBlockState().isCollisionShapeFullBlock(
                        net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO);
    }
}
