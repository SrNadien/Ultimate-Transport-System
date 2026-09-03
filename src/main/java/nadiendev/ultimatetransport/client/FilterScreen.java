package nadiendev.ultimatetransport.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.client.widget.CycleIconButton;
import nadiendev.ultimatetransport.client.widget.FilterIcons;
import nadiendev.ultimatetransport.client.widget.HoverArea;
import nadiendev.ultimatetransport.filter.DirectionalPosition;
import nadiendev.ultimatetransport.filter.FilterEntry;
import nadiendev.ultimatetransport.item.DestinationToolItem;
import nadiendev.ultimatetransport.menu.FilterMenu;
import nadiendev.ultimatetransport.network.CableActionPayload;
import nadiendev.ultimatetransport.network.EditFilterPayload;
import nadiendev.ultimatetransport.registry.UTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
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

/** The editor for one rule: what it matches, how exactly, and where it sends what it matches. */
public class FilterScreen extends AbstractContainerScreen<FilterMenu> {

    private static final ResourceLocation LIGHT_SKIN = UltimateTransport.id("textures/gui/filter.png");
    private static final ResourceLocation DARK_SKIN = UltimateTransport.id("textures/gui/filter_dark.png");

    public static ResourceLocation background() {
        return GuiTheme.skin(LIGHT_SKIN, DARK_SKIN);
    }

    private final List<HoverArea> hoverAreas = new ArrayList<>();
    private final FilterEntry entry;

    private EditBox keyBox;
    private EditBox nbtBox;
    private CycleIconButton exactButton;
    private CycleIconButton invertButton;
    private Button submitButton;

