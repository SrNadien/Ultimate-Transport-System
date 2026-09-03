package nadiendev.ultimatetransport.item;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.cable.UpgradeTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class UpgradeItem extends Item {
    private final UpgradeTier tier;

    public UpgradeItem(UpgradeTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public UpgradeTier tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatetransport.upgrade.energy",
                rate(tier.energyRate())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ultimatetransport.upgrade.fluid",
                rate(tier.fluidRate())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ultimatetransport.upgrade.gas",
                rate(tier.gasRate())).withStyle(ChatFormatting.GRAY));
        int interval = tier.itemInterval();
        tooltip.add((interval == 1
                ? Component.translatable("tooltip.ultimatetransport.upgrade.item_tick", tier.itemCount())
                : Component.translatable("tooltip.ultimatetransport.upgrade.item", tier.itemCount(), interval))
                .withStyle(ChatFormatting.GRAY));
        capability(tooltip, tier.canRedstone(), "redstone");
        capability(tooltip, tier.canDistribute(), "distribution");
        capability(tooltip, tier.canFilter(), "filter");
    }

    private static void capability(List<Component> tooltip, boolean allowed, String name) {
        tooltip.add(Component.translatable("tooltip.ultimatetransport.upgrade." + name
                        + (allowed ? "_yes" : "_no"))
                .withStyle(allowed ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
    }

    private static Component rate(int value) {
        return value == Integer.MAX_VALUE
                ? Component.translatable("tooltip.ultimatetransport.unlimited")
                : Component.literal(Numbers.compact(value));
    }
}
