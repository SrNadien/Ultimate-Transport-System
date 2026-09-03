package nadiendev.ultimatetransport.cell;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class EnergyCellItem extends BlockItem {

    private final EnergyCellTier tier;

    public EnergyCellItem(Block block, EnergyCellTier tier, Properties properties) {
        super(block, properties);
        this.tier = tier;
    }

    public EnergyCellTier tier() {
        return tier;
    }

    public static long charge(ItemStack stack) {
        Long value = stack.get(UTDataComponents.STORED_ENERGY.get());
        return value == null ? 0L : value;
    }

    public static void setCharge(ItemStack stack, long value) {
        stack.set(UTDataComponents.STORED_ENERGY.get(), value);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // A bottomless cell has no capacity worth printing, and Long.MAX_VALUE reads as noise.
        if (tier.creative()) {
            tooltip.add(Component.translatable("screen.ultimatetransport.cell_endless")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("tooltip.ultimatetransport.cell_charge",
                    Numbers.compact(charge(stack)),
                    tier.bottomless()
                            ? Component.translatable("tooltip.ultimatetransport.unlimited").getString()
                            : Numbers.compact(tier.capacity())).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.ultimatetransport.cell_transfer",
                Numbers.compact(tier.transfer())).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !tier.bottomless() && charge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13.0 * charge(stack) / tier.capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return tier.colour();
    }
}
