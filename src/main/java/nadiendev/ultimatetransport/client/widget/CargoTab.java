package nadiendev.ultimatetransport.client.widget;

import nadiendev.ultimatetransport.api.TransferType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

public class CargoTab extends Button {

    public static final int WIDTH = 26;
    public static final int HEIGHT = 22;

    private static final int BORDER = 0xFF0B0D10;

    private final TransferType cargo;
    private final BooleanSupplier selected;

    public CargoTab(int x, int y, TransferType cargo, BooleanSupplier selected, OnPress onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION);
        this.cargo = cargo;
        this.selected = selected;
    }

    public TransferType cargo() {
        return cargo;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        boolean active = selected.getAsBoolean();
        int face = 0xFF000000 | (active ? cargo.tint() : shade(cargo.tint(), 0.45F));

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BORDER);
        graphics.fill(x + 1, y + 1, x + WIDTH - (active ? 0 : 1), y + HEIGHT - 1, face);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + 2, 0xFF000000 | shade(cargo.tint(), active ? 1.4F : 0.7F));
        if (isHovered()) {
            graphics.renderOutline(x, y, WIDTH, HEIGHT, 0xFFFFFFFF);
        }

        var font = net.minecraft.client.Minecraft.getInstance().font;
        String letter = cargo.getSerializedName().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        graphics.drawString(font, letter, x + (WIDTH - font.width(letter)) / 2 - 1, y + 7, 0xFF101014, false);
    }

    @Override
    public void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private static int shade(int rgb, float factor) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * factor));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((rgb & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }
}
