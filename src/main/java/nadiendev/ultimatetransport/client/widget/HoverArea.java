package nadiendev.ultimatetransport.client.widget;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/** A rectangle in screen space that shows a tooltip while the pointer is over it. */
public record HoverArea(int x, int y, int width, int height, Supplier<List<Component>> tooltip) {

    public boolean isHovered(int left, int top, int mouseX, int mouseY) {
        return mouseX >= left + x && mouseX < left + x + width
                && mouseY >= top + y && mouseY < top + y + height;
    }
}
