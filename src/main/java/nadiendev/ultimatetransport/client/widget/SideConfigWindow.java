package nadiendev.ultimatetransport.client.widget;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.menu.SideConfigTarget;
import nadiendev.ultimatetransport.network.SideConfigPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * The face net as a panel that lives inside a machine screen, laid out on the same grid the rest of
 * the mod uses: the top face above, the four walls across, the floor below.
 */
public class SideConfigWindow {

    public static final int WIDTH = 156;
    public static final int HEIGHT = 135;

    private static final int TILE = 22;
    private static final int MID_X = 68;
    private static final int LEFT_X = 45;
    private static final int RIGHT_X = 91;
    private static final int TOP_Y = 46;
    private static final int MIDDLE_Y = 69;
    private static final int LOW_Y = 92;

    private static final int TAB_X = -26;
    private static final int TAB_SPACING = 28;
    private static final int TAB_TOP = 6;

    private static final int EJECT_X = 136;
    private static final int EJECT_Y = 6;
    private static final int CLOSE_X = 136;
    private static final int CLOSE_Y = 26;
    private static final int CLEAR_X = 136;
    private static final int CLEAR_Y = 95;
    private static final int SMALL = 14;

    public enum Face {
        TOP(MID_X, TOP_Y),
        LEFT(LEFT_X, MIDDLE_Y),
        FRONT(MID_X, MIDDLE_Y),
        RIGHT(RIGHT_X, MIDDLE_Y),
        BACK(LEFT_X, LOW_Y),
        BOTTOM(MID_X, LOW_Y);

        final int x;
        final int y;

