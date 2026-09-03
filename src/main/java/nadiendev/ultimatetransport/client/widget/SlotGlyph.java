package nadiendev.ultimatetransport.client.widget;

import net.minecraft.client.gui.GuiGraphics;

/**
 * The ghost marks that say what a slot is for while it stands empty: a cell filling a battery, a
 * battery emptying into the cell, and a flame for the fuel a generator burns.
 */
public final class SlotGlyph {

    private static final int CHARGE = 0x904CE07A;
    private static final int DISCHARGE = 0x90E0A82D;
    private static final int FUEL = 0x90E0752D;

    private SlotGlyph() {
    }

    public static void charge(GuiGraphics graphics, int x, int y) {
        battery(graphics, x + 1, y + 2, CHARGE);
        arrow(graphics, x + 10, y + 4, true, CHARGE);
    }

    public static void discharge(GuiGraphics graphics, int x, int y) {
        battery(graphics, x + 1, y + 2, DISCHARGE);
        arrow(graphics, x + 10, y + 4, false, DISCHARGE);
    }

    public static void fuel(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 7, y + 3, x + 9, y + 5, FUEL);
        graphics.fill(x + 6, y + 5, x + 10, y + 7, FUEL);
        graphics.fill(x + 5, y + 7, x + 11, y + 12, FUEL);
        graphics.fill(x + 6, y + 12, x + 10, y + 13, FUEL);
    }

    /** A battery seen side on: a cap, a hollow shell and the charge sitting in the bottom of it. */
    private static void battery(GuiGraphics graphics, int x, int y, int colour) {
        graphics.fill(x + 2, y, x + 5, y + 1, colour);
        graphics.fill(x, y + 1, x + 7, y + 2, colour);
        graphics.fill(x, y + 11, x + 7, y + 12, colour);
        graphics.fill(x, y + 1, x + 1, y + 12, colour);
        graphics.fill(x + 6, y + 1, x + 7, y + 12, colour);
        graphics.fill(x + 2, y + 7, x + 5, y + 11, colour);
    }

    private static void arrow(GuiGraphics graphics, int x, int y, boolean up, int colour) {
        if (up) {
            for (int step = 0; step < 3; step++) {
                graphics.fill(x + 2 - step, y + step, x + 3 + step, y + step + 1, colour);
            }
            graphics.fill(x + 1, y + 3, x + 4, y + 9, colour);
        } else {
            graphics.fill(x + 1, y, x + 4, y + 6, colour);
            for (int step = 0; step < 3; step++) {
                graphics.fill(x + step, y + 6 + step, x + 5 - step, y + 7 + step, colour);
            }
        }
    }
}