    public FilterScreen(FilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = FilterMenu.WIDTH;
        this.imageHeight = FilterMenu.HEIGHT;
        this.inventoryLabelY = imageHeight - 96 + 3;
        this.entry = menu.entry();
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();

        keyBox = new EditBox(font, leftPos + 30, topPos + 18, 138, 16, Component.empty());
        keyBox.setMaxLength(256);
        keyBox.setValue(entry.displayKey());
        keyBox.setResponder(this::onKeyChanged);
        addRenderableWidget(keyBox);

        nbtBox = new EditBox(font, leftPos + 8, topPos + 50, 160, 16, Component.empty());
        nbtBox.setMaxLength(1024);
        nbtBox.setValue(entry.nbt() == null ? "" : entry.nbt().toString());
        nbtBox.setResponder(this::onNbtChanged);
        addRenderableWidget(nbtBox);

        exactButton = new CycleIconButton(background(), leftPos + 125, topPos + 81,
                List.of(new CycleIconButton.Icon(176, 16), new CycleIconButton.Icon(192, 16)),
                () -> entry.exactNbt() ? 1 : 0,
                button -> entry.setExactNbt(!entry.exactNbt()));
        addRenderableWidget(exactButton);

        invertButton = new CycleIconButton(background(), leftPos + 149, topPos + 81,
                List.of(new CycleIconButton.Icon(176, 32), new CycleIconButton.Icon(192, 32)),
                () -> entry.invert() ? 1 : 0,
                button -> entry.setInvert(!entry.invert()));
        addRenderableWidget(invertButton);

        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatetransport.filter.cancel"),
                        button -> PacketDistributor.sendToServer(new CableActionPayload(
                                menu.cable().getBlockPos(), menu.side().ordinal(),
                                CableActionPayload.OPEN_CABLE, 0)))
                .bounds(leftPos + 25, topPos + 105, 60, 20).build());

        submitButton = Button.builder(Component.translatable("screen.ultimatetransport.filter.submit"),
                        button -> PacketDistributor.sendToServer(new EditFilterPayload(
                                menu.cable().getBlockPos(), menu.side().ordinal(), entry, true)))
                .bounds(leftPos + 91, topPos + 105, 60, 20).build();
        addRenderableWidget(submitButton);

        hoverAreas.add(new HoverArea(8, 18, 16, 16, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.filter.pick"))));
        hoverAreas.add(new HoverArea(29, 17, 140, 18, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.filter.item_tag"))));
        hoverAreas.add(new HoverArea(7, 49, 162, 18, () -> List.of(
                Component.translatable("tooltip.ultimatetransport.filter.nbt"))));
        hoverAreas.add(new HoverArea(126, 82, 20, 20, () -> List.of(
                Component.translatable(entry.exactNbt()
                        ? "tooltip.ultimatetransport.filter.nbt.exact"
                        : "tooltip.ultimatetransport.filter.nbt.partial"))));
        hoverAreas.add(new HoverArea(150, 82, 20, 20, () -> List.of(
                Component.translatable(entry.invert()
                        ? "tooltip.ultimatetransport.filter.inverted"
                        : "tooltip.ultimatetransport.filter.not_inverted"))));
        hoverAreas.add(new HoverArea(8, 83, 16, 16, () -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("tooltip.ultimatetransport.filter.destination"));
            if (entry.destination() != null) {
                tooltip.add(Component.translatable("tooltip.ultimatetransport.filter.destination.clear")
                        .withStyle(ChatFormatting.GRAY));
            }
            return tooltip;
        }));
    }

    // ------------------------------------------------------------------ editing

    private void onKeyChanged(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            entry.setKey(null, false);
            keyBox.setTextColor(0xE0E0E0);
            return;
        }
        boolean tag = trimmed.startsWith("#");
        ResourceLocation id = ResourceLocation.tryParse(tag ? trimmed.substring(1) : trimmed);
        boolean valid = id != null && (tag || known(id));
        entry.setKey(valid ? id : null, tag);
        keyBox.setTextColor(valid ? 0xE0E0E0 : 0xFF5555);
    }

    private boolean known(ResourceLocation id) {
        return BuiltInRegistries.ITEM.containsKey(id) || BuiltInRegistries.FLUID.containsKey(id);
    }

    private void onNbtChanged(String text) {
        if (text.trim().isEmpty()) {
            entry.setNbt(null);
            entry.setExactNbt(false);
            nbtBox.setTextColor(0xE0E0E0);
            return;
        }
        try {
            entry.setNbt(TagParser.parseTag(text));
            nbtBox.setTextColor(0xE0E0E0);
        } catch (CommandSyntaxException exception) {
            entry.setNbt(null);
            nbtBox.setTextColor(0xFF5555);
        }
    }

    /** Where JEI may drop an ingredient to fill this rule. */
    public net.minecraft.client.renderer.Rect2i itemSlotArea() {
        return new net.minecraft.client.renderer.Rect2i(leftPos + 7, topPos + 17, 18, 18);
    }

    public void fillFromGhost(ItemStack stack) {
        fillFrom(stack);
    }

    private void fillFrom(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        if (menu.cable().type() == nadiendev.ultimatetransport.api.TransferType.FLUID && !fluid.isEmpty()) {
            keyBox.setValue(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            nbtBox.setValue("");
            return;
        }
        keyBox.setValue(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        var nbt = FilterEntry.toNbt(stack.getComponentsPatch());
        nbtBox.setValue(nbt == null ? "" : nbt.toString());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        exactButton.active = entry.nbt() != null;
        submitButton.active = !entry.isBlank();
    }

    // ------------------------------------------------------------------ rendering

    @Override
    protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(background(), leftPos, topPos, 0, 0, imageWidth, imageHeight);

        ItemStack icon = FilterIcons.stackFor(menu.cable().type(), entry);
        if (!icon.isEmpty()) {
            graphics.renderItem(icon, leftPos + 8, topPos + 18);
        }

        DirectionalPosition destination = entry.destination();
        graphics.renderItem(new ItemStack(UTItems.DESTINATION_TOOL.get()), leftPos + 8, topPos + 83);
        if (destination == null) {
            // An empty slot only hints at what goes in it, so the tool is dimmed well down rather than
            // just greyed: at a glance it should read as part of the panel, not as an item sitting there.
            graphics.fill(leftPos + 8, topPos + 83, leftPos + 24, topPos + 99, GuiTheme.current().overlay());
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 31, topPos + 89, 0D);
        graphics.pose().scale(0.5F, 0.5F, 1F);
        graphics.drawString(font, destination == null
                        ? Component.translatable("screen.ultimatetransport.filter.destination.any")
                        : DestinationToolItem.describe(destination),
                0, 0, GuiTheme.current().label(), false);
        graphics.pose().popPose();
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
        graphics.drawString(font, Component.translatable("screen.ultimatetransport.filter.item_tag"),
                8, 7, GuiTheme.current().label(), false);
        graphics.drawString(font, Component.translatable("screen.ultimatetransport.filter.nbt"),
                8, 39, GuiTheme.current().label(), false);
        graphics.drawString(font, Component.translatable("screen.ultimatetransport.filter.destination"),
                8, 71, GuiTheme.current().label(), false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, GuiTheme.current().label(), false);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoverAreas.get(0).isHovered(leftPos, topPos, (int) mouseX, (int) mouseY)) {
            if (hasShiftDown()) {
                keyBox.setValue("");
                nbtBox.setValue("");
            } else {
                fillFrom(menu.getCarried());
            }
            return true;
        }
        if (hoverAreas.get(5).isHovered(leftPos, topPos, (int) mouseX, (int) mouseY)) {
            ItemStack carried = menu.getCarried();
            entry.setDestination(carried.getItem() instanceof DestinationToolItem
                    ? DestinationToolItem.destination(carried)
                    : null);
            return true;
        }
        if (hasShiftDown()) {
            Slot slot = getSlotUnderMouse();
            if (slot != null && slot.hasItem()) {
                fillFrom(slot.getItem());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            PacketDistributor.sendToServer(new CableActionPayload(
                    menu.cable().getBlockPos(), menu.side().ordinal(), CableActionPayload.OPEN_CABLE, 0));
            return true;
        }
        if (keyBox.isFocused() && (keyBox.keyPressed(key, scanCode, modifiers) || keyBox.canConsumeInput())) {
            return true;
        }
        if (nbtBox.isFocused() && (nbtBox.keyPressed(key, scanCode, modifiers) || nbtBox.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
