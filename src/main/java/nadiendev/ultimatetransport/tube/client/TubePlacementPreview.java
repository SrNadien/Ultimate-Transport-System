package nadiendev.ultimatetransport.tube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.tube.TubeConfig;
import nadiendev.ultimatetransport.tube.item.ItemTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = UltimateTransport.MODID, value = Dist.CLIENT)
public final class TubePlacementPreview {

    private static final float INSET = 0.02F;

    private TubePlacementPreview() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !TubeConfig.placementPreview) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        ItemTube tube = held(player, InteractionHand.MAIN_HAND);
        if (tube == null) {
            tube = held(player, InteractionHand.OFF_HAND);
        }
        if (tube == null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos target = hit.getBlockPos();
        if (!minecraft.level.getBlockState(target).canBeReplaced()) {
            target = target.relative(hit.getDirection());
        }
        if (!minecraft.level.getBlockState(target).canBeReplaced()) {
            return;
        }

        Direction facing = tube.getDirection() == ItemTube.UNDIRECTED
                ? hit.getDirection()
                : Direction.from3DDataValue(tube.getDirection());

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(target.getX() - camera.x, target.getY() - camera.y, target.getZ() - camera.z);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        outline(poses, lines);
        arrow(poses, lines, facing);
        buffers.endBatch(RenderType.lines());
        poses.popPose();
    }

    private static ItemTube held(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return stack.getItem() instanceof ItemTube tube ? tube : null;
    }

    private static void outline(PoseStack poses, VertexConsumer lines) {
        float lo = INSET;
        float hi = 1.0F - INSET;
        float[][] corners = {
                {lo, lo, lo}, {hi, lo, lo}, {hi, lo, hi}, {lo, lo, hi},
                {lo, hi, lo}, {hi, hi, lo}, {hi, hi, hi}, {lo, hi, hi}
        };
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (int[] edge : edges) {
            segment(poses, lines, corners[edge[0]], corners[edge[1]], 0.31F, 0.89F, 0.78F, 0.55F);
        }
    }

    private static void arrow(PoseStack poses, VertexConsumer lines, Direction facing) {
        float[] centre = {0.5F, 0.5F, 0.5F};
        float[] tip = {
                0.5F + facing.getStepX() * 0.42F,
                0.5F + facing.getStepY() * 0.42F,
                0.5F + facing.getStepZ() * 0.42F
        };
        segment(poses, lines, centre, tip, 1.0F, 0.86F, 0.29F, 0.95F);

        Direction.Axis axis = facing.getAxis();
        Direction[] spread = axis == Direction.Axis.Y
                ? new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}
                : axis == Direction.Axis.X
                ? new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}
                : new Direction[]{Direction.UP, Direction.DOWN, Direction.WEST, Direction.EAST};
        for (Direction side : spread) {
            float[] barb = {
                    tip[0] - facing.getStepX() * 0.18F + side.getStepX() * 0.14F,
                    tip[1] - facing.getStepY() * 0.18F + side.getStepY() * 0.14F,
                    tip[2] - facing.getStepZ() * 0.18F + side.getStepZ() * 0.14F
            };
            segment(poses, lines, tip, barb, 1.0F, 0.86F, 0.29F, 0.95F);
        }
    }

    private static void segment(PoseStack poses, VertexConsumer lines, float[] from, float[] to,
                                float red, float green, float blue, float alpha) {
        var pose = poses.last();
        float dx = to[0] - from[0];
        float dy = to[1] - from[1];
        float dz = to[2] - from[2];
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0F) {
            return;
        }
        dx /= length;
        dy /= length;
        dz /= length;
        lines.addVertex(pose, from[0], from[1], from[2]).setColor(red, green, blue, alpha).setNormal(pose, dx, dy, dz);
        lines.addVertex(pose, to[0], to[1], to[2]).setColor(red, green, blue, alpha).setNormal(pose, dx, dy, dz);
    }
}
