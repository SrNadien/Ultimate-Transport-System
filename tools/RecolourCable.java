import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class RecolourCable {

    public static void main(String[] args) throws Exception {
        String root = args[0];
        String dir = root + "/src/main/resources/assets/ultimatetransport/textures/block/";
        recolour(dir + "energy_cable_core.png", dir + "source_cable_core.png", 0x00E6B4, 0x4FE3C8);
        recolour(dir + "energy_cable_connector.png", dir + "source_cable_connector.png", 0x00E6B4, 0x4FE3C8);
        System.out.println("source cable recoloured");
    }

    static void recolour(String from, String to, int oldHue, int newHue) throws Exception {
        BufferedImage image = ImageIO.read(new File(from));
        float[] source = hsb(oldHue);
        float[] target = hsb(newHue);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = argb >>> 24;
                if (alpha == 0) {
                    continue;
                }
                float[] hsb = java.awt.Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
                if (hsb[1] < 0.12F) {
                    continue;
                }
                float hue = hsb[0] + (target[0] - source[0]);
                float saturation = Math.min(1.0F, hsb[1] * (target[1] / Math.max(source[1], 0.001F)));
                int rgb = java.awt.Color.HSBtoRGB(hue - (float) Math.floor(hue), saturation, hsb[2]);
                image.setRGB(x, y, (alpha << 24) | (rgb & 0xFFFFFF));
            }
        }
        ImageIO.write(image, "PNG", new File(to));
    }

    static float[] hsb(int rgb) {
        return java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
    }
}
