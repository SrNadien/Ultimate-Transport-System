package nadiendev.ultimatetransport.tube.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import nadiendev.ultimatetransport.tube.TubeConfig;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import nadiendev.ultimatetransport.tube.util.ConnectedTextures;
import nadiendev.ultimatetransport.tube.util.IConnectable;
import nadiendev.ultimatetransport.tube.util.Utilities;

/**
 * The 1.12.2 block kept its direction in metadata 0..5; here it is the {@code facing} property,
 * so {@link #metaOf(BlockState)} is the bridge every ported table still uses.
 */
public class BlockTube extends Block implements IConnectable {
    public static final MapCodec<BlockTube> CODEC = simpleCodec(BlockTube::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final ConnectedTextures[] textures = new ConnectedTextures[12];

    public BlockTube(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
        for (int i = 0; i < 6; i++) {
            this.textures[i] = new ConnectedTextures("ultimatetransport:block/tube" + i + "/%s", this, i);
        }
        for (int i = 0; i < 6; i++) {
            this.textures[i + 6] = new ConnectedTextures("ultimatetransport:block/tube/%s", this, i);
        }
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** 1.12.2 metadata of this state: the facing index, 0..5. */
    public static int metaOf(BlockState state) {
        return state.getValue(FACING).get3DDataValue();
    }

    public ConnectedTextures[] getTextures() {
        return textures;
    }

    /** Mirrors BlockTube.func_149673_e: cap faces (along the axis) use tube/, lateral use tube{dir}. */
    public ConnectedTextures pickSet(int meta, Direction axis, int s) {
        boolean cap;
        if (axis == Direction.UP || axis == Direction.DOWN) {
            cap = (s == 0 || s == 1);
        } else if (axis == Direction.NORTH || axis == Direction.SOUTH) {
            cap = (s == 2 || s == 3);
        } else {
            cap = (s == 4 || s == 5);
        }
        return cap ? textures[meta + 6] : textures[meta];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // ItemTube decides the real direction; this is only the fallback for other placers.
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    /**
     * Port of the 1.7.10 face cull.
     *
     * DEVIATION: the original tests the same block AND the same direction, this tests the block
     * only. Strict leaves a corner such as UP into NORTH with both tubes drawing a full face on
     * the shared plane, and the inner boxes are already flush there, so four coplanar quads
     * z-fight. Collision already treats the pair as connected and lets you ride the corner, so
     * the wall it drew was one you could walk through anyway.
     */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        if (!adjacentState.is(this)) {
            return false;
        }
        return !TubeConfig.lenientCulling || joins(state, adjacentState, side);
    }

    private static boolean joins(BlockState state, BlockState neighbour, Direction side) {
        Direction facing = state.getValue(FACING);
        return side.getAxis() == facing.getAxis() || neighbour.getValue(FACING) != facing;
    }

    private boolean travelConnected(BlockGetter level, BlockPos pos, BlockState state, Direction side) {
        BlockState neighbour = level.getBlockState(pos.relative(side));
        return neighbour.is(this) && (!TubeConfig.lenientCulling || joins(state, neighbour, side));
    }

    // --- entity physics inside the tube ---
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity == null) {
            return;
        }
        Utilities.entityAccelerate(entity, state.getValue(FACING));
        Utilities.entityLimitSpeed(entity);
        Utilities.entityResetFall(entity);
        Utilities.entityResetWalk(entity);
    }

    /** Outline and ray trace: the walls the original collisionRayTrace built, 0.05 thick. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (Direction d : Direction.values()) {
            if (!travelConnected(level, pos, state, d)) {
                shape = Shapes.or(shape, Utilities.getThinPart(d));
            }
        }
        return shape;
    }

    /** A wall on every side but the travel axis, the ceiling, and a floor the ride drops through. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Entity entity = context instanceof EntityCollisionContext ctx ? ctx.getEntity() : null;
        if (entity == null) {
            // The original only ever added boxes for a colliding entity.
            return Shapes.empty();
        }
        Direction dir = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();
        for (Direction d : Direction.values()) {
            if (d.getAxis() == dir.getAxis() || d == Direction.UP) {
                continue;
            }
            if (d == Direction.DOWN && travelConnected(level, pos, state, d)) {
                continue;
            }
            shape = Shapes.or(shape, Utilities.getCollisionBoxPart(d));
        }
        return shape;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    /** As in 1.12.2, picking a placed tube hands back the undirected item. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(TubeRegistration.TUBE_ITEM.get());
    }

    // --- IConnectable ---
    @Override
    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction d) {
        return level.getBlockState(pos.relative(d)).is(this);
    }

    @Override
    public boolean canConnectToStrict(BlockGetter level, BlockPos pos, Direction d) {
        BlockState other = level.getBlockState(pos.relative(d));
        return other.is(this) && metaOf(other) == metaOf(level.getBlockState(pos));
    }
}
