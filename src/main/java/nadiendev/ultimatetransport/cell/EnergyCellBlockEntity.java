package nadiendev.ultimatetransport.cell;

import nadiendev.ultimatetransport.registry.UTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EnergyCellBlockEntity extends BlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int DISCHARGE_SLOT = 1;

    private final Map<Direction, CellSideMode> sides = new EnumMap<>(Direction.class);

    private final ItemStackHandler slots = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
        }
    };

    private List<EnergyCellBlockEntity> bank;
    private int bankRevision = -1;
    private boolean autoEject = true;
    private long stored;
    private long lastSynced = -1;
    private boolean dirty;

    public EnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(UTBlockEntities.ENERGY_CELL.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sides.put(direction, CellSideMode.BOTH);
        }
    }

    public EnergyCellTier tier() {
        return getBlockState().getBlock() instanceof EnergyCellBlock cell ? cell.tier() : EnergyCellTier.TIER_1;
    }

    public long ownStored() {
        return stored;
    }

    public long stored() {
        return CellBank.stored(bank());
    }

    public long capacity() {
        return CellBank.capacity(bank());
    }

    public int transfer() {
        return CellBank.transfer(bank());
    }

    public int bankSize() {
        return bank().size();
    }

    public List<EnergyCellBlockEntity> bank() {
        if (bank == null || bankRevision != CellBank.revision()) {
            bank = CellBank.gather(this);
            bankRevision = CellBank.revision();
        }
        return bank;
    }

    public void forgetBank() {
        bank = null;
    }

    /**
     * How much of this block's own window is lit. The bank is read as one column: the charge fills
     * it from the floor up, so a stack shows a single bar running through it rather than one gauge
     * per block, which is how Ender IO's capacitor banks read.
     */
    public int gauge() {
        List<EnergyCellBlockEntity> members = bank();
        int floor = Integer.MAX_VALUE;
        int ceiling = Integer.MIN_VALUE;
        for (EnergyCellBlockEntity member : members) {
            floor = Math.min(floor, member.getBlockPos().getY());
            ceiling = Math.max(ceiling, member.getBlockPos().getY());
        }
        int height = ceiling - floor + 1;
        double share = fillRatio() * height - (worldPosition.getY() - floor);
        return EnergyCellBlock.chargeLevel(Math.clamp(share, 0.0, 1.0));
    }

    public void setStored(long value) {
        stored = Math.clamp(value, 0L, tier().capacity());
    }

    public double fillRatio() {
        EnergyCellTier tier = tier();
        if (tier.creative()) {
            return 1.0;
        }
        if (tier.bottomless()) {
            return stored() > 0 ? 1.0 : 0.0;
        }
        long room = capacity();
        return room <= 0 ? 0.0 : (double) stored() / room;
    }

    public CellSideMode side(Direction direction) {
        return sides.get(direction);
    }

    public void setSide(Direction direction, CellSideMode mode) {
        sides.put(direction, mode);
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStackHandler slots() {
        return slots;
    }

    public boolean autoEject() {
        return autoEject;
    }

    public void setAutoEject(boolean value) {
        autoEject = value;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyCellBlockEntity cell) {
        cell.moveItemEnergy();
        if ((level.getGameTime() + Math.floorMod(pos.hashCode(), 200L)) % 200L == 0L) {
            CellBank.equalise(cell.bank());
        }
        cell.distribute();

        int lit = cell.gauge();
        if (state.getValue(EnergyCellBlock.CHARGE) != lit) {
            level.setBlock(pos, state.setValue(EnergyCellBlock.CHARGE, lit), 3);
        }
        if (cell.dirty) {
            cell.dirty = false;
            cell.setChanged();
        }
        if (cell.lastSynced != cell.stored) {
            cell.lastSynced = cell.stored;
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private void distribute() {
        boolean endless = tier().creative();
        if (!autoEject || level == null || (stored() <= 0 && !endless)) {
            return;
        }
        int budget = tier().transfer();
        for (Direction direction : Direction.values()) {
            if ((stored() <= 0 && !endless) || budget <= 0 || !sides.get(direction).output()) {
                continue;
            }
            BlockPos neighbour = worldPosition.relative(direction);
            if (!level.isLoaded(neighbour)) {
                continue;
            }
            if (level.getBlockEntity(neighbour) instanceof EnergyCellBlockEntity other
                    && (other.tier() == tier() || !feeds(other))) {
                continue;
            }
            IEnergyStorage destination = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    neighbour, direction.getOpposite());
            if (destination == null || !destination.canReceive()) {
                continue;
            }
            int offer = (int) Math.min(Math.min(endless ? tier().transfer() : stored(), budget), Integer.MAX_VALUE);
            int moved = destination.receiveEnergy(offer, false);
            if (moved > 0) {
                budget -= moved;
                if (!endless) {
                    CellBank.extract(bank(), moved, false);
                }
            }
        }
    }

    private boolean feeds(EnergyCellBlockEntity other) {
        if (tier().creative()) {
            return !other.tier().creative();
        }
        if (other.tier().creative()) {
            return false;
        }
        if (tier().bottomless() || other.tier().bottomless()) {
            return other.stored() < stored();
        }
        return other.fillRatio() < fillRatio();
    }

    private void moveItemEnergy() {
        boolean endless = tier().creative();
        IEnergyStorage charging = slots.getStackInSlot(CHARGE_SLOT).getCapability(Capabilities.EnergyStorage.ITEM);
        if (charging != null && charging.canReceive() && (endless || stored() > 0)) {
            int offer = (int) Math.min(Math.min(endless ? tier().transfer() : stored(), tier().transfer()),
                    Integer.MAX_VALUE);
            int moved = charging.receiveEnergy(offer, false);
            if (moved > 0 && !endless) {
                CellBank.extract(bank(), moved, false);
            }
        }

        IEnergyStorage draining = slots.getStackInSlot(DISCHARGE_SLOT).getCapability(Capabilities.EnergyStorage.ITEM);
        if (draining != null && draining.canExtract()) {
            long room = capacity() - stored();
            int want = (int) Math.min(Math.min(room, tier().transfer()), Integer.MAX_VALUE);
            int moved = want <= 0 ? 0 : draining.extractEnergy(want, false);
            if (moved > 0) {
                CellBank.receive(bank(), moved, false);
            }
        }
    }

    public long receiveOwn(long amount, boolean simulate) {
        EnergyCellTier tier = tier();
        if (tier.creative()) {
            return Math.max(amount, 0);
        }
        long accepted = Math.min(Math.min(amount, tier.transfer()), tier.capacity() - stored);
        if (accepted > 0 && !simulate) {
            stored += accepted;
            dirty = true;
        }
        return Math.max(accepted, 0);
    }

    public long extractOwn(long amount, boolean simulate) {
        EnergyCellTier tier = tier();
        if (tier.creative()) {
            return Math.max(Math.min(amount, tier.transfer()), 0);
        }
        long removed = Math.min(Math.min(amount, tier.transfer()), stored);
        if (removed > 0 && !simulate) {
            stored -= removed;
            dirty = true;
        }
        return Math.max(removed, 0);
    }

    public long receive(long amount, boolean simulate) {
        return CellBank.receive(bank(), amount, simulate);
    }

    public long extract(long amount, boolean simulate) {
        return CellBank.extract(bank(), amount, simulate);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Energy", stored);
        tag.putBoolean("AutoEject", autoEject);
        CompoundTag faces = new CompoundTag();
        for (Direction direction : Direction.values()) {
            faces.putString(direction.getSerializedName(), sides.get(direction).getSerializedName());
        }
        tag.put("Sides", faces);
        tag.put("Slots", slots.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = tag.getLong("Energy");
        autoEject = !tag.contains("AutoEject") || tag.getBoolean("AutoEject");
        CompoundTag faces = tag.getCompound("Sides");
        for (Direction direction : Direction.values()) {
            if (faces.contains(direction.getSerializedName())) {
                sides.put(direction, CellSideMode.byName(faces.getString(direction.getSerializedName())));
            }
        }
        if (tag.contains("Slots")) {
            slots.deserializeNBT(registries, tag.getCompound("Slots"));
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
