package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.SideConfig;
import nadiendev.ultimatetransport.client.widget.CargoTab;
import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.client.widget.CycleIconButton;
import nadiendev.ultimatetransport.client.widget.FilterListWidget;
import nadiendev.ultimatetransport.client.widget.HoverArea;
import nadiendev.ultimatetransport.filter.FilterEntry;
import nadiendev.ultimatetransport.menu.CableMenu;
import nadiendev.ultimatetransport.network.CableActionPayload;
import nadiendev.ultimatetransport.network.EditFilterPayload;
import nadiendev.ultimatetransport.network.RemoveFilterPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CableScreen extends AbstractContainerScreen<CableMenu> {
    private static final ResourceLocation LIGHT_SKIN = UltimateTransport.id("textures/gui/cable.png");
    private static final ResourceLocation DARK_SKIN = UltimateTransport.id("textures/gui/cable_dark.png");

    public static ResourceLocation background() {
        return GuiTheme.skin(LIGHT_SKIN, DARK_SKIN);
    }

    private final List<HoverArea> hoverAreas = new ArrayList<>();

    private CycleIconButton redstoneButton;
    private CycleIconButton distributionButton;
    private CycleIconButton filterModeButton;
    private Button addButton;
    private Button editButton;
    private Button removeButton;

    private FilterListWidget filterList;
    private EditBox search;
    private CycleIconButton themeButton;
    private CycleIconButton retrieveButton;

    public CableScreen(CableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = CableMenu.WIDTH;
        this.imageHeight = CableMenu.HEIGHT;
        this.inventoryLabelY = imageHeight - 96 + 3;
    }

    private SideConfig config() {
        return menu.cable().config(menu.side(), menu.cargo());
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();

        filterList = new FilterListWidget(menu::cargo, () -> config().filter().entries());

        TransferType[] carried = menu.cable().type().carried();
        if (carried.length > 1) {
            for (int index = 0; index < carried.length; index++) {
                TransferType cargo = carried[index];
                int slot = index;
                CargoTab tab = new CargoTab(leftPos - 26, topPos + 7 + index * 24, cargo,
                        () -> menu.cargo() == cargo,
                        button -> send(CableActionPayload.SWITCH_CARGO, slot));
                addRenderableWidget(tab);
            }
        }

        redstoneButton = new CycleIconButton(background(), leftPos + 7, topPos + 7,
                List.of(new CycleIconButton.Icon(176, 16), new CycleIconButton.Icon(192, 16),
                        new CycleIconButton.Icon(208, 16), new CycleIconButton.Icon(224, 16)),
                () -> config().redstone().ordinal(),
                button -> send(CableActionPayload.CYCLE_REDSTONE));

        distributionButton = new CycleIconButton(background(), leftPos + 7, topPos + 31,
                List.of(new CycleIconButton.Icon(176, 0), new CycleIconButton.Icon(192, 0),
                        new CycleIconButton.Icon(208, 0), new CycleIconButton.Icon(224, 0)),
                () -> config().distribution().ordinal(),
                button -> send(CableActionPayload.CYCLE_DISTRIBUTION));

        filterModeButton = new CycleIconButton(background(), leftPos + 7, topPos + 55,
                List.of(new CycleIconButton.Icon(176, 32), new CycleIconButton.Icon(192, 32)),
                () -> config().filter().mode().ordinal(),
                button -> send(CableActionPayload.CYCLE_FILTER_MODE));

        search = new EditBox(font, leftPos + 34, topPos + 10, 114, 10, Component.empty());
        search.setBordered(false);
        search.setMaxLength(48);
        search.setTextColor(GuiTheme.current().field());
        search.setHint(Component.translatable("screen.ultimatetransport.filter.search")
                .withStyle(ChatFormatting.DARK_GRAY));
        search.setResponder(text -> filterList.setQuery(text));

        themeButton = new CycleIconButton(background(), leftPos + 152, topPos + 7, 16,
                List.of(new CycleIconButton.Icon(208, 32), new CycleIconButton.Icon(224, 32)),
                () -> GuiTheme.current().dark() ? 0 : 1,
                button -> {
                    GuiTheme.toggle();
                    rebuildWidgets();
                });

        retrieveButton = new CycleIconButton(background(), leftPos + 7, topPos + 79,
                List.of(new CycleIconButton.Icon(176, 48), new CycleIconButton.Icon(192, 48)),
                () -> config().retrieve() ? 1 : 0,
                button -> send(CableActionPayload.TOGGLE_RETRIEVE));

        addButton = Button.builder(Component.translatable("screen.ultimatetransport.filter.add"),
                        button -> openFilter(FilterEntry.empty()))
                .bounds(leftPos + 31, topPos + 96, 40, 20).build();
        editButton = Button.builder(Component.translatable("screen.ultimatetransport.filter.edit"), button -> {
            FilterEntry entry = filterList.selected();
            if (entry != null) {
                openFilter(entry);
            }
        }).bounds(leftPos + 80, topPos + 96, 40, 20).build();
        removeButton = Button.builder(Component.translatable("screen.ultimatetransport.filter.remove"), button -> {
            FilterEntry entry = filterList.selected();
            if (entry != null) {
                PacketDistributor.sendToServer(new RemoveFilterPayload(
                        menu.cable().getBlockPos(), menu.side().ordinal(), entry.id()));
            }
        }).bounds(leftPos + 129, topPos + 96, 40, 20).build();

        addRenderableWidget(search);
        addRenderableWidget(themeButton);
        addRenderableWidget(retrieveButton);
        addRenderableWidget(redstoneButton);
        addRenderableWidget(distributionButton);
        addRenderableWidget(filterModeButton);
        addRenderableWidget(addButton);
        addRenderableWidget(editButton);
        addRenderableWidget(removeButton);

        hoverAreas.add(new HoverArea(7, 7, 20, 20, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.redstone_mode",
                        Component.translatable(config().redstone().translationKey())))));
        hoverAreas.add(new HoverArea(7, 31, 20, 20, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.distribution",
                        Component.translatable(config().distribution().translationKey())))));
        hoverAreas.add(new HoverArea(7, 55, 20, 20, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.filter_mode",
                        Component.translatable(config().filter().mode().translationKey())))));
        hoverAreas.add(new HoverArea(152, 7, 16, 16, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.theme",
                        Component.translatable(GuiTheme.current().dark()
                                ? "ultimatetransport.theme.dark"
                                : "ultimatetransport.theme.light")))));
        hoverAreas.add(new HoverArea(7, 79, 20, 20, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.retrieve",
                        Component.translatable(config().retrieve()
                                ? "ultimatetransport.retrieve.on"
                                : "ultimatetransport.retrieve.off")),
                Component.translatable("tooltip.ultimatetransport.retrieve.hint")
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY))));

        refresh();
    }

    private void openFilter(FilterEntry entry) {
        PacketDistributor.sendToServer(new EditFilterPayload(
                menu.cable().getBlockPos(), menu.side().ordinal(), entry, false));
    }

    private void send(int action) {
        send(action, 0);
    }

    private void send(int action, int value) {
        PacketDistributor.sendToServer(new CableActionPayload(
                menu.cable().getBlockPos(), menu.side().ordinal(), action, value));
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (search != null && search.isFocused() && key != GLFW.GLFW_KEY_ESCAPE) {
            return search.keyPressed(key, scanCode, modifiers) || search.canConsumeInput();
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        filterList.tick();
        refresh();
    }

    private void refresh() {
        SideConfig config = config();
        UpgradeTier tier = config.tier();
        boolean configurable = tier.canConfigure();

        redstoneButton.active = tier.canRedstone();
        distributionButton.active = tier.canDistribute();
        filterModeButton.active = tier.canFilter();
        retrieveButton.active = configurable;
        addButton.active = tier.canFilter();
        editButton.active = tier.canFilter() && filterList.selected() != null;
        removeButton.active = editButton.active;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(background(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
        filterList.render(graphics, font, leftPos, topPos, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        renderBackground(graphics, mouseX, mouseY, partial);
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);

        for (HoverArea area : hoverAreas) {
            if (area.isHovered(leftPos, topPos, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, area.tooltip().get(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, GuiTheme.current().label(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (filterList.mouseClicked(leftPos, topPos, mouseX, mouseY)) {
            return true;
        }
        if (hasShiftDown() && addButton.active) {
            Slot slot = getSlotUnderMouse();
            if (slot != null && slot.hasItem() && quickFilter(slot.getItem())) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean quickFilter(ItemStack stack) {
        FilterEntry entry = FilterEntry.empty();
        switch (menu.cable().type()) {
            case FLUID -> {
                FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
                if (fluid.isEmpty()) {
                    return false;
                }
                entry.setKey(BuiltInRegistries.FLUID.getKey(fluid.getFluid()), false);
            }
            case GAS -> {
                return false;
            }
            default -> {
                entry.setKey(BuiltInRegistries.ITEM.getKey(stack.getItem()), false);
                entry.setNbt(FilterEntry.toNbt(stack.getComponentsPatch()));
                entry.setExactNbt(true);
            }
        }
        PacketDistributor.sendToServer(new EditFilterPayload(
                menu.cable().getBlockPos(), menu.side().ordinal(), entry, true));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (filterList.mouseScrolled(leftPos, topPos, mouseX, mouseY, deltaY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
