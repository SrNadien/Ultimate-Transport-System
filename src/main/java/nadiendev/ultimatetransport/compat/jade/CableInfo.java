package nadiendev.ultimatetransport.compat.jade;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.ConnectionMode;
import nadiendev.ultimatetransport.cable.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** What the face you are pointing at is set to, and what the upgrade in it allows. */
public enum CableInfo implements IBlockComponentProvider {

    INSTANCE;

    private static final ResourceLocation UID = UltimateTransport.id("cable");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof CableBlockEntity cable)) {
            return;
        }
        Direction side = CableBlock.pickSide(accessor.getBlockState(), accessor.getPosition(),
                accessor.getHitResult().getLocation(), accessor.getSide());
        if (cable.facade(side) != null) {
            tooltip.add(Component.translatable("jade.ultimatetransport.facade",
                    cable.facade(side).getBlock().getName()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!cable.container(side)) {
            return;
        }

        SideConfig sideConfig = cable.config(side);
        ConnectionMode mode = sideConfig.mode();

        tooltip.add(Component.translatable("message.ultimatetransport.side_mode",
                Component.translatable("ultimatetransport.direction." + side.getSerializedName()),
                Component.translatable(mode.translationKey()).withStyle(style -> style.withColor(mode.colour()))));

        tooltip.add(Component.translatable(sideConfig.tier().translationKey())
                .withStyle(style -> style.withColor(sideConfig.tier().colour())));

        int rules = sideConfig.filter().entries().size();
        if (rules > 0) {
            tooltip.add(Component.translatable("jade.ultimatetransport.rules",
                            Component.translatable(sideConfig.filter().mode().translationKey()), rules)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
