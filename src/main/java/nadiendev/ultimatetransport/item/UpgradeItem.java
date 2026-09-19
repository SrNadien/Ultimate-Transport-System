package nadiendev.ultimatetransport.item;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.SideConfig;
import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.filter.FilterData;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) {
            return InteractionResult.PASS;
        }
        Direction side = CableBlock.pickSide(level.getBlockState(pos), pos,
                context.getClickLocation(), context.getClickedFace());
        if (!cable.container(side)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return install(cable, side, player, context.getItemInHand(), level, pos);
    }

    private static InteractionResult install(CableBlockEntity cable, Direction side, Player player,
                                             ItemStack held, Level level, BlockPos pos) {
        boolean placed = false;
        for (TransferType cargo : cable.type().carried()) {
            if (held.isEmpty()) {
                break;
            }
            SideConfig config = cable.config(side, cargo);
            ItemStack seated = config.upgrade();
            if (ItemStack.isSameItemSameComponents(seated, held)) {
                continue;
            }
            config.setUpgrade(held.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            if (!seated.isEmpty()) {
                hand(player, seated);
            }
            placed = true;
        }
        if (!placed) {
            return InteractionResult.CONSUME;
        }
        cable.onConfigChanged();
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
        return InteractionResult.CONSUME;
    }

    private static void hand(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
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
        FilterData rules = stack.get(UTDataComponents.FILTER.get());
        if (rules != null && !rules.entries().isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ultimatetransport.upgrade.rules",
                            rules.entries().size(), Component.translatable(rules.mode().translationKey()))
                    .withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("tooltip.ultimatetransport.upgrade.install")
                .withStyle(ChatFormatting.DARK_GRAY));
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
