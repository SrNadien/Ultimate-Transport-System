package nadiendev.ultimatetransport.client;

import nadiendev.ultimatetransport.item.ConfiguratorMode;
import net.minecraft.client.gui.GuiGraphics;

import java.util.EnumMap;
import java.util.Map;

/**
 * A twelve by twelve mark for each mode, written out as art so the wheel reads at a glance without a
 * sheet to load or an atlas to register.
 */
public final class ConfiguratorIcons {

    public static final int SIZE = 12;

    private static final Map<ConfiguratorMode, String[]> ART = new EnumMap<>(ConfiguratorMode.class);

    static {
        ART.put(ConfiguratorMode.CONFIGURATE_ITEM, new String[]{
                "            ",
                "  ########  ",
                " ########## ",
                " ##      ## ",
                " ##      ## ",
                " ##      ## ",
                " ##      ## ",
                " ##      ## ",
                " ########## ",
                "  ########  ",
                "            ",
                "            "});
        ART.put(ConfiguratorMode.CONFIGURATE_FLUID, new String[]{
                "     ##     ",
                "     ##     ",
                "    ####    ",
                "    ####    ",
                "   ######   ",
                "  ########  ",
                " ########## ",
                " ########## ",
                " ########## ",
                "  ########  ",
                "   ######   ",
                "            "});
        ART.put(ConfiguratorMode.CONFIGURATE_GAS, new String[]{
                "   ######   ",
                "   ##  ##   ",
                "   ##  ##   ",
                "   ##  ##   ",
                "  ########  ",
                "  ########  ",
                " ########## ",
                " ########## ",
                " ########## ",
                " ########## ",
                "  ########  ",
                "            "});
        ART.put(ConfiguratorMode.CONFIGURATE_ENERGY, new String[]{
                "       ###  ",
                "      ###   ",
                "     ###    ",
                "    ###     ",
                "   #######  ",
                "  #######   ",
                "     ###    ",
                "    ###     ",
                "   ###      ",
                "  ###       ",
                " ###        ",
                "            "});
        ART.put(ConfiguratorMode.CONFIGURATE_SOURCE, new String[]{
                "     ##     ",
                "     ##     ",
                "  #  ##  #  ",
                "   # ## #   ",
                "    #####   ",
                " ########## ",
                "    #####   ",
                "   # ## #   ",
                "  #  ##  #  ",
                "     ##     ",
                "     ##     ",
                "            "});
        ART.put(ConfiguratorMode.EMPTY, new String[]{
                " ########## ",
                " ########## ",
                "  ##    ##  ",
                "  ##    ##  ",
                "   ##  ##   ",
                "   ##  ##   ",
                "    ####    ",
                "    ####    ",
                "     ##     ",
                "     ##     ",
                "            ",
                "            "});
        ART.put(ConfiguratorMode.ROTATE, new String[]{
                "    ####    ",
                "  ###  ###  ",
                " ##      ## ",
                " ##       ##",
                "##         #",
                "##          ",
                "##          ",
                " ##       ##",
                " ##      ###",
                "  ###  #####",
                "    ####  ##",
                "           #"});
        ART.put(ConfiguratorMode.WRENCH, new String[]{
                "  ##    ##  ",
                "  ##    ##  ",
                "  ########  ",
                "   ######   ",
                "    ####    ",
                "    ####    ",
                "    ####    ",
                "    ####    ",
                "    ####    ",
                "   ######   ",
                "   ######   ",
                "    ####    "});
    }

    private ConfiguratorIcons() {
    }

    public static void draw(GuiGraphics graphics, ConfiguratorMode mode, int left, int top, int colour) {
        String[] art = ART.get(mode);
        if (art == null) {
            return;
        }
        for (int row = 0; row < art.length; row++) {
            String line = art[row];
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) != ' ') {
                    graphics.fill(left + column, top + row, left + column + 1, top + row + 1, colour);
                }
            }
        }
    }
}
