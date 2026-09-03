package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.cell.EnergyCellItem;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import nadiendev.ultimatetransport.facade.FacadeItem;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.item.DestinationToolItem;
import nadiendev.ultimatetransport.item.UpgradeItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UTItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UltimateTransport.MODID);

    public static final List<DeferredItem<? extends Item>> CABLE_ITEMS = new ArrayList<>();
    public static final List<DeferredItem<? extends Item>> GENERATOR_ITEMS = new ArrayList<>();
    public static final Map<UpgradeTier, DeferredItem<UpgradeItem>> UPGRADES = new EnumMap<>(UpgradeTier.class);
    public static final Map<EnergyCellTier, DeferredItem<EnergyCellItem>> CELLS = new EnumMap<>(EnergyCellTier.class);

    static {
        UTBlocks.cables().forEach(block -> CABLE_ITEMS.add(ITEMS.registerSimpleBlockItem(block)));
        UTBlocks.generators().forEach(block -> GENERATOR_ITEMS.add(ITEMS.registerSimpleBlockItem(block)));
        for (UpgradeTier tier : UpgradeTier.VALUES) {
            if (tier == UpgradeTier.NONE) {
                continue;
            }
            UPGRADES.put(tier, ITEMS.registerItem(tier.itemName(),
                    properties -> new UpgradeItem(tier, properties.stacksTo(16))));
        }
        for (int index = 0; index < EnergyCellTier.VALUES.length; index++) {
            EnergyCellTier tier = EnergyCellTier.VALUES[index];
            var block = UTBlocks.cells().get(index);
            CELLS.put(tier, ITEMS.registerItem(tier.blockName(),
                    properties -> new EnergyCellItem(block.get(), tier, properties.stacksTo(1))));
        }
    }

    public static final DeferredItem<ConfiguratorItem> CONFIGURATOR =
            ITEMS.registerItem("configurator", properties -> new ConfiguratorItem(properties.stacksTo(1)));

    public static final DeferredItem<FacadeItem> FACADE =
            ITEMS.registerItem("facade", properties -> new FacadeItem(properties.stacksTo(16)));

    public static final DeferredItem<DestinationToolItem> DESTINATION_TOOL =
            ITEMS.registerItem("destination_tool", properties -> new DestinationToolItem(properties.stacksTo(1)));

    private UTItems() {
    }
}
