package nadiendev.ultimatetransport.cable;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.CableNetwork.Target;
import nadiendev.ultimatetransport.compat.ChemicalBridge;
import nadiendev.ultimatetransport.compat.SourceBridge;
import nadiendev.ultimatetransport.facade.FacadeProperties;
import nadiendev.ultimatetransport.filter.DirectionalPosition;
import nadiendev.ultimatetransport.registry.UTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CableBlockEntity extends BlockEntity {
    private final Map<Direction, EnumMap<TransferType, SideConfig>> configs = new EnumMap<>(Direction.class);
    private final Map<Direction, EnumMap<TransferType, List<Target>>> targetCache = new EnumMap<>(Direction.class);

    private final BlockState[] facades = new BlockState[Direction.values().length];

    private int cacheVersion = -1;
    private int tickCounter = 0;
    private boolean hasExtract = false;
    private boolean routing = false;
    private boolean settled = false;
    private boolean refreshing = false;
    private boolean refreshQueued = false;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(UTBlockEntities.CABLE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            EnumMap<TransferType, SideConfig> perType = new EnumMap<>(TransferType.class);
            for (TransferType cargo : TransferType.values()) {
                SideConfig config = new SideConfig();
                config.setCargo(cargo);
                perType.put(cargo, config);
            }
            configs.put(direction, perType);
        }
    }

    public TransferType type() {
        return getBlockState().getBlock() instanceof CableBlock cable ? cable.type() : TransferType.UNIVERSAL;
    }

    public TransferType primary() {
        return type().carried()[0];
    }

    public SideConfig config(Direction direction, TransferType cargo) {
        return configs.get(direction).get(type().carries(cargo) ? cargo : primary());
    }

    public SideConfig config(Direction direction) {
        return config(direction, primary());
    }

    public boolean hasExtract() {
        return hasExtract;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableBlockEntity cable) {
        cable.tick();
    }

    private void tick() {
        tickCounter++;
        if (!settled) {
            settled = true;
            refreshConnections();
        }
        if (!hasExtract || level == null || routing) {
            return;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        TransferType type = type();

        for (Direction direction : Direction.values()) {
            if (!container(direction)) {
                continue;
            }
            for (TransferType cargo : type.carried()) {
            SideConfig config = config(direction, cargo);
            if (config.mode() != ConnectionMode.EXTRACT || !config.redstone().allows(powered)) {
                continue;
            }
            routing = true;
            boolean pulling = config.retrieve();
            try {
                if (cargo == TransferType.ENERGY) {
                    if (pulling) {
                        Retrieval.energy(this, direction, config);
                    } else {
                        extractEnergy(direction, config);
                    }
                }
                if (cargo == TransferType.FLUID) {
                    if (pulling) {
                        Retrieval.fluid(this, direction, config);
                    } else {
                        extractFluid(direction, config);
                    }
                }
                if (cargo == TransferType.SOURCE && !pulling) {
                    SourceBridge.get().extract(this, direction, config);
                }
                if (cargo == TransferType.GAS && !pulling) {
                    ChemicalBridge.get().extract(this, direction, config);
                }
                if (cargo == TransferType.ITEM && tickCounter % config.tier().itemInterval() == 0) {
                    if (pulling) {
                        Retrieval.items(this, direction, config);
                    } else {
                        extractItems(direction, config);
                    }
                }
            } finally {
                routing = false;
            }
            }
        }
    }

    private boolean anyExtract(Direction direction) {
        for (TransferType cargo : type().carried()) {
            if (config(direction, cargo).mode() == ConnectionMode.EXTRACT) {
                return true;
            }
        }
        return false;
    }

    public List<Target> targets(Direction side) {
        return targets(side, primary());
    }

    public List<Target> targets(Direction side, TransferType cargo) {
        if (level == null) {
            return List.of();
        }
        if (cacheVersion != CableNetwork.version()) {
            targetCache.clear();
            cacheVersion = CableNetwork.version();
        }
        return targetCache.computeIfAbsent(side, s -> new EnumMap<>(TransferType.class))
                .computeIfAbsent(cargo, c -> CableNetwork.findTargets(level, worldPosition, side, type(), c));
    }

    public List<Target> orderedTargets(Direction side, SideConfig config) {
        return ordered(config, targets(side, config.cargo()));
    }

    private List<Target> ordered(SideConfig config, List<Target> targets) {
        List<Target> list = new ArrayList<>(targets);
        switch (config.distribution()) {
            case NEAREST -> {
            }
            case FURTHEST -> list.sort((a, b) -> a.priority() != b.priority()
                    ? Integer.compare(b.priority(), a.priority())
                    : Integer.compare(b.distance(), a.distance()));
            case RANDOM -> {
                if (level != null) {
                    Collections.shuffle(list, new java.util.Random(level.random.nextLong()));
                }
            }
            case ROUND_ROBIN -> Collections.rotate(list, -config.roundRobin(list.size()));
        }
        return list;
    }

    private static List<Target> pin(List<Target> targets, @Nullable DirectionalPosition destination) {
        if (destination == null) {
            return targets;
        }
        List<Target> pinned = new ArrayList<>(1);
        for (Target target : targets) {
            if (destination.is(target.destination(), target.face())) {
                pinned.add(target);
            }
        }
        return pinned;
    }

    @Nullable
    public <T> T neighbourCapability(BlockCapability<T, Direction> capability, BlockPos pos, Direction face) {
        if (level == null || !level.isLoaded(pos)) {
            return null;
        }
        return level.getCapability(capability, pos, face);
    }

    private void extractEnergy(Direction side, SideConfig config) {
        IEnergyStorage source = neighbourCapability(Capabilities.EnergyStorage.BLOCK,
                worldPosition.relative(side), side.getOpposite());
        if (source == null || !source.canExtract()) {
            return;
        }
        int available = source.extractEnergy(config.tier().energyRate(), true);
        if (available <= 0) {
            return;
        }
        int moved = pushEnergy(side, config, available, false);
        if (moved > 0) {
            source.extractEnergy(moved, false);
        }
    }

    public int pushEnergy(Direction side, SideConfig config, int budget, boolean simulate) {
        List<Target> targets = targets(side);
        if (targets.isEmpty() || budget <= 0) {
            return 0;
        }
        List<Target> list = ordered(config, targets);
        int remaining = budget;
        boolean roundRobin = config.distribution() == DistributionMode.ROUND_ROBIN;
        int share = roundRobin ? Math.max(1, budget / list.size()) : budget;

        for (int pass = 0; pass < (roundRobin ? 2 : 1) && remaining > 0; pass++) {
            int cap = pass == 0 ? share : remaining;
            for (Target target : list) {
                if (remaining <= 0) {
                    break;
                }
                IEnergyStorage destination = neighbourCapability(Capabilities.EnergyStorage.BLOCK,
                        target.destination(), target.face());
                if (destination == null || !destination.canReceive()) {
                    continue;
                }
                remaining -= destination.receiveEnergy(Math.min(remaining, cap), simulate);
            }
        }
        if (roundRobin && !simulate) {
            config.nextRoundRobin(list.size());
        }
        return budget - remaining;
    }

    private void extractFluid(Direction side, SideConfig config) {
        IFluidHandler source = neighbourCapability(Capabilities.FluidHandler.BLOCK,
                worldPosition.relative(side), side.getOpposite());
        if (source == null) {
            return;
        }
        int remaining = config.tier().fluidRate();
        for (int tank = 0; tank < source.getTanks() && remaining > 0; tank++) {
            FluidStack inTank = source.getFluidInTank(tank);
            if (inTank.isEmpty() || !config.filter().allowsFluid(inTank)) {
                continue;
            }
            FluidStack drained = source.drain(inTank.copyWithAmount(remaining), IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) {
                continue;
            }
            int filled = pushFluid(side, config, drained, false);
            if (filled > 0) {
                source.drain(drained.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
                remaining -= filled;
            }
        }
    }

    public int pushFluid(Direction side, SideConfig config, FluidStack stack, boolean simulate) {
        List<Target> targets = targets(side);
        if (targets.isEmpty() || stack.isEmpty()) {
            return 0;
        }
        IFluidHandler.FluidAction action = simulate
                ? IFluidHandler.FluidAction.SIMULATE
                : IFluidHandler.FluidAction.EXECUTE;
        List<Target> list = pin(ordered(config, targets), config.filter().destinationForFluid(stack));
        if (list.isEmpty()) {
            return 0;
        }
        int remaining = stack.getAmount();
        boolean roundRobin = config.distribution() == DistributionMode.ROUND_ROBIN;
        int share = roundRobin ? Math.max(1, remaining / list.size()) : remaining;

        for (int pass = 0; pass < (roundRobin ? 2 : 1) && remaining > 0; pass++) {
            int cap = pass == 0 ? share : remaining;
            for (Target target : list) {
                if (remaining <= 0) {
                    break;
                }
                IFluidHandler destination = neighbourCapability(Capabilities.FluidHandler.BLOCK,
                        target.destination(), target.face());
                if (destination == null) {
                    continue;
                }
                remaining -= destination.fill(stack.copyWithAmount(Math.min(remaining, cap)), action);
            }
        }
        if (roundRobin && !simulate) {
            config.nextRoundRobin(list.size());
        }
        return stack.getAmount() - remaining;
    }

    private void extractItems(Direction side, SideConfig config) {
        IItemHandler source = neighbourCapability(Capabilities.ItemHandler.BLOCK,
                worldPosition.relative(side), side.getOpposite());
        if (source == null) {
            return;
        }
        int remaining = config.tier().itemCount();
        for (int slot = 0; slot < source.getSlots() && remaining > 0; slot++) {
            ItemStack candidate = source.extractItem(slot, remaining, true);
            if (candidate.isEmpty() || !config.filter().allowsItem(candidate)) {
                continue;
            }
            int inserted = candidate.getCount() - pushItems(side, config, candidate, false).getCount();
            if (inserted > 0) {
                source.extractItem(slot, inserted, false);
                remaining -= inserted;
            }
        }
    }

    public ItemStack pushItems(Direction side, SideConfig config, ItemStack stack, boolean simulate) {
        List<Target> targets = targets(side);
        if (targets.isEmpty() || stack.isEmpty()) {
            return stack;
        }
        List<Target> list = pin(ordered(config, targets), config.filter().destinationForItem(stack));
        if (list.isEmpty()) {
            return stack;
        }
        ItemStack left = stack.copy();
        boolean roundRobin = config.distribution() == DistributionMode.ROUND_ROBIN;
        int share = roundRobin ? Math.max(1, stack.getCount() / list.size()) : stack.getCount();

        for (int pass = 0; pass < (roundRobin ? 2 : 1) && !left.isEmpty(); pass++) {
            int cap = pass == 0 ? share : left.getCount();
            for (Target target : list) {
                if (left.isEmpty()) {
                    break;
                }
                IItemHandler destination = neighbourCapability(Capabilities.ItemHandler.BLOCK,
                        target.destination(), target.face());
                if (destination == null) {
                    continue;
                }
                int offered = Math.min(left.getCount(), cap);
                ItemStack rejected = ItemHandlerHelper.insertItem(destination, left.copyWithCount(offered), simulate);
                left.shrink(offered - rejected.getCount());
            }
        }
        if (roundRobin && !simulate) {
            config.nextRoundRobin(list.size());
        }
        return left;
    }

    public void refreshConnections() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof CableBlock)) {
            return;
        }
        if (refreshing) {
            refreshQueued = true;
            return;
        }
        refreshing = true;
        try {
            do {
                refreshQueued = false;
                if (applyConnections()) {
                    CableNetwork.invalidate();
                    setChanged();
                }
            } while (refreshQueued);
        } finally {
            refreshing = false;
        }
    }

    private boolean applyConnections() {
        BlockState state = getBlockState();
        BlockState updated = state;
        boolean extract = false;

        for (Direction direction : Direction.values()) {
            extract |= container(direction) && anyExtract(direction);
            updated = updated.setValue(CableBlock.PROPERTY_BY_DIRECTION.get(direction), linked(direction));
        }

        boolean changed = hasExtract != extract;
        hasExtract = extract;
        if (updated != state) {
            level.setBlock(worldPosition, updated, 3);
            changed = true;
        }
        return changed;
    }

    public boolean cableNeighbour(Direction direction) {
        return level != null
                && level.getBlockEntity(worldPosition.relative(direction)) instanceof CableBlockEntity cable
                && cable.type().connectsTo(type())
                && !config(direction).severed()
                && !cable.config(direction.getOpposite()).severed();
    }

    public boolean severed(Direction direction) {
        return config(direction).severed();
    }

    public void setSevered(Direction direction, boolean severed) {
        config(direction).setSevered(severed);
        CableNetwork.invalidate();
        refreshConnections();
        onConfigChanged();
        if (level != null && level.getBlockEntity(worldPosition.relative(direction)) instanceof CableBlockEntity cable) {
            cable.refreshConnections();
            cable.onConfigChanged();
        }
    }

    public boolean container(Direction direction) {
        return level != null
                && !(level.getBlockEntity(worldPosition.relative(direction)) instanceof CableBlockEntity)
                && canReach(direction);
    }

    public boolean linked(Direction direction) {
        return cableNeighbour(direction) || container(direction);
    }

    public boolean canReach(Direction direction) {
        if (level == null) {
            return false;
        }
        BlockPos neighbour = worldPosition.relative(direction);
        if (level.getBlockEntity(neighbour) instanceof CableBlockEntity cable) {
            return cable.type().connectsTo(type());
        }
        Direction face = direction.getOpposite();
        TransferType type = type();
        if (type.carries(TransferType.ENERGY)
                && neighbourCapability(Capabilities.EnergyStorage.BLOCK, neighbour, face) != null) {
            return true;
        }
        if (type.carries(TransferType.FLUID)
                && neighbourCapability(Capabilities.FluidHandler.BLOCK, neighbour, face) != null) {
            return true;
        }
        if (type.carries(TransferType.ITEM)
                && neighbourCapability(Capabilities.ItemHandler.BLOCK, neighbour, face) != null) {
            return true;
        }
        if (type.carries(TransferType.SOURCE) && SourceBridge.get().present(level, neighbour, face)) {
            return true;
        }
        return type.carries(TransferType.GAS) && ChemicalBridge.get().present(level, neighbour, face);
    }

    public void setRetrieve(Direction direction, boolean retrieve) {
        config(direction).setRetrieve(retrieve);
        onConfigChanged();
    }

    public void setSideMode(Direction direction, ConnectionMode mode) {
        setSideMode(direction, primary(), mode);
    }

    public void setSideMode(Direction direction, TransferType cargo, ConnectionMode mode) {
        config(direction, cargo).setMode(mode);
        CableNetwork.invalidate();
        refreshConnections();
        if (level != null && level.getBlockEntity(worldPosition.relative(direction)) instanceof CableBlockEntity cable) {
            cable.refreshConnections();
        }
    }

    public void onConfigChanged() {
        CableNetwork.invalidate();
        boolean extract = false;
        for (Direction direction : Direction.values()) {
            extract |= container(direction) && anyExtract(direction);
        }
        hasExtract = extract;
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            CableNetwork.invalidate();
            refreshConnections();
        }
    }

    @Nullable
    public BlockState facade(Direction direction) {
        return facades[direction.ordinal()];
    }

    public boolean hasAnyFacade() {
        for (BlockState facade : facades) {
            if (facade != null) {
                return true;
            }
        }
        return false;
    }

    public void setFacade(Direction direction, @Nullable BlockState facade) {
        facades[direction.ordinal()] = facade;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(FacadeProperties.FACADES, facades.clone()).build();
    }

    /**
     * Facades live in the block entity, not in the block state, so the section has to be told to
     * rebuild by hand. Handing {@code setBlocksDirty} the same state twice is not enough: it asks the
     * model manager whether the change is worth redrawing, and one state against itself never is. Air
     * as the old state is always worth redrawing, which is the point.
     */
    private void refreshClientModel() {
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        refreshClientModel();
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        refreshClientModel();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag sides = new CompoundTag();
        for (Direction direction : Direction.values()) {
            CompoundTag perType = new CompoundTag();
            for (TransferType cargo : TransferType.values()) {
                perType.put(cargo.getSerializedName(), configs.get(direction).get(cargo).save(registries));
            }
            sides.put(direction.getSerializedName(), perType);
        }
        tag.put("Sides", sides);

        if (hasAnyFacade()) {
            CompoundTag covers = new CompoundTag();
            for (Direction direction : Direction.values()) {
                BlockState facade = facades[direction.ordinal()];
                if (facade != null) {
                    covers.put(direction.getSerializedName(), NbtUtils.writeBlockState(facade));
                }
            }
            tag.put("Facades", covers);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag sides = tag.getCompound("Sides");
        boolean extract = false;
        for (Direction direction : Direction.values()) {
            CompoundTag perType = sides.getCompound(direction.getSerializedName());
            for (TransferType cargo : TransferType.values()) {
                SideConfig config = configs.get(direction).get(cargo);
                // Worlds written before the universal cable gained a setting per cargo kind have a
                // single block here; it seeds every kind so nothing is lost.
                if (perType.contains(cargo.getSerializedName())) {
                    config.load(perType.getCompound(cargo.getSerializedName()), registries);
                } else if (perType.contains("Mode")) {
                    config.load(perType, registries);
                }
                extract |= config.mode() == ConnectionMode.EXTRACT;
            }
        }
        hasExtract = extract;

        CompoundTag covers = tag.getCompound("Facades");
        HolderGetter<Block> blocks = registries.lookupOrThrow(Registries.BLOCK);
        for (Direction direction : Direction.values()) {
            String key = direction.getSerializedName();
            facades[direction.ordinal()] = covers.contains(key)
                    ? NbtUtils.readBlockState(blocks, covers.getCompound(key))
                    : null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
