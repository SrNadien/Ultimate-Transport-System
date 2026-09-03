package nadiendev.ultimatetransport.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.item.ConfiguratorMode;
import nadiendev.ultimatetransport.menu.SideConfigTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

/**
 * While a configurate mode is in hand, the block you are looking at wears its own settings: one
 * translucent pane per face, in the colour that face is set to, so a run can be read without opening
 * anything.
 */
@EventBusSubscriber(modid = UltimateTransport.MODID, value = Dist.CLIENT)
public final class ConfiguratorOverlay {

    private static final float OUT = 0.004F;
    private static final float ALPHA = 0.45F;

    private ConfiguratorOverlay() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        ItemStack stack = ConfiguratorInput.held(player);
        if (stack.isEmpty()) {
            return;
        }
        ConfiguratorMode mode = ConfiguratorItem.mode(stack);
        if (!mode.configurating()) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockEntity target = minecraft.level.getBlockEntity(pos);
        TransferType cargo = mode.cargo();
        if (target == null || !SideConfigTarget.supports(target) || !SideConfigTarget.handles(target, cargo)) {
            return;
        }
        List<SideConfigTarget.Option> options = SideConfigTarget.options(target, cargo);
        if (options.isEmpty()) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer panes = buffers.getBuffer(RenderType.debugQuads());
        for (Direction side : Direction.values()) {
            if (!SideConfigTarget.enabled(target, side, cargo)) {
                continue;
            }
            int index = SideConfigTarget.index(target, side, cargo);
            if (index >= options.size()) {
                continue;
            }
            pane(poses, panes, side, options.get(index).colour());
        }
        buffers.endBatch(RenderType.debugQuads());
        poses.popPose();
    }

    /**
     * One face of the block, nudged clear of it so it does not fight the block's own surface. The
     * quad buffer is the one that matters here: the filled-box type draws triangle strips, which turns
     * four corners into a wedge across the face.
     */
    private static void pane(PoseStack poses, VertexConsumer panes, Direction side, int colour) {
        float red = ((colour >> 16) & 0xFF) / 255.0F;
        float green = ((colour >> 8) & 0xFF) / 255.0F;
        float blue = (colour & 0xFF) / 255.0F;
        float lo = -OUT;
        float hi = 1.0F + OUT;

        float[][] corners = switch (side) {
            case DOWN -> new float[][]{{lo, lo, lo}, {hi, lo, lo}, {hi, lo, hi}, {lo, lo, hi}};
            case UP -> new float[][]{{lo, hi, lo}, {lo, hi, hi}, {hi, hi, hi}, {hi, hi, lo}};
            case NORTH -> new float[][]{{lo, lo, lo}, {lo, hi, lo}, {hi, hi, lo}, {hi, lo, lo}};
            case SOUTH -> new float[][]{{lo, lo, hi}, {hi, lo, hi}, {hi, hi, hi}, {lo, hi, hi}};
            case WEST -> new float[][]{{lo, lo, lo}, {lo, lo, hi}, {lo, hi, hi}, {lo, hi, lo}};
            case EAST -> new float[][]{{hi, lo, lo}, {hi, hi, lo}, {hi, hi, hi}, {hi, lo, hi}};
        };
        var pose = poses.last().pose();
        for (float[] corner : corners) {
            panes.addVertex(pose, corner[0], corner[1], corner[2]).setColor(red, green, blue, ALPHA);
        }
    }
}
