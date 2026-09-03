package nadiendev.ultimatetransport.compat;

import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.SideConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class SourceBridge {
    private static SourceBridge instance = new SourceBridge();

    public static SourceBridge get() {
        return instance;
    }

    public static void set(SourceBridge bridge) {
        instance = bridge;
    }

    public boolean available() {
        return false;
    }

    public boolean present(Level level, BlockPos pos, Direction face) {
        return false;
    }

    public void extract(CableBlockEntity cable, Direction side, SideConfig config) {
    }

    public int push(CableBlockEntity cable, Direction side, SideConfig config, int budget, boolean simulate) {
        return 0;
    }
}
