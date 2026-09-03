package nadiendev.ultimatetransport.compat.jade;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.tube.block.BlockTube;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TubeInfo implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = UltimateTransport.id("tube");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockState().getBlock() instanceof BlockTube)) {
            return;
        }
        Direction facing = accessor.getBlockState().getValue(BlockTube.FACING);
        tooltip.add(Component.translatable("jade.ultimatetransport.tube_direction",
                        Component.translatable("tooltip.ultimatetransport.direction." + facing.get3DDataValue())
                                .withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
