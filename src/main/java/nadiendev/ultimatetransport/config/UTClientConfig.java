package nadiendev.ultimatetransport.config;

import nadiendev.ultimatetransport.client.GuiTheme;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class UTClientConfig {
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("gui");
        GuiTheme.CONFIG = builder
                .comment("Skin used by the cable, filter and cell screens.")
                .translation("config.ultimatetransport.gui_theme")
                .defineEnum("theme", GuiTheme.DARK);
        builder.pop();
        SPEC = builder.build();
    }

    private UTClientConfig() {
    }
}
