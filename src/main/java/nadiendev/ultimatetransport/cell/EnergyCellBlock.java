package nadiendev.ultimatetransport.cell;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatetransport.compat.camol.CamolCompat;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.menu.CellMenu;
import nadiendev.ultimatetransport.registry.UTBlockEntities;
import nadiendev.ultimatetransport.registry.UTDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EnergyCellBlock extends BaseEntityBlock {

    public static final MapCodec<EnergyCellBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(EnergyCellTier::values).fieldOf("tier").forGetter(EnergyCellBlock::tier),
            propertiesCodec()
    ).apply(instance, EnergyCellBlock::new));

    /** How many of the four bars on the casing are lit, and how brightly the cell glows. */
    public static final int STEPS = 8;

    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, STEPS);

    /** Set on a side that has a battery of the same rung against it, so no rim is drawn there. */
    public static final Map<Direction, BooleanProperty> JOINED = Map.of(
            Direction.NORTH, BooleanProperty.create("north"),
            Direction.SOUTH, BooleanProperty.create("south"),
            Direction.EAST, BooleanProperty.create("east"),
            Direction.WEST, BooleanProperty.create("west"),
            Direction.UP, BooleanProperty.create("up"),
            Direction.DOWN, BooleanProperty.create("down"));

    private final EnergyCellTier tier;

    public EnergyCellBlock(EnergyCellTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        BlockState base = stateDefinition.any().setValue(CHARGE, 0);
        for (BooleanProperty joined : JOINED.values()) {
            base = base.setValue(joined, false);
        }
        registerDefaultState(base);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
        JOINED.values().forEach(builder::add);
    }

    /** Rungs over the whole range, so an empty battery is dark and a full one is lit end to end. */
    public static int chargeLevel(double fillRatio) {
        if (fillRatio <= 0.0) {
            return 0;
        }
        return Math.clamp(1 + (int) (fillRatio * (STEPS - 0.001)), 1, STEPS);
    }

    public EnergyCellTier tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCellBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, UTBlockEntities.ENERGY_CELL.get(), EnergyCellBlockEntity::serverTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof EnergyCellBlockEntity cell && cell.stored() > 0) {
            return 1 + (int) (cell.fillRatio() * 14.0);
        }
        return 0;
    }

    /** The configurator has to reach the cell, so it is not swallowed by the charge readout. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return stack.getItem() instanceof ConfiguratorItem || CamolCompat.isCamoTool(stack)
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof EnergyCellBlockEntity cell && player instanceof ServerPlayer viewer) {
            viewer.openMenu(new SimpleMenuProvider(
                    (id, inventory, opener) -> new CellMenu(id, inventory, cell), getName()), buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    /** A broken cell keeps its charge, so moving a bank around does not throw the power away. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof EnergyCellBlockEntity cell
                && cell.ownStored() > 0) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    drop.set(UTDataComponents.STORED_ENERGY.get(), cell.ownStored());
                }
            }
        }
        return drops;
    }

    /** A rim is drawn on every side that is not answered by another battery of the same rung. */
    private BlockState joins(BlockState state, LevelReader level, BlockPos pos) {
        BlockState result = state;
        for (Map.Entry<Direction, BooleanProperty> entry : JOINED.entrySet()) {
            boolean joined = level.getBlockState(pos.relative(entry.getKey())).getBlock() == this;
            result = result.setValue(entry.getValue(), joined);
        }
        return result;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return joins(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        BooleanProperty property = JOINED.get(direction);
        return property == null ? state : state.setValue(property, neighbour.getBlock() == this);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        CellBank.invalidate();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (!state.is(replacement.getBlock())) {
            CellBank.invalidate();
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CellBank.invalidate();
        Long charge = stack.get(UTDataComponents.STORED_ENERGY.get());
        if (charge != null && level.getBlockEntity(pos) instanceof EnergyCellBlockEntity cell) {
            cell.setStored(charge);
        }
    }

    public static Block[] blocks(List<? extends EnergyCellBlock> cells) {
        return cells.toArray(Block[]::new);
    }
}
