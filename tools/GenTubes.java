import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenTubes {
    static final short[] CONNECTION_TO_INDEX = {0, 15, 13, 11, 12, 5, 3, 9, 14, 4, 2, 10, 8, 7, 6, 1};

    static final int GLASS_DEEP = 0x0E1A22;
    static final int GLASS = 0x16262F;
    static final int GLASS_LIT = 0x1D323D;

    static final int RIM = 0x4FE3C8;
    static final int RIM_DARK = 0x1E6B62;

    static final int[] FACING_RIM = {0x4F9FE3, 0x5FE04F, 0xE34F4F, 0xE3C94F, 0xE3814F, 0xB44FE3};
    static final int EDGE = 0x081014;

    static final int GLASS_ALPHA = 0x38;
    static final int SHEEN_ALPHA = 0x66;

    static final int STEEL_DARK = 0x2A2F36;
    static final int STEEL = 0x3A4048;
    static final int STEEL_LIT = 0x4A515B;
    static final int STEEL_RIVET = 0x6A727E;

    static Path blockDir;

    public static void main(String[] args) throws IOException {
        blockDir = Path.of(args[0], "src/main/resources/assets/ultimatetransport/textures/block");
        Files.createDirectories(blockDir);

        for (int facing = 0; facing < 6; facing++) {
            Files.createDirectories(blockDir.resolve("tube" + facing));
            for (int index = 0; index < 16; index++) {
                write("tube" + facing + "/" + index, lateral(open(index), FACING_RIM[facing]));
            }
        }
        Files.createDirectories(blockDir.resolve("tube"));
        for (int index = 0; index < 16; index++) {
            write("tube/" + index, cap(open(index)));
        }

        write("tube_nodir", greyed(lateral(new boolean[4])));

        write("station_side1", panel(false, false));
        write("station_side2", panel(true, false));
        write("station_side3", panel(false, true));
        write("station_side4", panel(true, true));
        write("station_misc", plate());
        write("station_entr1", entrance(true, false));
        write("station_entr2", entrance(false, false));
        write("station_entr3", entrance(true, true));
        write("station_entr4", entrance(false, true));
        write("station1", panel(false, false));
        write("station2", panel(true, false));

        System.out.println("tube textures written");
    }

    static boolean[] open(int spriteIndex) {
        for (int k = 0; k < 16; k++) {
            if (CONNECTION_TO_INDEX[k] == spriteIndex) {
                return new boolean[]{(k & 8) != 0, (k & 4) != 0, (k & 2) != 0, (k & 1) != 0};
            }
        }
        return new boolean[4];
    }

    static BufferedImage lateral(boolean[] joined) {
        return lateral(joined, RIM);
    }

    static BufferedImage lateral(boolean[] joined, int rim) {
        BufferedImage image = blank();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean lit = (x + y) % 8 == 0;
                boolean streak = (x - y + 16) % 8 == 0;
                int colour = lit ? GLASS_LIT : streak ? GLASS : GLASS_DEEP;
                int alpha = lit || streak ? SHEEN_ALPHA : GLASS_ALPHA;
                image.setRGB(x, y, (alpha << 24) | colour);
            }
        }
        rims(image, joined, rim);
        return image;
    }

    static BufferedImage cap(boolean[] joined) {
        BufferedImage image = blank();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double dx = x - 7.5;
                double dy = y - 7.5;
                double r = Math.sqrt(dx * dx + dy * dy);
                int colour;
                int alpha;
                if (r < 4.0) {
                    colour = GLASS_DEEP;
                    alpha = 0x00;
                } else if (r < 5.2) {
                    colour = RIM_DARK;
                    alpha = 0xFF;
                } else if (r < 6.2) {
                    colour = RIM;
                    alpha = 0xFF;
                } else {
                    colour = GLASS;
                    alpha = GLASS_ALPHA;
                }
                image.setRGB(x, y, (alpha << 24) | colour);
            }
        }
        rims(image, joined);
        return image;
    }

    static void rims(BufferedImage image, boolean[] joined) {
        rims(image, joined, RIM);
    }

    static void rims(BufferedImage image, boolean[] joined, int rim) {
        boolean left = joined[0];
        boolean right = joined[1];
        boolean bottom = joined[2];
        boolean top = joined[3];
        if (!left) {
            bar(image, 0, 0, 2, 16, rim);
        }
        if (!right) {
            bar(image, 14, 0, 2, 16, rim);
        }
        if (!top) {
            bar(image, 0, 0, 16, 2, rim);
        }
        if (!bottom) {
            bar(image, 0, 14, 16, 2, rim);
        }
    }

    static void bar(BufferedImage image, int x0, int y0, int width, int height, int rim) {
        for (int y = y0; y < y0 + height; y++) {
            for (int x = x0; x < x0 + width; x++) {
                boolean outer = x == 0 || x == 15 || y == 0 || y == 15;
                image.setRGB(x, y, 0xFF000000 | (outer ? EDGE : rim));
            }
        }
    }

    static BufferedImage panel(boolean alternate, boolean sideways) {
        BufferedImage image = blank();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int colour = switch (edge) {
                    case 0 -> EDGE;
                    case 1 -> STEEL_LIT;
                    default -> ((sideways ? x : y) / 4) % 2 == (alternate ? 1 : 0) ? STEEL : STEEL_DARK;
                };
                image.setRGB(x, y, 0xFF000000 | colour);
            }
        }
        for (int[] rivet : new int[][]{{3, 3}, {12, 3}, {3, 12}, {12, 12}}) {
            image.setRGB(rivet[0], rivet[1], 0xFF000000 | STEEL_RIVET);
        }
        return image;
    }

    static BufferedImage plate() {
        BufferedImage image = blank();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int edge = Math.min(Math.min(x, 15 - x), Math.min(y, 15 - y));
                int colour = edge == 0 ? EDGE : edge <= 2 ? STEEL_LIT : STEEL_DARK;
                image.setRGB(x, y, 0xFF000000 | colour);
            }
        }
        for (int y = 5; y < 11; y++) {
            for (int x = 5; x < 11; x++) {
                boolean ring = x == 5 || x == 10 || y == 5 || y == 10;
                image.setRGB(x, y, 0xFF000000 | (ring ? RIM_DARK : GLASS_DEEP));
            }
        }
        return image;
    }

    static BufferedImage entrance(boolean lower, boolean sideways) {
        BufferedImage image = panel(false, sideways);
        int near = 3;
        int far = 13;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int across = sideways ? y : x;
                int along = sideways ? x : y;
                if (across < near || across >= far) {
                    continue;
                }
                boolean capEnd = lower ? along >= 13 : along < 3;
                if (capEnd) {
                    continue;
                }
                boolean jamb = across == near || across == far - 1;
                image.setRGB(x, y, 0xFF000000 | (jamb ? RIM_DARK : GLASS_DEEP));
            }
        }
        return image;
    }

    static BufferedImage greyed(BufferedImage source) {
        BufferedImage image = blank();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int argb = source.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int grey = (r * 30 + g * 59 + b * 11) / 100;
                image.setRGB(x, y, (argb & 0xFF000000) | (grey << 16) | (grey << 8) | grey);
            }
        }
        return image;
    }

    static BufferedImage blank() {
        return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    }

    static void write(String name, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", new File(blockDir.resolve(name + ".png").toString()));
    }
}
