package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.menu.CableMenu;
import nadiendev.ultimatetransport.menu.CellMenu;
import nadiendev.ultimatetransport.menu.FilterMenu;
import nadiendev.ultimatetransport.menu.GeneratorMenu;
import nadiendev.ultimatetransport.menu.SideConfigMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UTMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, UltimateTransport.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CableMenu>> CABLE =
            MENUS.register("cable", () -> IMenuTypeExtension.create(CableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> FILTER =
            MENUS.register("filter", () -> IMenuTypeExtension.create(FilterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CellMenu>> CELL =
            MENUS.register("cell", () -> IMenuTypeExtension.create(CellMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GeneratorMenu>> GENERATOR =
            MENUS.register("generator", () -> IMenuTypeExtension.create(GeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SideConfigMenu>> SIDE_CONFIG =
            MENUS.register("side_config", () -> IMenuTypeExtension.create(SideConfigMenu::new));

    private UTMenus() {
    }
}
