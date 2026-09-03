package nadiendev.ultimatetransport.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

/** A tab clipped to the left edge of a machine screen, the way every side panel in the mod docks. */
public class GuiSideTab extends Button {

    public static final int WIDTH = 26;
    public static final int HEIGHT = 22;

    private final int colour;
    private final BooleanSupplier open;
    private final Component label;

    public GuiSideTab(int x, int y, int colour, Component label, BooleanSupplier open, OnPress onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION);
        this.colour = colour;
        this.open = open;
        this.label = label;
    }

    public Component label() {
        return label;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        boolean active = open.getAsBoolean();
        int face = 0xFF000000 | (active ? colour : shade(colour, 0.5F));

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF0B0D10);
        graphics.fill(x + 1, y + 1, x + WIDTH - (active ? 0 : 1), y + HEIGHT - 1, face);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + 2, 0xFF000000 | shade(colour, active ? 1.4F : 0.8F));
        if (isHovered()) {
            graphics.renderOutline(x, y, WIDTH, HEIGHT, 0xFFFFFFFF);
        }
        icon(graphics, x + 8, y + 6);
    }

    /** The unfolded cube, small enough to read at a glance on a 26 by 22 tab. */
    private void icon(GuiGraphics graphics, int x, int y) {
        int ink = 0xFF101014;
        graphics.fill(x + 4, y, x + 8, y + 3, ink);
        graphics.fill(x, y + 4, x + 12, y + 7, ink);
        graphics.fill(x + 4, y + 8, x + 8, y + 11, ink);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private static int shade(int rgb, float factor) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * factor));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((rgb & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }
}
