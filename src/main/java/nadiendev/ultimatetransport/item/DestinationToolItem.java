package nadiendev.ultimatetransport.item;

import nadiendev.ultimatetransport.filter.DirectionalPosition;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Records the block face it is used on. Dropping the tool onto a rule's destination slot pins that
 * rule to the recorded face, which is how one filter is made to feed one machine and no other.
 */
public class DestinationToolItem extends Item {

    public DestinationToolItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static DirectionalPosition destination(ItemStack stack) {
        return stack.get(UTDataComponents.DESTINATION.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        DirectionalPosition destination = new DirectionalPosition(context.getClickedPos(), context.getClickedFace());
        stack.set(UTDataComponents.DESTINATION.get(), destination);

        level.playSound(null, context.getClickedPos(), SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.6F, 1.6F);
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(describe(destination), true);
        }
        return InteractionResult.CONSUME;
    }

    public static Component describe(DirectionalPosition destination) {
        return Component.translatable("message.ultimatetransport.destination",
                destination.pos().getX(), destination.pos().getY(), destination.pos().getZ(),
                Component.translatable("ultimatetransport.direction." + destination.direction().getSerializedName()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        DirectionalPosition destination = destination(stack);
        if (destination == null) {
            tooltip.add(Component.translatable("tooltip.ultimatetransport.destination_tool")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(describe(destination).copy().withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
