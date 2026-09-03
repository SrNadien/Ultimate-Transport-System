package nadiendev.ultimatetransport.cable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public final class Retrieval {

    private Retrieval() {
    }

    public static void energy(CableBlockEntity cable, Direction side, SideConfig config) {
        IEnergyStorage destination = cable.neighbourCapability(Capabilities.EnergyStorage.BLOCK,
                cable.getBlockPos().relative(side), side.getOpposite());
        if (destination == null || !destination.canReceive()) {
            return;
        }
        int budget = config.tier().energyRate();
        for (CableNetwork.Target target : cable.orderedTargets(side, config)) {
            if (budget <= 0) {
                break;
            }
            IEnergyStorage source = cable.neighbourCapability(Capabilities.EnergyStorage.BLOCK,
                    target.destination(), target.face());
            if (source == null || !source.canExtract()) {
                continue;
            }
            int available = source.extractEnergy(budget, true);
            if (available <= 0) {
                continue;
            }
            int accepted = destination.receiveEnergy(available, false);
            if (accepted > 0) {
                source.extractEnergy(accepted, false);
                budget -= accepted;
            }
        }
    }

    public static void fluid(CableBlockEntity cable, Direction side, SideConfig config) {
        IFluidHandler destination = cable.neighbourCapability(Capabilities.FluidHandler.BLOCK,
                cable.getBlockPos().relative(side), side.getOpposite());
        if (destination == null) {
            return;
        }
        int budget = config.tier().fluidRate();
        for (CableNetwork.Target target : cable.orderedTargets(side, config)) {
            if (budget <= 0) {
                break;
            }
            IFluidHandler source = cable.neighbourCapability(Capabilities.FluidHandler.BLOCK,
                    target.destination(), target.face());
            if (source == null) {
                continue;
            }
            for (int tank = 0; tank < source.getTanks() && budget > 0; tank++) {
                FluidStack inTank = source.getFluidInTank(tank);
                if (inTank.isEmpty() || !config.filter().allowsFluid(inTank)) {
                    continue;
                }
                FluidStack drained = source.drain(inTank.copyWithAmount(budget), IFluidHandler.FluidAction.SIMULATE);
                if (drained.isEmpty()) {
                    continue;
                }
                int filled = destination.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    source.drain(drained.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
                    budget -= filled;
                }
            }
        }
    }

    public static void items(CableBlockEntity cable, Direction side, SideConfig config) {
        IItemHandler destination = cable.neighbourCapability(Capabilities.ItemHandler.BLOCK,
                cable.getBlockPos().relative(side), side.getOpposite());
        if (destination == null) {
            return;
        }
        int budget = config.tier().itemCount();
        List<CableNetwork.Target> targets = cable.orderedTargets(side, config);
        for (CableNetwork.Target target : targets) {
            if (budget <= 0) {
                break;
            }
            IItemHandler source = cable.neighbourCapability(Capabilities.ItemHandler.BLOCK,
                    target.destination(), target.face());
            if (source == null) {
                continue;
            }
            for (int slot = 0; slot < source.getSlots() && budget > 0; slot++) {
                ItemStack candidate = source.extractItem(slot, budget, true);
                if (candidate.isEmpty() || !config.filter().allowsItem(candidate)) {
                    continue;
                }
                ItemStack rejected = ItemHandlerHelper.insertItem(destination, candidate, false);
                int moved = candidate.getCount() - rejected.getCount();
                if (moved > 0) {
                    source.extractItem(slot, moved, false);
                    budget -= moved;
                }
            }
        }
    }
}
