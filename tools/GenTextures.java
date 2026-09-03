import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Texture generator for Ultimate Transport System. Every pixel here is authored: block textures are
 * built procedurally from a tube profile, item textures from explicit 16x16 masks.
 */
public class GenTextures {

    static final int GAUGE_STEPS = 8;

    static Path blockDir;
    static Path root;

    public static void main(String[] args) throws IOException {
        root = Path.of(args[0]);
        blockDir = root.resolve("src/main/resources/assets/ultimatetransport/textures/block");
        Files.createDirectories(blockDir);

        writeBlock("player_tube_core", tubeGlass());
        writeBlock("player_tube_connector", tubeRing());

        Map<String, Integer> pipes = new LinkedHashMap<>();
        pipes.put("transport_pipe", 0x9A6B3F);
        pipes.put("extraction_pipe", 0x4C9A45);
        pipes.put("speed_pipe", 0xE0B33A);
        pipes.put("void_pipe", 0x50356B);
        for (Map.Entry<String, Integer> entry : pipes.entrySet()) {
            writeBlock(entry.getKey() + "_core", pipeCore(entry.getValue()));
            writeBlock(entry.getKey() + "_connector", pipeCollar(entry.getValue()));
        }
        writeBlock("pipe_connector", pipeRing());
        writeBlock("pipe_extract", hazard());
        writeBlock("cable_extract", chevron(0xE0752D, false));

        Map<String, Integer> cables = new LinkedHashMap<>();
        cables.put("energy", 0x00E6B4);
        cables.put("fluid", 0x3A7BD5);
        cables.put("item", 0xC8AA46);
        cables.put("gas", 0xB45AD2);
        cables.put("source", 0x8CD9FF);
        cables.put("universal", 0xD8DCE4);
        for (Map.Entry<String, Integer> entry : cables.entrySet()) {
            writeBlock(entry.getKey() + "_cable_core", cableCore(entry.getValue()));
            writeBlock(entry.getKey() + "_cable_connector", cableCollar(entry.getValue()));
        }
        writeBlock("cable_structure", cableStructure());

        // Item art is hand drawn and lives in the repository; this generator only writes block
        // textures, so a run can never paint over it.

        Map<String, Integer> cells = new LinkedHashMap<>();
        int[] cellColours = {
                0xB87333, 0xD8D8D8, 0xFFD24A, 0xE03030, 0x4FC3F7,
                0x3ED66A, 0xB07A4A, 0x7A5F86, 0x1FD6D6, 0xFFF2A0, 0xE8F0FF
        };
        for (int i = 0; i < cellColours.length; i++) {
            cells.put("energy_cell_" + (i + 1), cellColours[i]);
        }
        cells.put("creative_energy_cell", 0xFF5AE0);
        for (Map.Entry<String, Integer> cell : cells.entrySet()) {
            for (int lit = 0; lit <= GAUGE_STEPS; lit++) {
                writeBlock(cell.getKey() + "_side_" + lit, cellSide(cell.getValue(), lit));
            }
            writeBlock(cell.getKey() + "_end", cellEnd(cell.getValue()));
        }
        writeBlock("battery_edge", bankEdge());

        Map<String, Integer> generators = new LinkedHashMap<>();
        generators.put("fuel_generator", 0xE07A2D);
        generators.put("lava_generator", 0xE04A1E);
        generators.put("solar_generator", 0x3FA9E0);
        generators.put("nether_star_generator", 0xF2F0D8);
        for (Map.Entry<String, Integer> generator : generators.entrySet()) {
            writeBlock(generator.getKey() + "_side", casing());
            writeBlock(generator.getKey() + "_top", generatorTop(generator.getValue()));
            writeBlock(generator.getKey() + "_front", generatorFront(generator.getValue(), false));
            writeBlock(generator.getKey() + "_front_on", generatorFront(generator.getValue(), true));
        }

        ImageIO.write(logo(), "PNG", root.resolve("src/main/resources/logo.png").toFile());
        System.out.println("textures written");
    }

    // ------------------------------------------------------------------ block textures