        Face(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String key() {
            return "ultimatetransport.face." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final Supplier<BlockEntity> target;
    private final BlockPos pos;
    private final Font font;
    private final Runnable onClose;

    private int left;
    private int top;
    private TransferType cargo;

    public SideConfigWindow(Supplier<BlockEntity> target, BlockPos pos, Font font, Runnable onClose) {
        this.target = target;
        this.pos = pos;
        this.font = font;
        this.onClose = onClose;
    }

    public void place(int left, int top) {
        this.left = left;
        this.top = top;
        BlockEntity blockEntity = target.get();
        if (blockEntity == null) {
            return;
        }
        TransferType[] cargoes = SideConfigTarget.cargoes(blockEntity);
        if (cargo == null || !carries(cargoes, cargo)) {
            cargo = cargoes.length > 0 ? cargoes[0] : null;
        }
    }

    private static boolean carries(TransferType[] cargoes, TransferType wanted) {
        for (TransferType option : cargoes) {
            if (option == wanted) {
                return true;
            }
        }
        return false;
    }

    private TransferType[] tabs() {
        BlockEntity blockEntity = target.get();
        if (blockEntity == null) {
            return new TransferType[0];
        }
        TransferType[] cargoes = SideConfigTarget.cargoes(blockEntity);
        return cargoes.length < 2 ? new TransferType[0] : cargoes;
    }

    private Direction facing(BlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        for (DirectionProperty property : new DirectionProperty[]{
                BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.FACING}) {
            if (state.hasProperty(property)) {
                Direction value = state.getValue(property);
                return value.getAxis() == Direction.Axis.Y ? Direction.NORTH : value;
            }
        }
        return Direction.NORTH;
    }

    public Direction direction(BlockEntity blockEntity, Face face) {
        Direction facing = facing(blockEntity);
        return switch (face) {
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getCounterClockWise();
            case RIGHT -> facing.getClockWise();
        };
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, Component title, int labelColour) {
        BlockEntity blockEntity = target.get();
        if (blockEntity == null) {
            return;
        }
        panel(graphics, left, top, WIDTH, HEIGHT);
        graphics.drawString(font, title, left + 6, top + 6, labelColour, false);
        graphics.drawString(font, Component.translatable("screen.ultimatetransport.side_config.hint")
                .withStyle(ChatFormatting.DARK_GRAY), left + 6, top + 119, labelColour, false);

        List<SideConfigTarget.Option> options = SideConfigTarget.options(blockEntity, cargo);
        for (Face face : Face.values()) {
            Direction side = direction(blockEntity, face);
            int x = left + face.x;
            int y = top + face.y;
            boolean enabled = SideConfigTarget.enabled(blockEntity, side, cargo);
            int index = SideConfigTarget.index(blockEntity, side, cargo);
            int colour = enabled && index < options.size()
                    ? 0xFF000000 | options.get(index).colour()
                    : 0xFF3A3E45;
            tile(graphics, x, y, TILE, colour, enabled && inside(mouseX, mouseY, x, y, TILE, TILE));
            String letter = face.name().substring(0, 1);
            graphics.drawString(font, letter, x + (TILE - font.width(letter)) / 2, y + 7,
                    enabled ? 0xFF101014 : 0xFF6A7078, false);
        }

        boolean ejects = SideConfigTarget.ejects(blockEntity, cargo);
        boolean ejecting = ejects && SideConfigTarget.ejecting(blockEntity, cargo);
        tile(graphics, left + EJECT_X, top + EJECT_Y, SMALL,
                ejects ? (ejecting ? 0xFF4CE07A : 0xFF555A62) : 0xFF3A3E45,
                ejects && inside(mouseX, mouseY, left + EJECT_X, top + EJECT_Y, SMALL, SMALL));
        tile(graphics, left + CLOSE_X, top + CLOSE_Y, SMALL, 0xFF9AA1AE,
                inside(mouseX, mouseY, left + CLOSE_X, top + CLOSE_Y, SMALL, SMALL));
        cross(graphics, left + CLOSE_X + 4, top + CLOSE_Y + 4);
        tile(graphics, left + CLEAR_X, top + CLEAR_Y, SMALL, 0xFFE04C4C,
                inside(mouseX, mouseY, left + CLEAR_X, top + CLEAR_Y, SMALL, SMALL));

        TransferType[] tabs = tabs();
        for (int index = 0; index < tabs.length; index++) {
            TransferType option = tabs[index];
            int x = left + TAB_X;
            int y = top + TAB_TOP + index * TAB_SPACING;
            int colour = 0xFF000000 | (option == cargo ? option.tint() : shadeRgb(option.tint(), 0.45F));
            tile(graphics, x, y, 22, colour, inside(mouseX, mouseY, x, y, 26, 22));
            String letter = option.getSerializedName().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
            graphics.drawString(font, letter, x + 8, y + 7, 0xFF101014, false);
        }
    }

    @Nullable
    public List<Component> tooltip(int mouseX, int mouseY) {
        BlockEntity blockEntity = target.get();
        if (blockEntity == null) {
            return null;
        }
        TransferType[] tabs = tabs();
        for (int index = 0; index < tabs.length; index++) {
            if (inside(mouseX, mouseY, left + TAB_X, top + TAB_TOP + index * TAB_SPACING, 26, 22)) {
                return List.of(Component.translatable(tabs[index].translationKey()));
            }
        }
        if (inside(mouseX, mouseY, left + EJECT_X, top + EJECT_Y, SMALL, SMALL)) {
            boolean ejecting = SideConfigTarget.ejecting(blockEntity, cargo);
            return List.of(Component.translatable("screen.ultimatetransport.side_config.eject"),
                    Component.translatable(ejecting
                                    ? "screen.ultimatetransport.side_config.eject_on"
                                    : "screen.ultimatetransport.side_config.eject_off")
                            .withStyle(style -> style.withColor(ejecting ? 0x4CE07A : 0x9AA1AE)));
        }
        if (inside(mouseX, mouseY, left + CLOSE_X, top + CLOSE_Y, SMALL, SMALL)) {
            return List.of(Component.translatable("screen.ultimatetransport.side_config.close"));
        }
        if (inside(mouseX, mouseY, left + CLEAR_X, top + CLEAR_Y, SMALL, SMALL)) {
            return List.of(Component.translatable("screen.ultimatetransport.side_config.clear"));
        }
        Face face = pick(mouseX, mouseY);
        if (face == null) {
            return null;
        }
        Direction side = direction(blockEntity, face);
        Component name = Component.translatable(face.key()).append(" · ")
                .append(Component.translatable("ultimatetransport.direction." + side.getSerializedName()));
        if (!SideConfigTarget.enabled(blockEntity, side, cargo)) {
            return List.of(name, Component.translatable("screen.ultimatetransport.side_config.unlinked")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        List<SideConfigTarget.Option> options = SideConfigTarget.options(blockEntity, cargo);
        int index = SideConfigTarget.index(blockEntity, side, cargo);
        if (index >= options.size()) {
            return List.of(name);
        }
        SideConfigTarget.Option option = options.get(index);
        return List.of(name, Component.translatable(option.translationKey())
                .withStyle(style -> style.withColor(option.colour())));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        BlockEntity blockEntity = target.get();
        if (blockEntity == null || (button != 0 && button != 1)) {
            return false;
        }
        int x = (int) mouseX;
        int y = (int) mouseY;
        TransferType[] tabs = tabs();
        for (int index = 0; index < tabs.length; index++) {
            if (inside(x, y, left + TAB_X, top + TAB_TOP + index * TAB_SPACING, 26, 22)) {
                cargo = tabs[index];
                click();
                return true;
            }
        }
        if (inside(x, y, left + EJECT_X, top + EJECT_Y, SMALL, SMALL)) {
            return SideConfigTarget.ejects(blockEntity, cargo)
                    ? send(SideConfigPayload.TOGGLE_EJECT, true)
                    : true;
        }
        if (inside(x, y, left + CLOSE_X, top + CLOSE_Y, SMALL, SMALL)) {
            click();
            onClose.run();
            return true;
        }
        if (inside(x, y, left + CLEAR_X, top + CLEAR_Y, SMALL, SMALL)) {
            return send(SideConfigPayload.CLEAR_ALL, true);
        }
        Face face = pick(x, y);
        if (face != null) {
            return send(direction(blockEntity, face).ordinal(), button == 0);
        }
        return inside(x, y, left, top, WIDTH, HEIGHT);
    }

    private boolean send(int side, boolean forward) {
        PacketDistributor.sendToServer(new SideConfigPayload(pos, side, cargo == null ? -1 : cargo.ordinal(), forward));
        click();
        return true;
    }

    private static void click() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance
                .forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
    }

    @Nullable
    private Face pick(int mouseX, int mouseY) {
        for (Face face : Face.values()) {
            if (inside(mouseX, mouseY, left + face.x, top + face.y, TILE, TILE)) {
                return face;
            }
        }
        return null;
    }

    public boolean covers(int mouseX, int mouseY) {
        return inside(mouseX, mouseY, left + TAB_X, top, WIDTH - TAB_X, HEIGHT);
    }

    private static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF101216);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF32363C);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFF5A5F68);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF16181C);
    }

    private static void cross(GuiGraphics graphics, int x, int y) {
        for (int step = 0; step < 6; step++) {
            graphics.fill(x + step, y + step, x + step + 1, y + step + 1, 0xFF14171B);
            graphics.fill(x + 5 - step, y + step, x + 6 - step, y + step + 1, 0xFF14171B);
        }
    }

    private static void tile(GuiGraphics graphics, int x, int y, int size, int colour, boolean hovered) {
        graphics.fill(x, y, x + size, y + size, 0xFF0B0D10);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, colour);
        graphics.fill(x + 1, y + 1, x + size - 1, y + 2, shade(colour, 1.35F));
        graphics.fill(x + 1, y + size - 2, x + size - 1, y + size - 1, shade(colour, 0.65F));
        if (hovered) {
            graphics.renderOutline(x, y, size, size, 0xFFFFFFFF);
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int shade(int argb, float factor) {
        return 0xFF000000 | shadeRgb(argb, factor);
    }

    private static int shadeRgb(int rgb, float factor) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * factor));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((rgb & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }
}
