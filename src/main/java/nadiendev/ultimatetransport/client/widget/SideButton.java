package nadiendev.ultimatetransport.client.widget;

import nadiendev.ultimatetransport.cell.CellSideMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * One face in the side configuration net: a coloured square you click to step the face through
 * disabled, input, output and both. The colour is the whole message, so the letter is only there to
 * say which face it is.
 */
public class SideButton extends Button {

    public static final int SIZE = 20;

    private static final int BORDER = 0xFF1A1A1E;
    private static final int HIGHLIGHT = 0xFFFFFFFF;

    private final Direction side;
    private final Supplier<CellSideMode> mode;

    public SideButton(int x, int y, Direction side, Supplier<CellSideMode> mode, OnPress onPress) {
        super(x, y, SIZE, SIZE, Component.empty(), onPress, DEFAULT_NARRATION);
        this.side = side;
        this.mode = mode;
    }

    public Direction side() {
        return side;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + SIZE, y + SIZE, BORDER);
        graphics.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, 0xFF000000 | mode.get().colour());
        if (isHovered()) {
            graphics.renderOutline(x, y, SIZE, SIZE, HIGHLIGHT);
        }

        var font = net.minecraft.client.Minecraft.getInstance().font;
        String letter = letter(side);
        graphics.drawString(font, letter, x + (SIZE - font.width(letter)) / 2, y + 6, 0xFF202020, false);
    }

    /** Bottom is the odd one out: B is already taken by Back in most people's heads, so it is D. */
    private static String letter(Direction side) {
        return switch (side) {
            case UP -> "U";
            case DOWN -> "D";
            case NORTH -> "N";
            case SOUTH -> "S";
            case WEST -> "W";
            case EAST -> "E";
        };
    }
}
