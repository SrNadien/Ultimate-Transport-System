package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cell.EnergyCellBlockEntity;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import nadiendev.ultimatetransport.client.widget.GuiSideTab;
import nadiendev.ultimatetransport.client.widget.HoverArea;
import nadiendev.ultimatetransport.client.widget.SideConfigWindow;
import nadiendev.ultimatetransport.client.widget.SlotGlyph;
import nadiendev.ultimatetransport.menu.CellMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/** Charge, throughput, a slot each way, and the side configuration behind its own tab. */
public class CellScreen extends AbstractContainerScreen<CellMenu> {

    private static final ResourceLocation LIGHT_SKIN = UltimateTransport.id("textures/gui/cell.png");
    private static final ResourceLocation DARK_SKIN = UltimateTransport.id("textures/gui/cell_dark.png");

    public static ResourceLocation background() {
        return GuiTheme.skin(LIGHT_SKIN, DARK_SKIN);
    }

    private static final int BAR_X = 8;
    private static final int BAR_Y = 18;
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 16;

    private static final int CHARGE_X = 133;
    private static final int DISCHARGE_X = 151;
    private static final int SLOT_Y = 18;

    private final List<HoverArea> hoverAreas = new ArrayList<>();
    private SideConfigWindow window;
    private boolean windowOpen;

    public CellScreen(CellMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = CellMenu.WIDTH;
        this.imageHeight = CellMenu.HEIGHT;
        this.inventoryLabelY = imageHeight - 96 + 3;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();

        window = new SideConfigWindow(menu::cell, menu.cell().getBlockPos(), font, () -> windowOpen = false);
        window.place(leftPos + (imageWidth - SideConfigWindow.WIDTH) / 2, topPos + 12);

        GuiSideTab tab = new GuiSideTab(leftPos - GuiSideTab.WIDTH, topPos + 6,
                menu.cell().tier().colour(),
                Component.translatable("screen.ultimatetransport.side_config.tab"),
                () -> windowOpen, press -> windowOpen = !windowOpen);
        addRenderableWidget(tab);

        hoverAreas.add(new HoverArea(CHARGE_X - 1, SLOT_Y - 1, 18, 18, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.cell_charge_slot"))));
        hoverAreas.add(new HoverArea(DISCHARGE_X - 1, SLOT_Y - 1, 18, 18, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.cell_discharge_slot"))));
        hoverAreas.add(new HoverArea(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, () -> {
            EnergyCellBlockEntity cell = menu.cell();
            return List.of(Component.translatable("tooltip.ultimatetransport.cell_bank", cell.bankSize()));
        }));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(background(), leftPos, topPos, 0, 0, imageWidth, imageHeight);

        EnergyCellBlockEntity cell = menu.cell();
        int filled = (int) Math.round(BAR_WIDTH * Math.min(1.0, cell.fillRatio()));
        if (filled > 0) {
            graphics.fill(leftPos + BAR_X, topPos + BAR_Y,
                    leftPos + BAR_X + filled, topPos + BAR_Y + BAR_HEIGHT,
                    0xFF000000 | cell.tier().colour());
        }

        if (menu.getSlot(0).getItem().isEmpty()) {
            SlotGlyph.charge(graphics, leftPos + CHARGE_X, topPos + SLOT_Y);
        }
        if (menu.getSlot(1).getItem().isEmpty()) {
            SlotGlyph.discharge(graphics, leftPos + DISCHARGE_X, topPos + SLOT_Y);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        renderBackground(graphics, mouseX, mouseY, partial);
        super.render(graphics, mouseX, mouseY, partial);

        if (windowOpen && window != null) {
            window.render(graphics, mouseX, mouseY,
                    Component.translatable("screen.ultimatetransport.side_config.tab"),
                    GuiTheme.current().label());
            List<Component> tip = window.tooltip(mouseX, mouseY);
            if (tip != null) {
                graphics.renderComponentTooltip(font, tip, mouseX, mouseY);
            }
            return;
        }

        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            return;
        }
        for (HoverArea area : hoverAreas) {
            if (area.isHovered(leftPos, topPos, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, area.tooltip().get(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (windowOpen && window != null && window.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (windowOpen && window != null && window.covers((int) mouseX, (int) mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        EnergyCellBlockEntity cell = menu.cell();
        EnergyCellTier tier = cell.tier();
        graphics.drawString(font, title, titleLabelX, titleLabelY, GuiTheme.current().label(), false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, GuiTheme.current().label(), false);

        Component charge = tier.creative()
                ? Component.translatable("screen.ultimatetransport.cell_endless")
                : Component.translatable("tooltip.ultimatetransport.cell_charge",
                Numbers.compact(cell.stored()),
                tier.bottomless()
                        ? Component.translatable("tooltip.ultimatetransport.unlimited").getString()
                        : Numbers.compact(cell.capacity()));
        graphics.drawString(font, charge, 8, 40, GuiTheme.current().label(), false);
        graphics.drawString(font, Component.translatable("tooltip.ultimatetransport.cell_transfer",
                        Numbers.compact(cell.transfer())).withStyle(ChatFormatting.DARK_GRAY),
                8, 52, GuiTheme.current().label(), false);
    }
}
