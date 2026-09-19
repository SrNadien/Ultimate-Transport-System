package nadiendev.ultimatetransport.client.cell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.cell.CellDisplayMode;
import nadiendev.ultimatetransport.cell.EnergyCellBlock;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class EnergyCellRenderer implements BlockEntityRenderer<EnergyCellBlockEntity> {

    private static final float OUT = 0.002F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GAINING = 0xFF3ED66A;
    private static final int LOSING = 0xFFE03030;
    private static final int STEADY = 0xFFD8D8D8;
    private static final int REACH = 16;

    private record Panel(int x0, int y0, int x1, int y1) {
        int width() {
            return x1 - x0 + 1;
        }

        int height() {
            return y1 - y0 + 1;
        }

        boolean middle() {
            return (-x0 == x1 || -x0 + 1 == x1) && (-y0 == y1 || -y0 + 1 == y1);
        }
    }

    public EnergyCellRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EnergyCellBlockEntity cell, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = cell.getLevel();
        if (level == null) {
            return;
        }
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            CellDisplayMode mode = cell.display(facing);
            if (mode == CellDisplayMode.NONE) {
                continue;
            }
            BlockPos ahead = cell.getBlockPos().relative(facing);
            if (!Block.shouldRenderFace(cell.getBlockState(), level, cell.getBlockPos(), facing, ahead)) {
                continue;
            }
            int light = LevelRenderer.getLightColor(level, ahead);
            if (mode == CellDisplayMode.BAR) {
                bar(cell, poseStack, buffers, facing, light);
            } else {
                readout(cell, poseStack, buffers, facing, light);
            }
        }
    }

    private static void bar(EnergyCellBlockEntity cell, PoseStack poseStack, MultiBufferSource buffers,
                            Direction facing, int light) {
        int level = cell.barLevel(facing);
        if (level <= 0) {
            return;
        }
        TextureAtlasSprite sprite = sprite(cell.tier().blockName() + "_side_" + level);
        float fill = Math.min(1.0F, level / (float) EnergyCellBlock.STEPS);
        quad(poseStack.last(), buffers.getBuffer(RenderType.cutout()), facing, sprite, light, fill);
    }

    private static void readout(EnergyCellBlockEntity cell, PoseStack poseStack, MultiBufferSource buffers,
                                Direction facing, int light) {
        Panel panel = panel(cell, facing);
        if (!panel.middle()) {
            return;
        }
        int width = panel.width();
        int height = panel.height();
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(facing.get2DDataValue() * 90.0F));
        poseStack.translate(width % 2 == 0 ? 0.5F : 0.0F, height % 2 == 0 ? -0.5F : 0.0F, 0.502F);
        poseStack.scale(1 / 16.0F, -1 / 16.0F, 1 / 16.0F);

        float byWidth = width / (float) font.width("OutputOutput");
        float byHeight = height / (float) (font.lineHeight * 5);
        float scale = Math.min(byWidth, byHeight) * 16.0F;
        poseStack.scale(scale, scale, scale);

        if (height > 1.4F * width) {
            line(font, poseStack, buffers, name("input"), -font.lineHeight * 3.5F, STEADY, light);
            line(font, poseStack, buffers, Numbers.compact(cell.inRate()), -font.lineHeight * 1.5F, GAINING, light);
            line(font, poseStack, buffers, name("output"), font.lineHeight * 0.5F, STEADY, light);
            line(font, poseStack, buffers, Numbers.compact(cell.outRate()), font.lineHeight * 2.5F, LOSING, light);
        } else {
            long net = cell.inRate() - cell.outRate();
            String value = (net > 0 ? "+" : net < 0 ? "-" : "") + Numbers.compact(Math.abs(net));
            line(font, poseStack, buffers, "I/O", -font.lineHeight * 1.5F, STEADY, light);
            line(font, poseStack, buffers, value, font.lineHeight * 0.5F,
                    net > 0 ? GAINING : net < 0 ? LOSING : STEADY, light);
        }
        poseStack.popPose();
    }

    private static String name(String mode) {
        return Component.translatable("ultimatetransport.cell_side." + mode).getString();
    }

    /**
     * The block of faces this one belongs to, as an offset rectangle around it. Only a rectangle with
     * nothing hanging off any of its four edges counts, so an L of faces is left to each block on its
     * own rather than drawn over with a panel that does not fit it.
     */
    private static Panel panel(EnergyCellBlockEntity cell, Direction facing) {
        int x0 = 0;
        int y0 = 0;
        int x1 = 0;
        int y1 = 0;
        for (int step = 1; step < REACH; step++) {
            if (!ioAt(cell, facing, step, 0)) {
                break;
            }
            x1 = step;
        }
        for (int step = 1; step < REACH - x1; step++) {
            if (!ioAt(cell, facing, -step, 0)) {
                break;
            }
            x0 = -step;
        }
        downward:
        for (int step = 1; step < REACH; step++) {
            int found = 0;
            for (int x = x0; x <= x1; x++) {
                if (ioAt(cell, facing, x, step)) {
                    found++;
                } else {
                    if (found > 0) {
                        return new Panel(0, 0, 0, 0);
                    }
                    break downward;
                }
            }
            y1 = step;
        }
        upward:
        for (int step = 1; step < REACH - y1; step++) {
            int found = 0;
            for (int x = x0; x <= x1; x++) {
                if (ioAt(cell, facing, x, -step)) {
                    found++;
                } else {
                    if (found > 0) {
                        return new Panel(0, 0, 0, 0);
                    }
                    break upward;
                }
            }
            y0 = -step;
        }
        return settle(cell, facing, x0, y0, x1, y1);
    }

    private static Panel settle(EnergyCellBlockEntity cell, Direction facing, int x0, int y0, int x1, int y1) {
        for (int x = x0; x <= x1; x++) {
            if (ioAt(cell, facing, x, y0 - 1) || ioAt(cell, facing, x, y1 + 1)) {
                return new Panel(0, 0, 0, 0);
            }
        }
        for (int y = y0; y <= y1; y++) {
            if (ioAt(cell, facing, x0 - 1, y) || ioAt(cell, facing, x1 + 1, y)) {
                return new Panel(0, 0, 0, 0);
            }
        }
        return new Panel(x0, y0, x1, y1);
    }

    private static boolean ioAt(EnergyCellBlockEntity cell, Direction facing, int x, int y) {
        Level level = cell.getLevel();
        if (level == null) {
            return false;
        }
        BlockPos at = cell.getBlockPos().below(y).relative(facing.getClockWise(), -x);
        return level.getBlockEntity(at) instanceof EnergyCellBlockEntity other
                && other.tier() == cell.tier()
                && other.display(facing) == CellDisplayMode.IO;
    }

    private static void line(Font font, PoseStack poseStack, MultiBufferSource buffers, String text,
                             float y, int colour, int light) {
        poseStack.pushPose();
        poseStack.translate(-font.width(text) / 2.0F, y, 0.0F);
        font.drawInBatch(text, 0.0F, 0.0F, colour, false, poseStack.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, light, false);
        poseStack.popPose();
    }

    private static TextureAtlasSprite sprite(String name) {
        return Minecraft.getInstance()
                .getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                .getSprite(UltimateTransport.id("block/" + name));
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Direction facing,
                             TextureAtlasSprite sprite, int light, float fill) {
        float low = -OUT;
        float high = 1.0F + OUT;
        float top = fill;
        float[][] corners = switch (facing) {
            case NORTH -> new float[][] {{1, 0, low}, {0, 0, low}, {0, top, low}, {1, top, low}};
            case SOUTH -> new float[][] {{0, 0, high}, {1, 0, high}, {1, top, high}, {0, top, high}};
            case WEST -> new float[][] {{low, 0, 0}, {low, 0, 1}, {low, top, 1}, {low, top, 0}};
            default -> new float[][] {{high, 0, 1}, {high, 0, 0}, {high, top, 0}, {high, top, 1}};
        };
        float edge = sprite.getV1() + (sprite.getV0() - sprite.getV1()) * fill;
        float[] u = {sprite.getU0(), sprite.getU1(), sprite.getU1(), sprite.getU0()};
        float[] v = {sprite.getV1(), sprite.getV1(), edge, edge};
        for (int index = 0; index < 4; index++) {
            buffer.addVertex(pose, corners[index][0], corners[index][1], corners[index][2])
                    .setColor(WHITE)
                    .setUv(u[index], v[index])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, facing.getStepX(), facing.getStepY(), facing.getStepZ());
        }
    }
}