    /** A tube seen side on: dark casing top and bottom, metal collars, a lit channel in the middle. */
    /** Plain casing, used for facade backing and cell shells. */
    static BufferedImage pipeCore(int colour) {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int c;
                if (edge == 0) {
                    c = 0x0B0D10;
                } else if (edge <= 2) {
                    c = shade(colour, edge == 1 ? 1.0f : 0.72f);
                } else {
                    int depth = Math.min(edge - 3, 4);
                    c = switch (depth) {
                        case 0 -> 0x151920;
                        case 1 -> 0x1D222B;
                        case 2 -> 0x232935;
                        default -> 0x1A1F27;
                    };
                }
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        return image;
    }

    static BufferedImage pipeCollar(int colour) {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int c;
                if (edge == 0) {
                    c = 0x0B0D10;
                } else {
                    float light = switch (Math.min(edge - 1, 3)) {
                        case 0 -> 1.18f;
                        case 1 -> 1.05f;
                        case 2 -> 0.9f;
                        default -> 0.78f;
                    };
                    c = shade(colour, light);
                }
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        for (int[] stud : new int[][]{{3, 3}, {12, 3}, {3, 12}, {12, 12}}) {
            image.setRGB(stud[0], stud[1], 0xFF000000 | shade(colour, 0.55f));
        }
        return image;
    }

