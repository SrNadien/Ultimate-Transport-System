package nadiendev.ultimatetransport.compat.ars;

import com.hollingsworth.arsnouveau.api.source.ISourceCap;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.CableNetwork;
import nadiendev.ultimatetransport.cable.DistributionMode;
import nadiendev.ultimatetransport.cable.SideConfig;
import nadiendev.ultimatetransport.compat.SourceBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;

public class ArsSourceBridge extends SourceBridge {
    @Override
    public boolean available() {
        return true;
    }

    @Override
    public boolean present(Level level, BlockPos pos, Direction face) {
        return level.isLoaded(pos) && level.getCapability(ArsCapabilities.SOURCE, pos, face) != null;
    }

    @Override
    public boolean extract(CableBlockEntity cable, Direction side, SideConfig config) {
        Level level = cable.getLevel();
        if (level == null) {
            return false;
        }
        ISourceCap source = level.getCapability(ArsCapabilities.SOURCE,
                cable.getBlockPos().relative(side), side.getOpposite());
        if (source == null || !source.canExtract()) {
            return false;
        }
        int available = source.extractSource(rate(config), true);
        if (available <= 0) {
            return false;
        }
        int moved = push(cable, side, config, available, false);
        if (moved > 0) {
            source.extractSource(moved, false);
        }
        return moved > 0;
    }

    @Override
    public int push(CableBlockEntity cable, Direction side, SideConfig config, int budget, boolean simulate) {
        Level level = cable.getLevel();
        if (level == null || budget <= 0) {
            return 0;
        }
        List<CableNetwork.Target> targets = cable.orderedTargets(side, config);
        if (targets.isEmpty()) {
            return 0;
        }
        int remaining = budget;
        boolean roundRobin = config.distribution() == DistributionMode.ROUND_ROBIN;
        int share = roundRobin ? Math.max(1, budget / targets.size()) : budget;

        for (int pass = 0; pass < (roundRobin ? 2 : 1) && remaining > 0; pass++) {
            int cap = pass == 0 ? share : remaining;
            for (CableNetwork.Target target : targets) {
                if (remaining <= 0) {
                    break;
                }
                ISourceCap destination = level.getCapability(ArsCapabilities.SOURCE,
                        target.destination(), target.face());
                if (destination == null || !destination.canReceive()) {
                    continue;
                }
                remaining -= destination.receiveSource(Math.min(remaining, cap), simulate);
            }
        }
        if (roundRobin && !simulate) {
            config.nextRoundRobin(targets.size());
        }
        return budget - remaining;
    }

    static int rate(SideConfig config) {
        return Math.max(1, config.tier().energyRate() / 100);
    }
}
