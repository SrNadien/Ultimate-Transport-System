package nadiendev.ultimatetransport.tube.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import nadiendev.ultimatetransport.tube.TubeConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import nadiendev.ultimatetransport.tube.block.BlockStation;
import nadiendev.ultimatetransport.tube.block.BlockStationHorizontal;

/**
 * Port of RenderStation.renderWorldBlock, shared by the vertical and horizontal
 * station. The original draws two boxes through renderStandardBlock:
 *
 *   1. the inset inner box, with setRenderFromInside(true) and (for the horizontal
 *      station) uvRotate 3 on the bottom and the four lateral faces
 *   2. the full 0..1 outer cube, drawn normally
 *
 * Both go through the block's face-cull hook, so quads are emitted per side and
 * vanilla applies the cull.
 */
public class StationBakedModel implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet TRANSLUCENT = ChunkRenderTypeSet.of(RenderType.translucent());
    private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutoutMipped());

    /**
     * Glass only looks like glass in the translucent pass. Cutout throws away everything that is not
     * fully opaque or fully clear, which turns a tinted pane into a solid wall, so the config decides
     * and translucent is the default.
     */
    private static ChunkRenderTypeSet layers() {
        return TubeConfig.renderPass == TubeConfig.RenderPass.CUTOUT ? CUTOUT : TRANSLUCENT;
    }


    private final Function<String, TextureAtlasSprite> sprites;

    public StationBakedModel(Function<String, TextureAtlasSprite> sprites) {
        this.sprites = sprites;
    }

    private TextureAtlasSprite get(String name) {
        return sprites.apply(name);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        StationRenderData data;
        if (state.getBlock() instanceof BlockStation) {
            data = StationRenderData.vertical(level, pos, state);
        } else if (state.getBlock() instanceof BlockStationHorizontal) {
            data = StationRenderData.horizontal(level, pos, state);
        } else {
            return modelData;
        }
        return modelData.derive().with(StationRenderData.PROPERTY, data).build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return layers();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType) {
        StationRenderData d = extraData.get(StationRenderData.PROPERTY);
        if (side == null) {
            List<BakedQuad> inner = new ArrayList<>();
            if (d == null) {
                // item / fallback: plain cube with the side skin
                TextureAtlasSprite base = get("ultimatetransport:block/station_side2");
                for (int s = 0; s < 6; s++) {
                    QuadBaker.addBoxFace(inner, Direction.from3DDataValue(s), base, 0, 0, 0, 1, 1, 1);
                }
                return inner;
            }
            // The inner skin is what you see while you are stood in the station, and a solid block
            // outside must not take it away with the outer face.
            for (int s = 0; s < 6; s++) {
                if (flush(d, Direction.from3DDataValue(s))) {
                    continue;
                }
                QuadBaker.addBoxFace(inner, Direction.from3DDataValue(s), get(d.sprite[s]),
                        d.minX, d.minY, d.minZ, d.maxX, d.maxY, d.maxZ, true, false, d.uvRotation[s]);
            }
            return inner;
        }
        int s = side.get3DDataValue();
        List<BakedQuad> q = new ArrayList<>();
        String name = d != null ? d.sprite[s] : "ultimatetransport:block/station_side2";
        TextureAtlasSprite sprite = get(name);

        // outer cube
        QuadBaker.addBoxFace(q, side, sprite, 0, 0, 0, 1, 1, 1);
        return q;
    }

    private static boolean flush(StationRenderData d, Direction side) {
        return switch (side) {
            case DOWN -> d.minY <= 0.0;
            case UP -> d.maxY >= 1.0;
            case NORTH -> d.minZ <= 0.0;
            case SOUTH -> d.maxZ >= 1.0;
            case WEST -> d.minX <= 0.0;
            case EAST -> d.maxX >= 1.0;
        };
    }

    @Override
    public boolean useAmbientOcclusion() {
        // Off for the same reason as TubeBakedModel: smooth lighting darkens the inset inner box
        // against solid neighbours. The per-face shade comes from the quads' shade flag instead.
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return get("ultimatetransport:block/station_side2");
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
