package nadiendev.ultimatetransport.cable.io;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public record NetworkFluid(CableBlockEntity cable, Direction side) implements IFluidHandler {

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return cable.config(side, TransferType.FLUID).tier().fluidRate();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return cable.config(side, TransferType.FLUID).filter().allowsFluid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!isFluidValid(0, resource)) {
            return 0;
        }
        int budget = Math.min(resource.getAmount(), cable.config(side, TransferType.FLUID).tier().fluidRate());
        return cable.pushFluid(side, cable.config(side, TransferType.FLUID), resource.copyWithAmount(budget), action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
