package nadiendev.ultimatetransport.compat.camol;

import nadiendev.ultimatetransport.compat.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class CamolCompat {
    private CamolCompat() {
    }

    public static boolean isCamoTool(ItemStack stack) {
        return !stack.isEmpty()
                && ModIds.loaded(ModIds.CAMOL)
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(ModIds.CAMOL);
    }
}
