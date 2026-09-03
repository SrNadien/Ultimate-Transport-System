package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.compat.patchouli.GuideBook;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UTCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UltimateTransport.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ultimatetransport"))
                    .icon(() -> new ItemStack(UTBlocks.UNIVERSAL_CABLE.get()))
                    .displayItems((parameters, output) -> {
                        UTItems.CABLE_ITEMS.forEach(item -> output.accept(item.get()));
                        UTItems.UPGRADES.values().forEach(item -> output.accept(item.get()));
                        output.accept(UTItems.CONFIGURATOR.get());
                        output.accept(UTItems.FACADE.get());
                        output.accept(UTItems.DESTINATION_TOOL.get());
                        output.accept(TubeRegistration.TUBE_ITEM.get());
                        TubeRegistration.DIRECTED_TUBES.forEach(tube -> output.accept(tube.get()));
                        output.accept(TubeRegistration.STATION_ITEM.get());
                        UTItems.CELLS.values().forEach(item -> output.accept(item.get()));
                        UTItems.GENERATOR_ITEMS.forEach(item -> output.accept(item.get()));
                        ItemStack guide = GuideBook.stack();
                        if (!guide.isEmpty()) {
                            output.accept(guide);
                        }
                    })
                    .build());

    private UTCreativeTabs() {
    }
}
