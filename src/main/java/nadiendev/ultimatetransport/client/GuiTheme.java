package nadiendev.ultimatetransport.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public enum GuiTheme {
    DARK(0xB0B6C0, 0xE0E0E0, 0xC0101014),
    LIGHT(0x404040, 0xE0E0E0, 0xC0101014);

    private final int label;
    private final int field;
    private final int overlay;

    GuiTheme(int label, int field, int overlay) {
        this.label = label;
        this.field = field;
        this.overlay = overlay;
    }

    public int label() {
        return label;
    }

    public int field() {
        return field;
    }

    public int overlay() {
        return overlay;
    }

    public boolean dark() {
        return this == DARK;
    }

    public static ModConfigSpec.EnumValue<GuiTheme> CONFIG;

    public static GuiTheme current() {
        return CONFIG == null ? DARK : CONFIG.get();
    }

    public static void toggle() {
        if (CONFIG != null) {
            CONFIG.set(current().dark() ? LIGHT : DARK);
            CONFIG.save();
        }
    }

    public static ResourceLocation skin(ResourceLocation light, ResourceLocation dark) {
        return current().dark() ? dark : light;
    }
}
