package nadiendev.ultimatetransport.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.IntSupplier;

/**
 * A square button that shows the current value as a picture rather than a word, and steps to the next
 * value when pressed. The frame is the vanilla button; the icons come out of the screen's own sheet.
 */
public class CycleIconButton extends Button {

    public static final int SIZE = 20;

    public record Icon(int u, int v) {
    }

    private final ResourceLocation texture;
    private final List<Icon> icons;
    private final IntSupplier index;

    public CycleIconButton(ResourceLocation texture, int x, int y, List<Icon> icons, IntSupplier index, OnPress onPress) {
        this(texture, x, y, SIZE, icons, index, onPress);
    }

    public CycleIconButton(ResourceLocation texture, int x, int y, int size, List<Icon> icons,
                           IntSupplier index, OnPress onPress) {
        super(x, y, size, size, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.icons = icons;
        this.index = index;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (icons.isEmpty()) {
            return;
        }
        Icon icon = icons.get(Math.floorMod(index.getAsInt(), icons.size()));
        int inset = Math.max(0, (getWidth() - 16) / 2);
        graphics.blit(texture, getX() + inset, getY() + inset, icon.u(), icon.v(), 16, 16);
    }
}
