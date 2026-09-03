package nadiendev.ultimatetransport.tube;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class TubeConfig {

    public enum RenderPass {
        TRANSLUCENT, CUTOUT
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue MAX_TUBE_SPEED = BUILDER
            .comment("The maximum speed an entity can travel through the Transport Tubes")
            .translation("ultimatetransport.configuration.max_tube_speed")
            .defineInRange("MaxTubeSpeed", 0.5, 0.0, 10.0);

    public static final ModConfigSpec.BooleanValue SUPPRESS_WALK = BUILDER
            .comment("Silence footsteps and stop the head bob while riding a tube.",
                    "This is a client-side setting and does not affect other players.")
            .translation("ultimatetransport.configuration.suppress_walk")
            .define("SuppressWalkEffects", true);

    public static final ModConfigSpec.EnumValue<RenderPass> RENDER_PASS = BUILDER
            .comment("Which pass tubes and stations draw in.",
                    "TRANSLUCENT keeps the glass see-through; CUTOUT keeps them out of the pass that",
                    "can paint over another mod's sky and clouds. Takes effect on restart.")
            .translation("ultimatetransport.configuration.render_pass")
            .defineEnum("TubeRenderPass", RenderPass.TRANSLUCENT);

    public static final ModConfigSpec.BooleanValue LENIENT_CULLING = BUILDER
            .comment("Join a tube to any neighbour that points a different way, so corners and loops open",
                    "while two lanes pointing the same way keep their wall.",
                    "Off restores the original, where any two touching tubes merge.")
            .translation("ultimatetransport.configuration.lenient_culling")
            .define("LenientFaceCulling", true);

    public static final ModConfigSpec.BooleanValue PLACEMENT_PREVIEW = BUILDER
            .comment("Draw a faint outline of the tube you are about to place, showing which way it will carry.")
            .translation("ultimatetransport.configuration.placement_preview")
            .define("ShowPlacementPreview", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static double maxSpeed = 0.5;
    public static double maxSpeedInverse = -0.5;
    public static boolean suppressWalk = true;
    public static boolean lenientCulling = true;
    public static boolean placementPreview = true;

    private TubeConfig() {
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        cache(event.getConfig());
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        cache(event.getConfig());
    }

    private static void cache(net.neoforged.fml.config.ModConfig config) {
        if (config.getSpec() != SPEC) {
            return;
        }
        maxSpeed = MAX_TUBE_SPEED.get();
        maxSpeedInverse = -maxSpeed;
        suppressWalk = SUPPRESS_WALK.get();
        lenientCulling = LENIENT_CULLING.get();
        placementPreview = PLACEMENT_PREVIEW.get();
    }
}
