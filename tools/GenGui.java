import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The two cable screens, laid out to the same grid Pipez uses for its pipe screens: a 176x196 face
 * screen with three mode buttons, a rule list and an upgrade slot, and a 176x222 rule editor with two
 * text fields and a destination slot. The mode icons follow the same visual language, so a player who
 * knows one reads the other without relearning it.
 */
public class GenGui {

    static final int CLEAR = 0x00000000;

    // The panel is drawn twice, once per theme. Everything below reads these six, so a theme is just
    // a different set of six numbers rather than a second copy of the drawing code.
    static int OUTLINE;
    static int LIGHT;
    static int FACE;
    static int SHADOW;
    static int SLOT;
    static int SLOT_DARK;
    static int SUNKEN;

    static void theme(boolean dark) {
        if (dark) {
            OUTLINE = 0xFF000000;
            LIGHT = 0xFF5A5F68;
            FACE = 0xFF32363C;
            SHADOW = 0xFF16181C;
            SLOT = 0xFF1E2126;
            SLOT_DARK = 0xFF101216;
            SUNKEN = 0xFF24272C;
        } else {
            OUTLINE = 0xFF000000;
            LIGHT = 0xFFFFFFFF;
            FACE = 0xFFC6C6C6;
            SHADOW = 0xFF555555;
            SLOT = 0xFF8B8B8B;
            SLOT_DARK = 0xFF373737;
            SUNKEN = 0xFFAEAEAE;
        }
    }