    /** How far into its bar a pixel sits: 0 at the outer edge, 2 at the inner one. */
    /**
     * The collar a cable wears on a configured face. Chevrons point inward on an insert face and
     * outward on an extract one, so which way the cargo is going is readable without opening anything.
     */
    static BufferedImage chevron(int colour, boolean inward) {
        BufferedImage image = blank(16);
        int dark = shade(colour, 0.45f);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                image.setRGB(x, y, 0xFF000000 | (edge == 0 ? 0x1A1A1A : edge == 1 ? dark : colour));
            }
        }
        int light = shade(colour, 1.6f);
        for (int arm = 0; arm < 5; arm++) {
            int y = inward ? 5 + arm : 9 - arm;
            for (int thickness = 0; thickness < 2; thickness++) {
                image.setRGB(3 + arm, y + thickness, 0xFF000000 | light);
                image.setRGB(12 - arm, y + thickness, 0xFF000000 | light);
            }
        }
        return image;
    }

    /** Yellow and black hazard stripes: the band that marks the face a pipe pulls from. */
    static BufferedImage hazard() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int c;
                if (edge == 0) {
                    c = 0x1A1A1A;
                } else {
                    boolean stripe = Math.floorMod(x + y, 8) < 4;
                    c = stripe ? 0xF2C42C : 0x24242A;
                }
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        return image;
    }

    static BufferedImage pipeRing() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int c = switch (edge) {
                    case 0 -> 0x2A2A30;
                    case 1, 2 -> 0x8A8F98;
                    default -> 0x6A707A;
                };
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        for (int[] stud : new int[][]{{2, 2}, {13, 2}, {2, 13}, {13, 13}}) {
            for (int dx = 0; dx < 2; dx++) {
                for (int dy = 0; dy < 2; dy++) {
                    image.setRGB(stud[0] - dx, stud[1] - dy, 0xFF000000 | 0xB4BAC4);
                }
            }
        }
        return image;
    }

    /** The see-through wall of a player tube: tinted glass held by an opaque frame. */
    static BufferedImage tubeGlass() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                if (edge == 0) {
                    image.setRGB(x, y, 0xFF1E2228);
                } else if (edge == 1) {
                    image.setRGB(x, y, 0xFF4A525E);
                } else {
                    boolean streak = (x + y) % 7 == 0;
                    int glass = streak ? 0xA8D8F2 : 0x69B6DC;
                    int alpha = streak ? 0x70 : 0x4E;
                    image.setRGB(x, y, (alpha << 24) | glass);
                }
            }
        }
        for (int i = 2; i < 14; i++) {
            image.setRGB(i, 2, 0x66E4F4FF);
            image.setRGB(2, i, 0x66E4F4FF);
        }
        return image;
    }

    /** The opaque collar where two tube sections meet. */
    static BufferedImage tubeRing() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int c = switch (edge) {
                    case 0 -> 0x1E2228;
                    case 1, 2 -> 0x7B838F;
                    case 3 -> 0x4A525E;
                    default -> 0x5E6672;
                };
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        for (int[] bolt : new int[][]{{2, 2}, {13, 2}, {2, 13}, {13, 13}, {7, 1}, {8, 14}}) {
            image.setRGB(bolt[0], bolt[1], 0xFF000000 | 0xB4BCC8);
        }
        return image;
    }

    /**
     * The plate a battery face is built on. It carries no border of its own: the raised edge is a
     * separate strip the model lays along whichever sides have no battery next to them, so a stack
     * of them reads as one machine instead of a grid of boxes.
     */
    static BufferedImage bankShell() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean seam = x == 0 || y == 0;
                image.setRGB(x, y, 0xFF000000 | (seam ? 0x1D2126 : 0x23272D));
            }
        }
        return image;
    }

    /** The raised edge laid along an open side of a battery, lit at the top and dark at the foot. */
    static BufferedImage bankEdge() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            int c = y < 3 ? 0x5A616C : y < 12 ? 0x3C424B : 0x14171B;
            for (int x = 0; x < 16; x++) {
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        for (int x = 2; x < 16; x += 5) {
            image.setRGB(x, 6, 0xFF000000 | 0x7C838F);
            image.setRGB(x, 7, 0xFF000000 | 0x272C33);
        }
        return image;
    }

    /**
     * Battery face: a channel that runs the whole height of the texture, so two of them stacked make
     * one unbroken line instead of a window per block, and a fill that climbs it from the floor.
     */
    static BufferedImage cellSide(int colour, int lit) {
        BufferedImage image = bankShell();
        for (int y = 0; y < 16; y++) {
            for (int x = 5; x < 11; x++) {
                image.setRGB(x, y, 0xFF000000 | 0x090B0E);
            }
            image.setRGB(4, y, 0xFF000000 | 0x14171B);
            image.setRGB(11, y, 0xFF000000 | 0x14171B);
        }
        int filled = lit * 16 / GAUGE_STEPS;
        for (int step = 0; step < filled; step++) {
            int y = 15 - step;
            int c = shade(colour, 0.85f + step * 0.02f);
            for (int x = 6; x < 10; x++) {
                image.setRGB(x, y, 0xFF000000 | (step % 4 == 3 ? shade(c, 0.7f) : c));
            }
        }
        return image;
    }

    /** Capacitor cap: the same chassis with a vent grille and a port in the tier colour. */
    static BufferedImage cellEnd(int colour) {
        BufferedImage image = bankShell();
        for (int y = 3; y < 13; y++) {
            for (int x = 3; x < 13; x++) {
                image.setRGB(x, y, 0xFF000000 | (y % 2 == 0 ? 0x191D22 : 0x0D0F13));
            }
        }
        for (int y = 6; y < 10; y++) {
            for (int x = 6; x < 10; x++) {
                boolean ring = x == 6 || x == 9 || y == 6 || y == 9;
                image.setRGB(x, y, 0xFF000000 | (ring ? shade(colour, 0.6f) : shade(colour, 1.2f)));
            }
        }
        return image;
    }

    /** Generator front: a vent grille that goes dark when the machine is idle and glows when it runs. */
    static BufferedImage generatorFront(int colour, boolean lit) {
        BufferedImage image = casing();
        for (int y = 4; y < 12; y++) {
            for (int x = 3; x < 13; x++) {
                image.setRGB(x, y, 0xFF000000 | 0x0E1014);
            }
        }
        // Four louvres, brightest at the top so the grille reads as lit from above.
        for (int i = 0; i < 4; i++) {
            int y = 5 + i * 2;
            int c = lit ? shade(colour, 1.3f - i * 0.14f) : shade(0x2A2E36, 1.0f + i * 0.05f);
            for (int x = 4; x < 12; x++) {
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        return image;
    }

    /** Generator top: the casing with a coloured intake plate so the three read apart from above. */
    static BufferedImage generatorTop(int colour) {
        BufferedImage image = casing();
        for (int y = 4; y < 12; y++) {
            for (int x = 4; x < 12; x++) {
                boolean rim = x == 4 || x == 11 || y == 4 || y == 11;
                image.setRGB(x, y, 0xFF000000 | (rim ? 0x16181C : shade(colour, 0.8f)));
            }
        }
        return image;
    }

    static BufferedImage cableCore(int colour) {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                float light = 1.18f - (y % 4) * 0.13f;
                if (((x * 7 + y * 5) % 11) == 0) {
                    light += 0.12f;
                }
                if (y % 8 == 3) {
                    light -= 0.22f;
                }
                image.setRGB(x, y, 0xFF000000 | shade(colour, light));
            }
        }
        return image;
    }

    static BufferedImage cableCollar(int colour) {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int ring = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int inner = Math.min(Math.min(Math.abs(x - 3), Math.abs(x - 12)),
                        Math.min(Math.abs(y - 3), Math.abs(y - 12)));
                float light;
                if (ring <= 1) {
                    light = 0.42f;
                } else if (inner == 0) {
                    light = 0.58f;
                } else {
                    light = 1.05f - (y % 3) * 0.09f;
                }
                image.setRGB(x, y, 0xFF000000 | shade(colour, light));
            }
        }
        return image;
    }

    static BufferedImage cableStructure() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int base = switch ((x * 3 + y * 7) % 9) {
                    case 0 -> 0x4A525C;
                    case 1, 2 -> 0x3C434C;
                    case 3, 4, 5 -> 0x333941;
                    case 6, 7 -> 0x2B3037;
                    default -> 0x232830;
                };
                if (x % 4 == 0) {
                    base = shade(base, 1.12f);
                }
                image.setRGB(x, y, 0xFF000000 | base);
            }
        }
        return image;
    }

    static BufferedImage casing() {
        BufferedImage image = blank(16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int c = switch (edge) {
                    case 0 -> 0x16181C;
                    case 1 -> 0x6E7480;
                    default -> 0x565C66;
                };
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        for (int[] rivet : new int[][]{{2, 2}, {13, 2}, {2, 13}, {13, 13}}) {
            image.setRGB(rivet[0], rivet[1], 0xFF000000 | 0x9AA1AE);
        }
        return image;
    }

    // ------------------------------------------------------------------ item textures

    static BufferedImage chip(int colour) {
        Map<Character, Integer> palette = new LinkedHashMap<>();
        palette.put('0', 0xFF17181C);
        palette.put('a', 0xFF000000 | colour);
        palette.put('b', 0xFF000000 | shade(colour, 1.45f));
        palette.put('c', 0xFF000000 | shade(colour, 0.58f));
        palette.put('g', 0xFFD9B45A);
        return mask(CHIP, palette);
    }

    static Map<Character, Integer> wrenchPalette() {
        Map<Character, Integer> palette = new LinkedHashMap<>();
        String keys = "abcdefghijklmnopqrst";
        int[] colours = {0xFF9D9D9D, 0xFFF0F1F1, 0xFF676767, 0xFFCACBCB, 0xFF8F9191, 0xFF3F3F3F,
                0xFF18BAEF, 0xFF0E90A7, 0xFF0E5867, 0xFFD58C44, 0xFFC27041, 0xFF0E4550, 0xFF0E8CA5,
                0xFF1F1F1F, 0xFFB45C3E, 0xFF412F26, 0xFFA8A9A9, 0xFF868A8A, 0xFF844934, 0xFF555656};
        for (int i = 0; i < keys.length(); i++) {
            palette.put(keys.charAt(i), colours[i]);
        }
        return palette;
    }

    static Map<Character, Integer> facadePalette() {
        Map<Character, Integer> palette = new LinkedHashMap<>();
        palette.put('0', 0xFF1B1E24);
        palette.put('1', 0xFF3E444E);
        palette.put('2', 0xFF6E7480);
        palette.put('3', 0xFF9AA1AE);
        palette.put('e', 0xFF00E6B4);
        return palette;
    }

    static final String[] CHIP = {
            "................",
            "................",
            "..000000000000..",
            "..0cccccccccc0..",
            "..0caaaaaaaac0..",
            "..0ca0bbbb0ac0..",
            "..0ca0b00b0ac0..",
            "..0ca0b00b0ac0..",
            "..0ca0bbbb0ac0..",
            "..0caaaaaaaac0..",
            "..0cccccccccc0..",
            "..000000000000..",
            "...0g0.0g0.0g0..",
            "...0g0.0g0.0g0..",
            "...000.000.000..",
            "................"
    };

    static final String[] WRENCH = {
            "................",
            "..........aa....",
            ".........abc....",
            "........abc.....",
            "........abc..aa.",
            "........adecabc.",
            ".......faddbbc..",
            "......fgedccc...",
            ".....fghic......",
            "....fjklmn......",
            "...fjoopn.......",
            "..fqrspn........",
            "..frrtn.........",
            "..nttn..........",
            "...nn...........",
            "................"
    };

    static Map<Character, Integer> targetPalette() {
        Map<Character, Integer> palette = new LinkedHashMap<>();
        palette.put('0', 0xFF1B2A1E);
        palette.put('e', 0xFF4CAF50);
        palette.put('w', 0xFFB6F0BC);
        return palette;
    }

    static final String[] TARGET = {
            "................",
            "................",
            ".......00.......",
            ".......ee.......",
            "...0...ee...0...",
            "...0e.eeee.e0...",
            ".....ee00ee.....",
            "..0eee0ww0eee0..",
            "..0eee0ww0eee0..",
            ".....ee00ee.....",
            "...0e.eeee.e0...",
            "...0...ee...0...",
            ".......ee.......",
            ".......00.......",
            "................",
            "................"
    };

    static final String[] FACADE = {
            "................",
            ".00000000000000.",
            ".03333333333310.",
            ".03222222222210.",
            ".03222222222210.",
            ".0322eeeeee2210.",
            ".0322e0000e2210.",
            ".0322e0000e2210.",
            ".0322e0000e2210.",
            ".0322eeeeee2210.",
            ".03222222222210.",
            ".03222222222210.",
            ".03222222222210.",
            ".01111111111110.",
            ".00000000000000.",
            "................"
    };

    // ------------------------------------------------------------------ logo

    static BufferedImage logo() {
        int size = 128;
        BufferedImage image = blank(size);
        int background = 0x181B21;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int corner = Math.min(Math.min(x, size - 1 - x), Math.min(y, size - 1 - y));
                if (corner < 4 && (x < 10 || x > size - 11) && (y < 10 || y > size - 11)) {
                    continue;
                }
                image.setRGB(x, y, 0xFF000000 | (corner < 3 ? 0x0F1116 : background));
            }
        }
        bar(image, 0x00E6B4, 0, size, 52, 76, true);
        bar(image, 0xC8AA46, 0, size, 52, 76, false);
        bar(image, 0x3A7BD5, 0, 46, 20, 34, true);
        bar(image, 0xB45AD2, 82, size, 94, 108, true);
        for (int y = 44; y < 84; y++) {
            for (int x = 44; x < 84; x++) {
                int edge = Math.min(Math.min(x - 44, 83 - x), Math.min(y - 44, 83 - y));
                int c = edge < 2 ? 0x23262B : edge < 5 ? 0x6E7480 : 0x8F97A5;
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
        return image;
    }

    static void bar(BufferedImage image, int colour, int from, int to, int lo, int hi, boolean horizontal) {
        for (int a = from; a < to; a++) {
            for (int b = lo; b < hi; b++) {
                int x = horizontal ? a : b;
                int y = horizontal ? b : a;
                if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
                    continue;
                }
                float t = (float) (b - lo) / (hi - lo);
                int c = lerp(shade(colour, 1.5f), shade(colour, 0.4f), t);
                if (b == lo || b == hi - 1) {
                    c = 0x1B1D21;
                }
                image.setRGB(x, y, 0xFF000000 | c);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    static BufferedImage mask(String[] rows, Map<Character, Integer> palette) {
        BufferedImage image = blank(rows.length);
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            if (row.length() != rows.length) {
                throw new IllegalStateException("row " + y + " is " + row.length() + " wide");
            }
            for (int x = 0; x < row.length(); x++) {
                Integer colour = palette.get(row.charAt(x));
                if (colour != null) {
                    image.setRGB(x, y, colour);
                }
            }
        }
        return image;
    }

    static BufferedImage blank(int size) {
        return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    }

    static int shade(int colour, float factor) {
        int r = clamp((int) (((colour >> 16) & 0xFF) * factor));
        int g = clamp((int) (((colour >> 8) & 0xFF) * factor));
        int b = clamp((int) ((colour & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }

    static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(bl);
    }

    static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    static void writeBlock(String name, BufferedImage image) throws IOException {
        write(blockDir.resolve(name + ".png").toFile(), image);
    }

    static void write(File file, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", file);
    }
}
