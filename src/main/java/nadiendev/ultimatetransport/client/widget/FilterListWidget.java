package nadiendev.ultimatetransport.client.widget;

import nadiendev.ultimatetransport.client.GuiTheme;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.filter.FilterEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class FilterListWidget {
    private static final ResourceLocation LIGHT_ROWS = UltimateTransport.id("textures/gui/cable_rows.png");
    private static final ResourceLocation DARK_ROWS = UltimateTransport.id("textures/gui/cable_rows_dark.png");

    private static ResourceLocation rows() {
        return GuiTheme.skin(LIGHT_ROWS, DARK_ROWS);
    }

    public static final int X = 32;
    public static final int Y = 26;
    public static final int ROW_WIDTH = 125;
    public static final int ROW_HEIGHT = 22;
    public static final int ROWS = 3;

    private static final int TRACK_X = 158 - X;
    private static final int TRACK_WIDTH = 10;
    private static final int THUMB_HEIGHT = 18;

    private static final int ROW_V = 0;
    private static final int ROW_SELECTED_V = 22;
    private static final int THUMB_U = 125;
    private static final int THUMB_DISABLED_U = 135;
    private static final int THUMB_V = 0;
    private static final int SHEET_WIDTH = 256;
    private static final int SHEET_HEIGHT = 64;

    private final Supplier<TransferType> type;
    private final Supplier<List<FilterEntry>> entries;

    private int offset;
    private int selected = -1;
    private String query = "";

    public void setQuery(String text) {
        query = text == null ? "" : text.trim().toLowerCase(java.util.Locale.ROOT);
        offset = 0;
        selected = -1;
    }

    private List<FilterEntry> visible() {
        List<FilterEntry> all = entries.get();
        if (query.isEmpty()) {
            return all;
        }
        List<FilterEntry> matched = new java.util.ArrayList<>();
        for (FilterEntry entry : all) {
            if (label(entry).toLowerCase(java.util.Locale.ROOT).contains(query)
                    || entry.displayKey().toLowerCase(java.util.Locale.ROOT).contains(query)) {
                matched.add(entry);
            }
        }
        return matched;
    }

    public FilterListWidget(Supplier<TransferType> type, Supplier<List<FilterEntry>> entries) {
        this.type = type;
        this.entries = entries;
    }

    @Nullable
    public FilterEntry selected() {
        List<FilterEntry> list = visible();
        return selected >= 0 && selected < list.size() ? list.get(selected) : null;
    }

    public void tick() {
        int size = visible().size();
        offset = Math.max(0, Math.min(offset, Math.max(0, size - ROWS)));
        if (selected >= size) {
            selected = -1;
        }
    }

    public void render(GuiGraphics graphics, Font font, int left, int top, int mouseX, int mouseY) {
        List<FilterEntry> list = visible();
        int originX = left + X;
        int originY = top + Y;

        for (int row = 0; row < ROWS; row++) {
            int index = offset + row;
            if (index >= list.size()) {
                break;
            }
            int rowY = originY + row * ROW_HEIGHT;
            graphics.blit(rows(), originX, rowY, 0,
                    index == selected ? ROW_SELECTED_V : ROW_V, ROW_WIDTH, ROW_HEIGHT,
                    SHEET_WIDTH, SHEET_HEIGHT);

            FilterEntry entry = list.get(index);
            ItemStack stack = FilterIcons.stackFor(type.get(), entry);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, originX + 3, rowY + 3);
            }

            int textX = originX + 22;
            int textWidth = ROW_WIDTH - 28;
            if (entry.invert()) {
                graphics.drawString(font, "!", textX, rowY + 7, 0xAA2222, false);
                textX += 7;
                textWidth -= 7;
            }
            graphics.drawString(font, font.plainSubstrByWidth(label(entry), textWidth),
                    textX, rowY + 7, GuiTheme.current().label(), false);

            if (entry.destination() != null) {
                graphics.drawString(font, "●", originX + ROW_WIDTH - 10, rowY + 7, 0x2E7D32, false);
            }
        }

        renderScrollbar(graphics, left, originY, list.size());
    }

    private static String label(FilterEntry entry) {
        String key = entry.displayKey();
        if (key.isEmpty()) {
            return Component.translatable("screen.ultimatetransport.filter.any").getString();
        }
        if (key.startsWith("#")) {
            return key;
        }
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id == null) {
            return key;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item != null && item != Items.AIR) {
            return item.getDescription().getString();
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        if (block != null && block != Blocks.AIR) {
            return block.getName().getString();
        }
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
        if (fluid != null && fluid != Fluids.EMPTY) {
            return new FluidStack(fluid, 1).getHoverName().getString();
        }
        return id.getPath();
    }

    private void renderScrollbar(GuiGraphics graphics, int left, int originY, int size) {
        int trackX = left + X + TRACK_X;
        boolean scrollable = size > ROWS;
        int travel = ROWS * ROW_HEIGHT - THUMB_HEIGHT;
        int thumbY = scrollable ? originY + (travel * offset) / (size - ROWS) : originY;
        graphics.blit(rows(), trackX, thumbY, scrollable ? THUMB_U : THUMB_DISABLED_U, THUMB_V,
                TRACK_WIDTH, THUMB_HEIGHT, SHEET_WIDTH, SHEET_HEIGHT);
    }

    public boolean mouseClicked(int left, int top, double mouseX, double mouseY) {
        int originX = left + X;
        int originY = top + Y;
        if (mouseX < originX || mouseX >= originX + ROW_WIDTH
                || mouseY < originY || mouseY >= originY + ROWS * ROW_HEIGHT) {
            return false;
        }
        int index = offset + (int) ((mouseY - originY) / ROW_HEIGHT);
        selected = index < visible().size() && index != selected ? index : -1;
        return true;
    }

    public boolean mouseScrolled(int left, int top, double mouseX, double mouseY, double delta) {
        int originX = left + X;
        int originY = top + Y;
        if (mouseX < originX || mouseX >= originX + TRACK_X + TRACK_WIDTH
                || mouseY < originY || mouseY >= originY + ROWS * ROW_HEIGHT) {
            return false;
        }
        int size = visible().size();
        if (size > ROWS) {
            offset = Math.max(0, Math.min(offset - (int) Math.signum(delta), size - ROWS));
        }
        return true;
    }
}
