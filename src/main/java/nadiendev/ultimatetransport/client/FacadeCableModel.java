package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.facade.FacadeProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A covered cable looks like the block covering it, the way Ender IO's conduit facades do: the block
 * takes the covering's whole model, so its textures, tints, render type and shape all come along and
 * nothing of the cable shows through. Taking the facade off with the configurator gives the cable back.
 */
public class FacadeCableModel extends BakedModelWrapper<BakedModel> {

    public FacadeCableModel(BakedModel original) {
        super(original);
    }

    public static void clearCache() {
    }

    @Nullable
    private static BlockState cover(ModelData data) {
        BlockState[] facades = data.get(FacadeProperties.FACADES);
        if (facades == null) {
            return null;
        }
        for (BlockState facade : facades) {
            if (facade != null && !facade.isAir()) {
                return facade;
            }
        }
        return null;
    }

    private static BakedModel modelOf(BlockState facade) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(facade);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                    ModelData data, @Nullable RenderType renderType) {
        BlockState facade = cover(data);
        if (facade == null) {
            return originalModel.getQuads(state, side, random, data, renderType);
        }
        BakedModel model = modelOf(facade);
        if (renderType != null && !model.getRenderTypes(facade, random, ModelData.EMPTY).contains(renderType)) {
            return List.of();
        }
        return model.getQuads(facade, side, random, ModelData.EMPTY, renderType);
    }

    @Override
    public ModelData getModelData(net.minecraft.world.level.BlockAndTintGetter level,
                                  net.minecraft.core.BlockPos pos, BlockState state, ModelData data) {
        ModelData derived = originalModel.getModelData(level, pos, state, data);
        BlockState[] facades = data.get(FacadeProperties.FACADES);
        return facades == null ? derived : derived.derive().with(FacadeProperties.FACADES, facades).build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        BlockState facade = cover(data);
        return facade == null
                ? originalModel.getRenderTypes(state, random, data)
                : modelOf(facade).getRenderTypes(facade, random, ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState facade = cover(data);
        return facade == null
                ? originalModel.getParticleIcon(data)
                : modelOf(facade).getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }
}
