package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.client.widget.GuiSideTab;
import nadiendev.ultimatetransport.client.widget.HoverArea;
import nadiendev.ultimatetransport.client.widget.SideConfigWindow;
import nadiendev.ultimatetransport.client.widget.SlotGlyph;
import nadiendev.ultimatetransport.generator.GeneratorBlockEntity;
import nadiendev.ultimatetransport.generator.GeneratorType;
import nadiendev.ultimatetransport.menu.GeneratorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation LIGHT_SKIN = UltimateTransport.id("textures/gui/generator.png");
    private static final ResourceLocation DARK_SKIN = UltimateTransport.id("textures/gui/generator_dark.png");

    public static ResourceLocation background() {
        return GuiTheme.skin(LIGHT_SKIN, DARK_SKIN);
    }

    private static final int BAR_X = 9;
    private static final int BAR_Y = 18;
    private static final int BAR_WIDTH = 158;
    private static final int BAR_HEIGHT = 12;

    private static final int BURN_X = 63;
    private static final int BURN_Y = 43;
    private static final int BURN_SIZE = 12;

    private static final int FUEL_X = 44;
    private static final int CHARGE_X = 134;
    private static final int SLOT_Y = 42;

    private final List<HoverArea> hoverAreas = new ArrayList<>();
    private SideConfigWindow window;
    private boolean windowOpen;

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GeneratorMenu.WIDTH;
        this.imageHeight = GeneratorMenu.HEIGHT;
        this.inventoryLabelY = imageHeight - 96 + 3;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();

        window = new SideConfigWindow(menu::generator, menu.generator().getBlockPos(), font, () -> windowOpen = false);
        window.place(leftPos + (imageWidth - SideConfigWindow.WIDTH) / 2, topPos + 12);

        addRenderableWidget(new GuiSideTab(leftPos - GuiSideTab.WIDTH, topPos + 6,
                menu.generator().type().colour(),
                Component.translatable("screen.ultimatetransport.side_config.tab"),
                () -> windowOpen, press -> windowOpen = !windowOpen));

        hoverAreas.add(new HoverArea(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, () -> {
            GeneratorBlockEntity generator = menu.generator();
            return List.of(Component.translatable("tooltip.ultimatetransport.cell_charge",
                    Numbers.compact(generator.stored()),
                    Numbers.compact(generator.type().capacity())));
        }));
        hoverAreas.add(new HoverArea(BURN_X, BURN_Y, BURN_SIZE, BURN_SIZE, () -> {
            GeneratorBlockEntity generator = menu.generator();
            return List.of(generator.running()
                    ? Component.translatable("screen.ultimatetransport.generator_running",
                    Numbers.compact(generator.activeRate()))
                    : Component.translatable("screen.ultimatetransport.generator_idle"));
        }));
        hoverAreas.add(new HoverArea(CHARGE_X - 1, SLOT_Y - 1, 18, 18, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.cell_charge_slot"))));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(background(), leftPos, topPos, 0, 0, imageWidth, imageHeight);

        GeneratorBlockEntity generator = menu.generator();
        GeneratorType type = generator.type();

        int filled = (int) Math.round(BAR_WIDTH * Math.min(1.0, generator.fillRatio()));
        if (filled > 0) {
            graphics.fill(leftPos + BAR_X, topPos + BAR_Y,
                    leftPos + BAR_X + filled, topPos + BAR_Y + BAR_HEIGHT,
                    0xFF000000 | type.colour());
        }

        int burn = (int) Math.round(BURN_SIZE * Math.min(1.0, generator.burnRatio()));
        if (burn > 0) {
            graphics.fill(leftPos + BURN_X, topPos + BURN_Y + BURN_SIZE - burn,
                    leftPos + BURN_X + BURN_SIZE, topPos + BURN_Y + BURN_SIZE,
                    0xFF000000 | type.colour());
        }

        if (type.consumesItems() && menu.getSlot(0).getItem().isEmpty()) {
            SlotGlyph.fuel(graphics, leftPos + FUEL_X, topPos + SLOT_Y);
        }
        if (menu.getSlot(1).getItem().isEmpty()) {
            SlotGlyph.charge(graphics, leftPos + CHARGE_X, topPos + SLOT_Y);
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
        if (windowOpen && window != null
                && (window.mouseClicked(mouseX, mouseY, button) || window.covers((int) mouseX, (int) mouseY))) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int colour = GuiTheme.current().label();
        graphics.drawString(font, title, titleLabelX, titleLabelY, colour, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, colour, false);

        GeneratorBlockEntity generator = menu.generator();
        graphics.drawString(font, Component.translatable("screen.ultimatetransport.generator_output",
                        Numbers.compact(generator.running() ? generator.activeRate() : 0))
                .withStyle(ChatFormatting.DARK_GRAY), 8, 34, colour, false);
    }
}
