package nadiendev.ultimatetransport.compat;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

public final class ModIds {

    public static final String MEKANISM = "mekanism";
    public static final String FLUX_NETWORKS = "fluxnetworks";
    public static final String ARS_NOUVEAU = "ars_nouveau";
    public static final String CAMOL = "camol";
    public static final String ENDER_IO = "enderio";
    public static final String FTB_ULTIMINE = "ftbultimine";
    public static final String ORE_EXCAVATION = "oreexcavation";
    public static final String ULTIMATE_AVARITIA = "ultimateavaritiaadditions";
    public static final String PATCHOULI = "patchouli";

    private ModIds() {
    }

    public static boolean loaded(String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }

    /**
     * Whether a mod is present early enough to decide what to register. {@link ModList} is not built
     * yet while a {@code DeferredRegister} is being filled, but the loading list already is.
     *
     * <p>Data generation always answers yes: the recipes, models and translations for the optional
     * content have to end up in the jar so they are ready the moment the mod alongside is installed.
     */
    public static boolean installedAtLoad(String modId) {
        if (DatagenModLoader.isRunningDataGen()) {
            return true;
        }
        LoadingModList list = LoadingModList.get();
        return list != null && list.getModFileById(modId) != null;
    }
}
