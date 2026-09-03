package nadiendev.ultimatetransport.compat.ars;

import com.hollingsworth.arsnouveau.api.source.ISourceCap;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.compat.SourceBridge;
import net.minecraft.core.Direction;

public record NetworkSource(CableBlockEntity cable, Direction side) implements ISourceCap {
    @Override
    public boolean canAcceptSource(int source) {
        return true;
    }

    @Override
    public boolean canProvideSource(int source) {
        return false;
    }

    @Override
    public int getMaxExtract() {
        return 0;
    }

    @Override
    public int getMaxReceive() {
        return ArsSourceBridge.rate(cable.config(side, TransferType.SOURCE));
    }

    @Override
    public int getSource() {
        return 0;
    }

    @Override
    public int getSourceCapacity() {
        return getMaxReceive();
    }

    @Override
    public void setSource(int source) {
    }

    @Override
    public void setMaxSource(int max) {
    }

    @Override
    public int receiveSource(int source, boolean simulate) {
        int budget = Math.min(source, getMaxReceive());
        return SourceBridge.get().push(cable, side, cable.config(side, TransferType.SOURCE), budget, simulate);
    }

    @Override
    public int extractSource(int source, boolean simulate) {
        return 0;
    }
}
