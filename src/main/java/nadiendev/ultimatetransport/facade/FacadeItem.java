package nadiendev.ultimatetransport.facade;

import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A blank facade copies the block it is used on, and a filled one covers a cable face with it.
 * Copying is free and non-destructive: the facade reads the block, it does not take it.
 */
public class FacadeItem extends Item {

    public FacadeItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockState facadeOf(ItemStack stack) {
        return stack.get(UTDataComponents.FACADE_BLOCK.get());
    }

    public static ItemStack of(Item item, BlockState state) {
        ItemStack stack = new ItemStack(item);
        stack.set(UTDataComponents.FACADE_BLOCK.get(), state);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState target = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        BlockState facade = facadeOf(stack);

        if (target.getBlock() instanceof CableBlock) {
            return facade == null ? InteractionResult.PASS : apply(context, level, pos, target, facade, stack);
        }
        if (facade != null) {
            return InteractionResult.PASS;
        }
        return copy(context, level, target, stack);
    }

    private InteractionResult copy(UseOnContext context, Level level, BlockState target, ItemStack stack) {
        if (!FacadeSupport.canBeFacade(target)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        ItemStack copied = of(this, target);
        copied.setCount(1);
        stack.shrink(1);
        if (player != null && !player.addItem(copied)) {
            player.drop(copied, false);
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult apply(UseOnContext context, Level level, BlockPos pos,
                                    BlockState state, BlockState facade, ItemStack stack) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) {
            return InteractionResult.PASS;
        }
        Direction side = CableBlock.pickSide(state, pos, context.getClickLocation(), context.getClickedFace());
        if (cable.facade(side) != null) {
            return InteractionResult.PASS;
        }
        cable.setFacade(side, facade);
        SoundType sound = facade.getSoundType();
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);

        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Component getName(ItemStack stack) {
        BlockState facade = facadeOf(stack);
        return facade == null
                ? super.getName(stack)
                : Component.translatable("item.ultimatetransport.facade.of", facade.getBlock().getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(facadeOf(stack) == null
                ? "tooltip.ultimatetransport.facade.blank"
                : "tooltip.ultimatetransport.facade.filled").withStyle(ChatFormatting.GRAY));
    }
}
