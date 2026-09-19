package nadiendev.ultimatetransport.compat;

import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.SideConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * The single seam between the gas cable and Mekanism. The default instance answers "no gas here" to
 * everything, so the rest of the mod compiles and runs with Mekanism absent; the Mekanism compat
 * layer swaps in a real implementation during setup.
 */
public class ChemicalBridge {

    private static ChemicalBridge instance = new ChemicalBridge();

    public static ChemicalBridge get() {
        return instance;
    }

    public static void set(ChemicalBridge bridge) {
        instance = bridge;
    }

    public boolean available() {
        return false;
    }

    public boolean present(Level level, BlockPos pos, Direction face) {
        return false;
    }

    public boolean extract(CableBlockEntity cable, Direction side, SideConfig config) {
        return false;
    }
}
