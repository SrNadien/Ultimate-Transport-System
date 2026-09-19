package nadiendev.ultimatetransport.compat.jade;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

/** The real charge, which above tier six is more than the energy capability can report. */
public enum CellInfo implements IBlockComponentProvider {

    INSTANCE;

    private static final ResourceLocation UID = UltimateTransport.id("energy_cell");
    private static final int FILL = 0xFF4AA3E0;
    private static final int EMPTY = 0xFF20242B;
    private static final int TEXT = 0xFFFFFFFF;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof EnergyCellBlockEntity cell)) {
            return;
        }
        tooltip.remove(JadeIds.UNIVERSAL_ENERGY_STORAGE);
        IElementHelper helper = IElementHelper.get();
        EnergyCellTier tier = cell.tier();
        Component charge = tier.creative()
                ? Component.translatable("screen.ultimatetransport.cell_endless")
                : Component.translatable("tooltip.ultimatetransport.cell_charge",
                Numbers.compact(cell.stored()),
                tier.bottomless()
                        ? Component.translatable("tooltip.ultimatetransport.unlimited").getString()
                        : Numbers.compact(cell.capacity()));
        tooltip.add(helper.progress((float) cell.fillRatio(), charge,
                helper.progressStyle().color(FILL, EMPTY).textColor(TEXT),
                BoxStyle.getNestedBox(),
                true));
        tooltip.add(Component.translatable("message.ultimatetransport.side_mode",
                Component.translatable("ultimatetransport.direction." + accessor.getSide().getSerializedName()),
                Component.translatable(cell.side(accessor.getSide()).translationKey())
                        .withStyle(style -> style.withColor(cell.side(accessor.getSide()).colour()))));
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.BODY + 5000;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
