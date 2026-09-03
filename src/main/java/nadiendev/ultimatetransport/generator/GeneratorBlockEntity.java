package nadiendev.ultimatetransport.generator;

import nadiendev.ultimatetransport.cell.CellSideMode;
import nadiendev.ultimatetransport.config.UTServerConfig;
import nadiendev.ultimatetransport.registry.UTBlockEntities;
import nadiendev.ultimatetransport.registry.UTRecipes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class GeneratorBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int CHARGE_SLOT = 1;

    private final ItemStackHandler slots = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == CHARGE_SLOT
                    ? stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM) != null
                    : accepts(stack);
        }
    };

    private final Map<Direction, CellSideMode> sides = new EnumMap<>(Direction.class);

    private boolean autoEject = true;
    private int stored;
    private int burnTime;
    private int burnDuration;
    private int burnRate;
    private int lastSynced = -1;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(UTBlockEntities.GENERATOR.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sides.put(direction, CellSideMode.OUTPUT);
        }
    }

    public CellSideMode side(Direction direction) {
        return sides.getOrDefault(direction, CellSideMode.OUTPUT);
    }

    public void setSide(Direction direction, CellSideMode mode) {
        sides.put(direction, mode.output() ? CellSideMode.OUTPUT : CellSideMode.DISABLED);
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public GeneratorType type() {
        return getBlockState().getBlock() instanceof GeneratorBlock generator ? generator.type() : GeneratorType.FUEL;
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

    /** The generator tops up whatever charge-holding item sits in its second slot. */
    private void chargeHeldItem() {
        if (stored <= 0 || slots.getSlots() <= CHARGE_SLOT) {
            return;
        }
        var charging = slots.getStackInSlot(CHARGE_SLOT)
                .getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM);
        if (charging == null || !charging.canReceive()) {
            return;
        }
        int moved = charging.receiveEnergy(Math.min(stored, type().transfer()), false);
        if (moved > 0) {
            stored -= moved;
        }
    }

    public int stored() {
        return stored;
    }

    public int activeRate() {
        return burnRate > 0 ? burnRate : type().rate();
    }

    public boolean running() {
        return burnTime > 0;
    }

    public double fillRatio() {
        return (double) stored / type().capacity();
    }

    public double burnRatio() {
        if (!type().consumesItems()) {
            return running() ? 1.0 : 0.0;
        }
        return burnDuration <= 0 ? 0.0 : (double) burnTime / burnDuration;
    }

    public boolean accepts(ItemStack stack) {
        if (recipeFor(stack) != null) {
            return true;
        }
        return switch (type()) {
            case FUEL -> stack.getBurnTime(null) > 0;
            case LAVA -> stack.is(Items.LAVA_BUCKET) || FluidUtil.getFluidContained(stack)
                    .map(fluid -> fluid.getAmount() >= FluidType.BUCKET_VOLUME
                            && fluid.getFluid().defaultFluidState().is(FluidTags.LAVA))
                    .orElse(false);
            case NETHER_STAR -> stack.is(Items.NETHER_STAR);
            case SOLAR -> false;
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity generator) {
        boolean wasRunning = generator.running();
        generator.produce(level, pos);
        generator.chargeHeldItem();
        generator.distribute(level, pos);

        if (generator.running() && generator.type() == GeneratorType.NETHER_STAR) {
            generator.wither(level, pos);
        }
        if (state.getValue(GeneratorBlock.LIT) != generator.running()) {
            level.setBlock(pos, state.setValue(GeneratorBlock.LIT, generator.running()), 3);
        }
        if (wasRunning != generator.running() || generator.lastSynced != generator.stored) {
            generator.lastSynced = generator.stored;
            generator.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private void wither(Level level, BlockPos pos) {
        int radius = UTServerConfig.witherRadius();
        if (radius <= 0 || level.getGameTime() % 40L != 0L) {
            return;
        }
        AABB area = new AABB(pos).inflate(radius);
        int duration = UTServerConfig.witherSeconds() * 20;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0, false, true));
        }
    }

    private void produce(Level level, BlockPos pos) {
        GeneratorType type = type();
        if (type == GeneratorType.SOLAR) {
            burnDuration = 0;
            burnTime = level.isDay() && !level.isRaining() && level.canSeeSky(pos.above()) ? 1 : 0;
            burnRate = type.rate();
            if (burnTime > 0) {
                stored = Math.min(type.capacity(), stored + burnRate);
            }
            return;
        }

        if (burnTime > 0) {
            burnTime--;
            stored = Math.min(type.capacity(), stored + activeRate());
            return;
        }
        if (stored >= type.capacity()) {
            return;
        }

        ItemStack fuel = slots.getStackInSlot(FUEL_SLOT);
        GeneratorFuelRecipe recipe = recipeFor(fuel);
        int duration = recipe != null ? recipe.burnTime() : fuelValue(fuel);
        if (duration <= 0) {
            return;
        }
        burnDuration = duration;
        burnTime = duration;
        burnRate = recipe != null ? recipe.rateOr(type.rate()) : type.rate();

        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (!remainder.isEmpty() && fuel.isEmpty()) {
            slots.setStackInSlot(FUEL_SLOT, remainder);
        }
        setChanged();
    }

    private GeneratorFuelRecipe recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        for (RecipeHolder<GeneratorFuelRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(UTRecipes.GENERATOR_FUEL.get())) {
            GeneratorFuelRecipe recipe = holder.value();
            if (recipe.generator() == type() && recipe.fuel().test(stack)) {
                return recipe;
            }
        }
        return null;
    }

    private int fuelValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        GeneratorFuelRecipe recipe = recipeFor(stack);
        if (recipe != null) {
            return recipe.burnTime();
        }
        return switch (type()) {
            case FUEL -> stack.getBurnTime(null);
            case LAVA, NETHER_STAR -> accepts(stack) ? type().burnTicks() : 0;
            case SOLAR -> 0;
        };
    }

    private void distribute(Level level, BlockPos pos) {
        if (!autoEject || stored <= 0) {
            return;
        }
        int budget = Math.min(type().transfer(), stored);
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                break;
            }
            if (!side(direction).output()) {
                continue;
            }
            BlockPos neighbour = pos.relative(direction);
            if (!level.isLoaded(neighbour)) {
                continue;
            }
            IEnergyStorage destination = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    neighbour, direction.getOpposite());
            if (destination == null || !destination.canReceive()) {
                continue;
            }
            int moved = destination.receiveEnergy(budget, false);
            if (moved > 0) {
                budget -= moved;
                stored -= moved;
            }
        }
    }

    public IEnergyStorage energy() {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int amount, boolean simulate) {
                return 0;
            }

            @Override
            public int extractEnergy(int amount, boolean simulate) {
                int removed = Math.min(Math.min(amount, type().transfer()), stored);
                if (removed > 0 && !simulate) {
                    stored -= removed;
                }
                return Math.max(removed, 0);
            }

            @Override
            public int getEnergyStored() {
                return stored;
            }

            @Override
            public int getMaxEnergyStored() {
                return type().capacity();
            }

            @Override
            public boolean canExtract() {
                return true;
            }

            @Override
            public boolean canReceive() {
                return false;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", stored);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
        tag.putInt("BurnRate", burnRate);
        CompoundTag faces = new CompoundTag();
        for (Direction direction : Direction.values()) {
            faces.putString(direction.getSerializedName(), side(direction).getSerializedName());
        }
        tag.put("Sides", faces);
        tag.put("Slots", slots.serializeNBT(registries));
        tag.putBoolean("AutoEject", autoEject);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stored = tag.getInt("Energy");
        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
        burnRate = tag.getInt("BurnRate");
        CompoundTag faces = tag.getCompound("Sides");
        for (Direction direction : Direction.values()) {
            if (faces.contains(direction.getSerializedName())) {
                sides.put(direction, CellSideMode.byName(faces.getString(direction.getSerializedName())));
            }
        }
        if (tag.contains("Slots")) {
            slots.deserializeNBT(registries, tag.getCompound("Slots"));
            autoEject = !tag.contains("AutoEject") || tag.getBoolean("AutoEject");
            if (slots.getSlots() < 2) {
                slots.setSize(2);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
