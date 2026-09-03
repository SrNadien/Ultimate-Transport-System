package nadiendev.ultimatetransport.cable;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatetransport.compat.camol.CamolCompat;
import nadiendev.ultimatetransport.facade.FacadeSupport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.facade.FacadeItem;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.menu.CableMenus;
import nadiendev.ultimatetransport.registry.UTItems;
import nadiendev.ultimatetransport.registry.UTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CableBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<CableBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TransferType.CODEC.fieldOf("transfer_type").forGetter(CableBlock::type),
            propertiesCodec()
    ).apply(instance, CableBlock::new));

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.<Direction, BooleanProperty>builder()
            .put(Direction.DOWN, DOWN)
            .put(Direction.UP, UP)
            .put(Direction.NORTH, NORTH)
            .put(Direction.SOUTH, SOUTH)
            .put(Direction.WEST, WEST)
            .put(Direction.EAST, EAST)
            .build();

    private static final VoxelShape CORE = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape[] ARMS = {
            Block.box(4, 0, 4, 12, 4, 12),
            Block.box(4, 12, 4, 12, 16, 12),
            Block.box(4, 4, 0, 12, 12, 4),
            Block.box(4, 4, 12, 12, 12, 16),
            Block.box(0, 4, 4, 4, 12, 12),
            Block.box(12, 4, 4, 16, 12, 12)
    };
    private static final VoxelShape[] SHAPES = buildShapes();
    private static final VoxelShape[] FACADE_SHAPES = {
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 15, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 1),
            Block.box(0, 0, 15, 16, 16, 16),
            Block.box(0, 0, 0, 1, 16, 16),
            Block.box(15, 0, 0, 16, 16, 16)
    };

    private final TransferType type;

    public CableBlock(TransferType type, Properties properties) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any()
                .setValue(DOWN, false).setValue(UP, false)
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(EAST, false)
                .setValue(WATERLOGGED, false));
    }

    public TransferType type() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST, WATERLOGGED);
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = CORE;
            for (int i = 0; i < 6; i++) {
                if ((mask & (1 << i)) != 0) {
                    shape = Shapes.or(shape, ARMS[i]);
                }
            }
            shapes[mask] = shape;
        }
        return shapes;
    }

    public static int mask(BlockState state) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return withFacades(SHAPES[mask(state)], level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return withFacades(SHAPES[mask(state)], level, pos);
    }

    private static VoxelShape withFacades(VoxelShape shape, BlockGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable) || !cable.hasAnyFacade()) {
            return shape;
        }
        VoxelShape result = shape;
        for (Direction direction : Direction.values()) {
            if (cable.facade(direction) != null) {
                result = Shapes.or(result, FACADE_SHAPES[direction.ordinal()]);
            }
        }
        return result;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static Direction pickSide(BlockState state, BlockPos pos, net.minecraft.world.phys.Vec3 hit, Direction clicked) {
        net.minecraft.world.phys.Vec3 local = hit.subtract(pos.getX(), pos.getY(), pos.getZ());
        for (Direction direction : Direction.values()) {
            if (!state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                continue;
            }
            if (ARMS[direction.ordinal()].bounds().inflate(0.002).contains(local)) {
                return direction;
            }
        }
        return clicked;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(WATERLOGGED, water);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide) {
            CableNetwork.invalidate();
            if (level.getBlockEntity(pos) instanceof CableBlockEntity cable) {
                cable.refreshConnections();
            }
        }
    }

    @Override
    public void onNeighborChange(BlockState state, net.minecraft.world.level.LevelReader level,
                                 BlockPos pos, BlockPos neighbour) {
        if (level instanceof Level actual && !actual.isClientSide) {
            CableNetwork.invalidate();
            if (actual.getBlockEntity(pos) instanceof CableBlockEntity cable) {
                cable.refreshConnections();
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            CableNetwork.invalidate();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ConfiguratorItem || stack.getItem() instanceof FacadeItem
                || CamolCompat.isCamoTool(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        BlockState foreign = FacadeSupport.foreignFacade(stack);
        if (foreign != null) {
            return cover(stack, state, level, pos, player, hit, foreign);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult cover(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                        Player player, BlockHitResult hit, BlockState facade) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Direction side = pickSide(state, pos, hit.getLocation(), hit.getDirection());
        if (cable.facade(side) != null) {
            return ItemInteractionResult.CONSUME;
        }
        cable.setFacade(side, facade);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, facade.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        Direction side = pickSide(state, pos, hit.getLocation(), hit.getDirection());
        if (!cable.container(side) || cable.config(side).mode() != ConnectionMode.EXTRACT) {
            return InteractionResult.PASS;
        }
        CableMenus.openCable(serverPlayer, cable, side);
        return InteractionResult.CONSUME;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CableBlockEntity cable) {
            for (Direction direction : Direction.values()) {
                ItemStack upgrade = cable.config(direction).upgrade();
                if (!upgrade.isEmpty()) {
                    drops.add(upgrade.copy());
                }
                BlockState facade = cable.facade(direction);
                if (facade != null) {
                    drops.add(FacadeItem.of(UTItems.FACADE.get(), facade));
                }
            }
        }
        return drops;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, UTBlockEntities.CABLE.get(), CableBlockEntity::serverTick);
    }
}
