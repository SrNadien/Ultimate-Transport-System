package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.item.ConfiguratorItem;
import nadiendev.ultimatetransport.item.ConfiguratorMode;
import nadiendev.ultimatetransport.network.ConfiguratorModePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** The modes laid out in a ring around the crosshair; point at one and let the key go. */
public class ConfiguratorModeScreen extends Screen {

    private static final int RADIUS = 72;
    private static final int SLICE = 24;

    private final List<ConfiguratorMode> modes = new ArrayList<>();
    private ConfiguratorMode current;

    public ConfiguratorModeScreen() {
        super(Component.translatable("key.ultimatetransport.configurator_menu"));
    }

    @Override
    protected void init() {
        modes.clear();
        for (ConfiguratorMode mode : ConfiguratorMode.VALUES) {
            if (mode.available()) {
                modes.add(mode);
            }
        }
        if (minecraft != null && minecraft.player != null) {
            ItemStack stack = ConfiguratorInput.held(minecraft.player);
            current = stack.isEmpty() ? modes.get(0) : ConfiguratorItem.mode(stack);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int centreX() {
        return width / 2;
    }

    private int centreY() {
        return height / 2;
    }

    private int slotX(int index) {
        double angle = Math.TAU * index / modes.size() - Math.PI / 2.0;
        return centreX() + (int) Math.round(Math.cos(angle) * RADIUS);
    }

    private int slotY(int index) {
        double angle = Math.TAU * index / modes.size() - Math.PI / 2.0;
        return centreY() + (int) Math.round(Math.sin(angle) * RADIUS);
    }

    private int hovered(double mouseX, double mouseY) {
        for (int index = 0; index < modes.size(); index++) {
            int x = slotX(index);
            int y = slotY(index);
            if (Math.abs(mouseX - x) <= SLICE / 2.0 && Math.abs(mouseY - y) <= SLICE / 2.0) {
                return index;
            }
        }
        return -1;
    }

    /** No blur: the wheel is meant to be read against the world, not over a frosted copy of it. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        graphics.fill(0, 0, width, height, 0x66000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        int hover = hovered(mouseX, mouseY);

        for (int index = 0; index < modes.size(); index++) {
            ConfiguratorMode mode = modes.get(index);
            int x = slotX(index);
            int y = slotY(index);
            boolean lit = index == hover || (hover < 0 && mode == current);
            int colour = 0xFF000000 | (lit ? mode.colour() : shade(mode.colour(), 0.45F));

            graphics.fill(x - SLICE / 2, y - SLICE / 2, x + SLICE / 2, y + SLICE / 2, 0xFF0B0D10);
            graphics.fill(x - SLICE / 2 + 1, y - SLICE / 2 + 1, x + SLICE / 2 - 1, y + SLICE / 2 - 1, colour);
            ConfiguratorIcons.draw(graphics, mode,
                    x - ConfiguratorIcons.SIZE / 2, y - ConfiguratorIcons.SIZE / 2,
                    lit ? 0xFF101014 : 0xB0101014);
            if (lit) {
                graphics.renderOutline(x - SLICE / 2, y - SLICE / 2, SLICE, SLICE, 0xFFFFFFFF);
            }
        }

        // Only the mode under the pointer is named. Eight labels around a small ring overlap each
        // other into a smear, and the one you are about to pick is the only one worth reading.
        ConfiguratorMode shown = hover >= 0 ? modes.get(hover) : current;
        if (shown != null) {
            Component label = shown.label();
            graphics.drawString(font, label, centreX() - font.width(label) / 2, centreY() - 4, 0xFFFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int hover = hovered(mouseX, mouseY);
        if (hover >= 0) {
            choose(modes.get(hover));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (ConfiguratorInput.MODE_MENU.matches(key, scanCode)) {
            if (minecraft != null) {
                double mouseX = minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth();
                double mouseY = minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight();
                int hover = hovered(mouseX, mouseY);
                if (hover >= 0) {
                    choose(modes.get(hover));
                    return true;
                }
            }
            onClose();
            return true;
        }
        return super.keyReleased(key, scanCode, modifiers);
    }

    private void choose(ConfiguratorMode mode) {
        PacketDistributor.sendToServer(ConfiguratorModePayload.pick(mode));
        onClose();
    }

    private static int shade(int rgb, float factor) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * factor));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((rgb & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }
}
