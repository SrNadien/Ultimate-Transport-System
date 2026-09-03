package nadiendev.ultimatetransport.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlockEntity;
import nadiendev.ultimatetransport.cable.ConnectionMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Draws the collar that says what a cable face is doing. The mode lives on the block entity rather
 * than in the block state, so it is drawn here instead of baked into the chunk mesh: putting four
 * modes on six faces into the state table would mean thousands of block states per cable.
 */
public class CableRenderer implements BlockEntityRenderer<CableBlockEntity> {

    public static final ModelResourceLocation EXTRACT =
            ModelResourceLocation.standalone(UltimateTransport.id("block/cable_extract"));

    private final RandomSource random = RandomSource.create();

    public CableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBlockEntity cable, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        // Only extraction gets a marker. Insert is the default for any face touching a machine, so
        // marking it would collar nearly every cable in a base and say nothing.
        List<BakedQuad> extract = null;
        VertexConsumer consumer = null;

        for (Direction side : Direction.values()) {
            if (cable.config(side).mode() != ConnectionMode.EXTRACT || !cable.container(side)) {
                continue;
            }
            if (consumer == null) {
                consumer = buffers.getBuffer(RenderType.solid());
                extract = quads(EXTRACT);
            }
            draw(side, pose, consumer, extract, light, overlay);
        }
    }

    private List<BakedQuad> quads(ModelResourceLocation model) {
        BakedModel baked = Minecraft.getInstance().getModelManager().getModel(model);
        return baked.getQuads(null, null, random, ModelData.EMPTY, RenderType.solid());
    }

    private void draw(Direction side, PoseStack pose, VertexConsumer consumer,
                      List<BakedQuad> quads, int light, int overlay) {
        pose.pushPose();
        // A hair proud of the face, so the collar never fights with the arm underneath it.
        pose.translate(side.getStepX() * 0.001D, side.getStepY() * 0.001D, side.getStepZ() * 0.001D);
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(rotation(side));
        pose.translate(-0.5D, -0.5D, -0.5D);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose.last(), quad, 1F, 1F, 1F, 1F, light, overlay);
        }
        pose.popPose();
    }

    /** The models are authored facing north; every other face is that model turned to suit. */
    private static Quaternionf rotation(Direction side) {
        Quaternionf quaternion = new Quaternionf();
        switch (side) {
            case NORTH -> {
            }
            case SOUTH -> quaternion.mul(Axis.YP.rotationDegrees(180F));
            case WEST -> quaternion.mul(Axis.YP.rotationDegrees(90F));
            case EAST -> quaternion.mul(Axis.YP.rotationDegrees(270F));
            case UP -> quaternion.mul(Axis.XP.rotationDegrees(90F));
            case DOWN -> quaternion.mul(Axis.XP.rotationDegrees(270F));
        }
        return quaternion;
    }
}
