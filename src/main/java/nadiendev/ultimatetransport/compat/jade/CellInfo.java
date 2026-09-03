package nadiendev.ultimatetransport.compat.jade;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** The real charge, which above tier six is more than the energy capability can report. */
public enum CellInfo implements IBlockComponentProvider {

    INSTANCE;

    private static final ResourceLocation UID = UltimateTransport.id("energy_cell");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof EnergyCellBlockEntity cell)) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.ultimatetransport.cell_charge",
                        Numbers.compact(cell.stored()), Numbers.compact(cell.capacity()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("message.ultimatetransport.side_mode",
                Component.translatable("ultimatetransport.direction." + accessor.getSide().getSerializedName()),
                Component.translatable(cell.side(accessor.getSide()).translationKey())
                        .withStyle(style -> style.withColor(cell.side(accessor.getSide()).colour()))));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