    /** Scales a packed colour toward white, clamping at 255, so a theme only names its base tones. */
    static int shade(int argb, float factor) {
        int a = argb >>> 24;
        int r = Math.min(255, Math.round(((argb >> 16) & 0xFF) * factor));
        int g = Math.min(255, Math.round(((argb >> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((argb & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args[0], "src/main/resources/assets/ultimatetransport/textures/gui");
        Files.createDirectories(dir);
        for (boolean dark : new boolean[]{false, true}) {
            theme(dark);
            String suffix = dark ? "_dark" : "";
            ImageIO.write(cable(), "PNG", new File(dir.resolve("cable" + suffix + ".png").toString()));
            ImageIO.write(cableRows(), "PNG", new File(dir.resolve("cable_rows" + suffix + ".png").toString()));
            ImageIO.write(filter(), "PNG", new File(dir.resolve("filter" + suffix + ".png").toString()));
            ImageIO.write(cell(), "PNG", new File(dir.resolve("cell" + suffix + ".png").toString()));
            ImageIO.write(generator(), "PNG", new File(dir.resolve("generator" + suffix + ".png").toString()));
            ImageIO.write(sideConfig(), "PNG", new File(dir.resolve("side_config" + suffix + ".png").toString()));
        }
        System.out.println("gui written");
    }

    // ------------------------------------------------------------------ the face screen

    static BufferedImage cable() {
        BufferedImage image = sheet();
        panel(image, 0, 0, 176, 216);

        sunken(image, 31, 7, 138, 16, 0xFF101014);
        sunken(image, 31, 25, 138, 68, SUNKEN);
        vline(image, 157, 26, 66, SLOT_DARK);

        slot(image, 8, 101);
        inventory(image, 7, 130, 188);

        mask(image, 176, 0, NEAREST);
        mask(image, 192, 0, FURTHEST);
        mask(image, 208, 0, ROUND_ROBIN);
        mask(image, 224, 0, RANDOM);
        mask(image, 176, 16, REDSTONE_IGNORED);
        mask(image, 192, 16, REDSTONE_OFF_WHEN_POWERED);
        mask(image, 208, 16, REDSTONE_ON_WHEN_POWERED);
        mask(image, 224, 16, REDSTONE_ALWAYS_OFF);
        draw(image, 176, 32, SHEET, PALE);
        draw(image, 192, 32, SHEET, DARK);
        mask(image, 208, 32, THEME_DARK);
        mask(image, 224, 32, THEME_LIGHT);
        mask(image, 176, 48, RETRIEVE_OFF);
        mask(image, 192, 48, RETRIEVE_ON);
        return image;
    }

    /** Row backgrounds and scrollbar thumbs, on their own sheet so the panel can use its full height. */
    static BufferedImage cableRows() {
        BufferedImage image = new BufferedImage(256, 64, BufferedImage.TYPE_INT_ARGB);
        strip(image, 0, 0, 125, 22, SHADOW, FACE);
        strip(image, 0, 22, 125, 22, OUTLINE, shade(FACE, 1.35f));
        thumb(image, 125, 0, LIGHT, FACE, SLOT, SHADOW);
        thumb(image, 135, 0, LIGHT, SLOT, SHADOW, SHADOW);
        return image;
    }

    // ------------------------------------------------------------------ the rule editor

    static BufferedImage filter() {
        BufferedImage image = sheet();
        panel(image, 0, 0, 176, 222);

        slot(image, 7, 17);
        sunken(image, 29, 17, 140, 18, SLOT);
        sunken(image, 7, 49, 162, 18, SLOT);
        slot(image, 7, 82);
        inventory(image, 7, 139, 197);

        mask(image, 176, 16, NBT_PARTIAL);
        mask(image, 192, 16, NBT_EXACT);
        draw(image, 176, 32, SHEET, PALE);
        draw(image, 192, 32, SHEET, DARK);
        return image;
    }

    // ------------------------------------------------------------------ the cell screen

    static BufferedImage cell() {
        BufferedImage image = sheet();
        panel(image, 0, 0, 176, 222);
        sunken(image, 7, 17, 122, 18, 0xFF101014);
        slot(image, 132, 17);
        slot(image, 150, 17);
        inventory(image, 7, 139, 197);
        return image;
    }

    // ------------------------------------------------------------------ the generator screen

    static BufferedImage sideConfig() {
        BufferedImage image = sheet();
        panel(image, 0, 0, 156, 135);
        sunken(image, 41, 42, 76, 76, SUNKEN);
        sunken(image, 134, 4, 18, 18, SLOT);
        sunken(image, 134, 93, 18, 18, SLOT);
        return image;
    }

    static BufferedImage generator() {
        BufferedImage image = sheet();
        panel(image, 0, 0, 176, 166);
        sunken(image, 8, 17, 160, 14, 0xFF101014);
        slot(image, 43, 41);
        slot(image, 133, 41);
        sunken(image, 62, 42, 14, 14, 0xFF101014);
        inventory(image, 7, 83, 141);
        return image;
    }

    // ------------------------------------------------------------------ chrome

    /**
     * A bevelled panel with clipped corners: a black outline, two pixels of highlight along the top and
     * left, two of shadow along the bottom and right, face colour inside.
     */
    static void panel(BufferedImage image, int ox, int oy, int width, int height) {
        boolean[][] inside = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                inside[x][y] = x + y >= 2
                        && (width - 1 - x) + y >= 2
                        && x + (height - 1 - y) >= 2
                        && (width - 1 - x) + (height - 1 - y) >= 2;
            }
        }

        List<int[]> lightEdge = new ArrayList<>();
        List<int[]> darkEdge = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!inside[x][y] || !onEdge(inside, width, height, x, y)) {
                    continue;
                }
                image.setRGB(ox + x, oy + y, OUTLINE);
                // An outline pixel lights the panel if it is the first solid pixel of its column from
                // the top or of its row from the left; the far side of each is the shadow.
                boolean light = isFirst(inside, width, height, x, y, true) || isFirst(inside, width, height, x, y, false);
                (light ? lightEdge : darkEdge).add(new int[]{x, y});
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!inside[x][y] || onEdge(inside, width, height, x, y)) {
                    if (!inside[x][y]) {
                        image.setRGB(ox + x, oy + y, CLEAR);
                    }
                    continue;
                }
                int toLight = nearest(lightEdge, x, y);
                int toDark = nearest(darkEdge, x, y);
                int band = Math.min(toLight, toDark);
                image.setRGB(ox + x, oy + y, band > 2 ? FACE : (toLight <= toDark ? LIGHT : SHADOW));
            }
        }
    }

    static boolean onEdge(boolean[][] inside, int width, int height, int x, int y) {
        return !solid(inside, width, height, x - 1, y) || !solid(inside, width, height, x + 1, y)
                || !solid(inside, width, height, x, y - 1) || !solid(inside, width, height, x, y + 1);
    }

    static boolean solid(boolean[][] inside, int width, int height, int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && inside[x][y];
    }

    static boolean isFirst(boolean[][] inside, int width, int height, int x, int y, boolean vertical) {
        if (vertical) {
            for (int probe = 0; probe < y; probe++) {
                if (inside[x][probe]) {
                    return false;
                }
            }
        } else {
            for (int probe = 0; probe < x; probe++) {
                if (inside[probe][y]) {
                    return false;
                }
            }
        }
        return true;
    }

    static int nearest(List<int[]> pixels, int x, int y) {
        int best = Integer.MAX_VALUE;
        for (int[] pixel : pixels) {
            int distance = Math.max(Math.abs(pixel[0] - x), Math.abs(pixel[1] - y));
            if (distance < best) {
                best = distance;
            }
        }
        return best;
    }

    /** A recessed well: dark along the top and left, light along the bottom and right. */
    static void sunken(BufferedImage image, int x, int y, int width, int height, int fill) {
        rect(image, x, y, width, height, fill);
        hline(image, x, y, width, SLOT_DARK);
        vline(image, x, y, height, SLOT_DARK);
        hline(image, x, y + height - 1, width, LIGHT);
        vline(image, x + width - 1, y, height, LIGHT);
        image.setRGB(x, y + height - 1, SLOT_DARK);
        image.setRGB(x + width - 1, y, SLOT_DARK);
    }

    static void slot(BufferedImage image, int x, int y) {
        sunken(image, x, y, 18, 18, SLOT);
    }

    static void inventory(BufferedImage image, int x, int y, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slot(image, x + column * 18, y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            slot(image, x + column * 18, hotbarY);
        }
    }

    /** One row of the rule list: a flat plate the widget blits under each entry. */
    static void strip(BufferedImage image, int x, int y, int width, int height, int border, int fill) {
        rect(image, x, y, width, height, fill);
        hline(image, x, y, width, border);
        hline(image, x, y + height - 1, width, border);
        vline(image, x, y, height, border);
        vline(image, x + width - 1, y, height, border);
    }

    static void thumb(BufferedImage image, int x, int y, int top, int face, int grip, int bottom) {
        rect(image, x, y + 1, 10, 17, face);
        hline(image, x, y, 10, OUTLINE);
        hline(image, x, y + 1, 10, top);
        vline(image, x, y + 1, 17, top);
        vline(image, x + 9, y + 1, 17, bottom);
        hline(image, x, y + 17, 10, bottom);
        for (int line = y + 3; line < y + 17; line += 2) {
            hline(image, x + 2, line, 6, grip);
        }
    }

    // ------------------------------------------------------------------ icons

    static final String[] NEAREST = {
            "aaaaa...........",
            "aaaaa.aaa..aaa..",
            "aaaaa.aaa..aaa..",
            "aaaaa.aaa..aaa..",
            "aaaaa...........",
            "................",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            "................",
            "................",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            "................",
            "................"
    };

    static final String[] FURTHEST = {
            "................",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            "................",
            "................",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            ".aaa..aaa..aaa..",
            "................",
            "..........aaaaa.",
            ".aaa..aaa.aaaaa.",
            ".aaa..aaa.aaaaa.",
            ".aaa..aaa.aaaaa.",
            "..........aaaaa.",
            "................"
    };

    static final String[] ROUND_ROBIN = {
            "................",
            "................",
            "................",
            "...aaaaaaaaaa...",
            "...a........a...",
            "...a......a.a.a.",
            "...........aaa..",
            "............a...",
            "...a............",
            "..aaa...........",
            ".a.a.a......a...",
            "...a........a...",
            "...aaaaaaaaaa...",
            "................",
            "................",
            "................"
    };

    static final String[] RANDOM = {
            "................",
            "................",
            "...........a....",
            "............a...",
            "..aaa......aaa..",
            ".....a....a.a...",
            "......a..a.a....",
            ".......aa.......",
            ".......aa.......",
            "......a..a.a....",
            ".....a....a.a...",
            "..aaa......aaa..",
            "............a...",
            "...........a....",
            "................",
            "................"
    };

    static final String[] REDSTONE_IGNORED = {
            "................",
            "................",
            "................",
            "................",
            ".......aa.......",
            "......abba......",
            "......acda......",
            ".......ae.......",
            ".......fg.......",
            ".......hi.......",
            ".......hg.......",
            ".......fg.......",
            ".......jk.......",
            ".......hl.......",
            ".......jm.......",
            "................"
    };

    static final String[] REDSTONE_OFF_WHEN_POWERED = {
            "................",
            "................",
            "................",
            "................",
            "................",
            ".......ab.......",
            ".......bc.......",
            ".......de.......",
            ".......fg.......",
            ".......dh.......",
            ".......dg.......",
            ".......fg.......",
            ".......ei.......",
            ".......dj.......",
            ".......ek.......",
            "................"
    };

    static final String[] REDSTONE_ON_WHEN_POWERED = {
            "................",
            "................",
            "................",
            "................",
            ".......aa.......",
            "......abca......",
            "......adea......",
            ".......af.......",
            ".......gh.......",
            ".......ij.......",
            ".......ih.......",
            ".......gh.......",
            ".......kl.......",
            ".......im.......",
            ".......kn.......",
            "................"
    };

    static final String[] REDSTONE_ALWAYS_OFF = {
            "................",
            "...abbbbbbbba...",
            "..bbbbbbbbbbbc..",
            ".abba......bbbc.",
            ".bba......bbabc.",
            ".bb......bba.bc.",
            ".bb.....bba..bc.",
            ".bb....bba...bc.",
            ".bb...bba....bc.",
            ".bb..bba.....bc.",
            ".bb.bba......bc.",
            ".bbbba......bbc.",
            ".abba......bbbc.",
            "..cbbbbbbbbbbc..",
            "...cccccccccc...",
            "................"
    };

    /** One sheet of paper; pale means allow, dark means deny, and the same shape serves invert. */
    static final String[] SHEET = {
            "................",
            "................",
            ".........aa.....",
            "........abca....",
            "......aacddba...",
            ".....abdddddce..",
            "...aacdddddddae.",
            "..acbddddddddcae",
            ".acbdddddddddbe.",
            "..ecbddddddbce..",
            "...ecbddddbee...",
            "....ecbdbfe.....",
            ".....ecfee......",
            "......ee........",
            "................",
            "................"
    };

    static final String[] NBT_PARTIAL = {
            "................",
            "................",
            "................",
            "................",
            ".a..a.aaa.aaaaa.",
            ".aa.a.a..a..a...",
            ".aa.a.a..a..a...",
            ".a.aa.aaa...a...",
            ".a.aa.a..a..a...",
            ".a..a.a..a..a...",
            ".a..a.a..a..a...",
            ".a..a.aaa...a...",
            "................",
            "..aa......aa....",
            ".a..a....a..a...",
            "................"
    };

    static final String[] NBT_EXACT = {
            "................",
            "................",
            "................",
            "................",
            ".a..a.aaa.aaaaa.",
            ".aa.a.a..a..a...",
            ".aa.a.a..a..a...",
            ".a.aa.aaa...a...",
            ".a.aa.a..a..a...",
            ".a..a.a..a..a...",
            ".a..a.a..a..a...",
            ".a..a.aaa...a...",
            "................",
            "..aaaaaaaaaa....",
            "..aaaaaaaaaa....",
            "................"
    };

    /** The ghost drawn in an empty destination slot, so the slot says what it wants. */
    static final String[] DESTINATION_SLOT = {
            "................",
            "................",
            "................",
            ".....aaaaaa.....",
            "...aa......aa...",
            "..a....bb....a..",
            "..a...bccb...a..",
            ".a...bc..cb...a.",
            ".a...bc..cb...a.",
            "..a...bccb...a..",
            "..a....bb....a..",
            "...aa......aa...",
            ".....aaaaaa.....",
            "................",
            "................",
            "................"
    };

    static final String[] RETRIEVE_OFF = {
            "................",
            "................",
            "................",
            "....aaaaaaaa....",
            "...a........a...",
            "..a...aaaa...a..",
            "..a..a....a..a..",
            "..a..a....a..a..",
            "..a..a....a..a..",
            "..a...aaaa...a..",
            "...a........a...",
            "....aaaaaaaa....",
            "................",
            "................",
            "................",
            "................"
    };

    static final String[] RETRIEVE_ON = {
            "................",
            "................",
            ".......aa.......",
            "......aaaa......",
            ".....aaaaaa.....",
            "....aa.aa.aa....",
            "...aa..aa..aa...",
            "..aa...aa...aa..",
            ".......aa.......",
            ".......aa.......",
            ".......aa.......",
            ".......aa.......",
            ".......aa.......",
            "................",
            "................",
            "................"
    };

    static final String[] THEME_DARK = {
            "................",
            "................",
            "................",
            ".....aaaa.......",
            "....abbba.......",
            "...abbbaa.......",
            "...abba.........",
            "...abba.........",
            "...abba.........",
            "...abbaa........",
            "....abbba.......",
            ".....aaaa.......",
            "................",
            "................",
            "................",
            "................"
    };

    static final String[] THEME_LIGHT = {
            "................",
            "................",
            ".....a..a..a....",
            "......aaaa......",
            ".....abbbba.....",
            "..a.abbbbbba.a..",
            "....abbbbbba....",
            "....abbbbbba....",
            "..a.abbbbbba.a..",
            ".....abbbba.....",
            "......aaaa......",
            ".....a..a..a....",
            "................",
            "................",
            "................",
            "................"
    };

    /** Every mask's palette, keyed by the mask it belongs to. */
    static final Map<String[], Map<Character, Integer>> PALETTES = new LinkedHashMap<>();

    static {
        Map<Character, Integer> white = palette("a", 0xFFFFFFFF);
        PALETTES.put(NEAREST, white);
        PALETTES.put(FURTHEST, white);
        PALETTES.put(ROUND_ROBIN, white);
        PALETTES.put(RANDOM, white);
        PALETTES.put(REDSTONE_IGNORED, palette("abcdefghijklm",
                0xFF7F7F7F, 0xFF808080, 0xFFCBCBCB, 0xFFFFFFFF, 0xFF585858, 0xFF6E6E6E, 0xFF424242,
                0xFF787878, 0xFF323232, 0xFF525252, 0xFF2D2D2D, 0xFF2B2B2B, 0xFF272727));
        PALETTES.put(REDSTONE_OFF_WHEN_POWERED, palette("abcdefghijk",
                0xFF560000, 0xFF480000, 0xFF290000, 0xFF9F7F50, 0xFF6D5736, 0xFF957546, 0xFF55452E,
                0xFF423522, 0xFF3D301D, 0xFF3B2E1B, 0xFF372A17));
        PALETTES.put(REDSTONE_ON_WHEN_POWERED, palette("abcdefghijklmn",
                0xFFFD0000, 0xFFFFD800, 0xFFFF8F00, 0xFFFFFF97, 0xFFFFFFFF, 0xFFAF0000, 0xFF957546,
                0xFF55452E, 0xFF9F7F50, 0xFF423522, 0xFF6D5736, 0xFF3D301D, 0xFF3B2E1B, 0xFF372A17));
        PALETTES.put(REDSTONE_ALWAYS_OFF, palette("abc", 0xFFBE0101, 0xFFE30000, 0xFFB10000));
        PALETTES.put(NBT_PARTIAL, palette("a", 0xFF0000FF));
        PALETTES.put(RETRIEVE_OFF, palette("a", 0xFF6E7681));
        PALETTES.put(RETRIEVE_ON, palette("a", 0xFF4CAF50));
        PALETTES.put(THEME_DARK, palette("ab", 0xFF1B1F26, 0xFF9AA3B2));
        PALETTES.put(THEME_LIGHT, palette("ab", 0xFF1B1F26, 0xFFE8C34A));
        PALETTES.put(NBT_EXACT, palette("a", 0xFF0000FF));
        PALETTES.put(DESTINATION_SLOT, palette("abcd", 0xFF373737, 0xFF8B8B8B, 0xFFFFFFFF, 0xFF000000));
    }

    /** The pale and dark variants of the same sheet shape, used for the two-state icons. */
    static final Map<Character, Integer> PALE = palette("abcdef",
            0xFFAEAEAE, 0xFFE9EAEB, 0xFFD6D6D6, 0xFFFCFCF2, 0xFF878787, 0xFFC1C1C1);
    static final Map<Character, Integer> DARK = palette("abcdef",
            0xFF515151, 0xFF161514, 0xFF292929, 0xFF03030D, 0xFF787878, 0xFF3E3E3E);

    static void mask(BufferedImage image, int ox, int oy, String[] rows) {
        draw(image, ox, oy, rows, PALETTES.get(rows));
    }

    static void draw(BufferedImage image, int ox, int oy, String[] rows, Map<Character, Integer> palette) {
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            for (int x = 0; x < row.length(); x++) {
                Integer colour = palette.get(row.charAt(x));
                if (colour != null) {
                    image.setRGB(ox + x, oy + y, colour);
                }
            }
        }
    }

    static Map<Character, Integer> palette(String keys, int... colours) {
        Map<Character, Integer> palette = new LinkedHashMap<>();
        for (int i = 0; i < keys.length(); i++) {
            palette.put(keys.charAt(i), colours[Math.min(i, colours.length - 1)]);
        }
        return palette;
    }

    // ------------------------------------------------------------------ primitives

    static BufferedImage sheet() {
        return new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    }

    static void rect(BufferedImage image, int x, int y, int width, int height, int colour) {
        for (int dy = 0; dy < height; dy++) {
            hline(image, x, y + dy, width, colour);
        }
    }

    static void hline(BufferedImage image, int x, int y, int length, int colour) {
        for (int i = 0; i < length; i++) {
            put(image, x + i, y, colour);
        }
    }

    static void vline(BufferedImage image, int x, int y, int length, int colour) {
        for (int i = 0; i < length; i++) {
            put(image, x, y + i, colour);
        }
    }

    static void put(BufferedImage image, int x, int y, int colour) {
        if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
            image.setRGB(x, y, colour);
        }
    }
}
