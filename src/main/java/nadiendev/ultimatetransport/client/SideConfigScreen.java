package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.client.widget.SideConfigWindow;
import nadiendev.ultimatetransport.menu.SideConfigMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/** The same face panel the machines carry, opened on its own by the configurator. */
public class SideConfigScreen extends AbstractContainerScreen<SideConfigMenu> {

    private SideConfigWindow window;

    public SideConfigScreen(SideConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = SideConfigMenu.WIDTH;
        this.imageHeight = SideConfigMenu.HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        window = new SideConfigWindow(menu::target, menu.pos(), font, this::onClose);
        window.place(leftPos, topPos);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        renderBackground(graphics, mouseX, mouseY, partial);
        super.render(graphics, mouseX, mouseY, partial);
        if (window == null) {
            return;
        }
        window.render(graphics, mouseX, mouseY, title, GuiTheme.current().label());
        List<Component> tip = window.tooltip(mouseX, mouseY);
        if (tip != null) {
            graphics.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (window != null && window.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
