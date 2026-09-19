package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.CableBlock;
import nadiendev.ultimatetransport.cell.EnergyCellBlock;
import net.minecraft.core.Direction;
import nadiendev.ultimatetransport.generator.GeneratorBlock;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class UTBlockStateProvider extends BlockStateProvider {

    public UTBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, UltimateTransport.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        parents();
        UTBlocks.cables().forEach(holder -> cable(holder.get()));
        UTBlocks.generators().forEach(holder -> generator(holder.get()));
        tubes();
        batteryRims();
        UTBlocks.cells().forEach(holder -> battery(holder.get()));
    }

    /**
     * The core cube plus a rim on every open side. Two batteries of the same rung drop the rim
     * between them, so a stack of them carries one frame around the outside instead of six each.
     */
    private void battery(EnergyCellBlock block) {
        String name = block.tier().blockName();
        var builder = getMultipartBuilder(block);
        builder.part()
                .modelFile(models().cubeColumn("block/" + name,
                        modLoc("block/" + name + "_side_plain"),
                        modLoc("block/" + name + "_end")))
                .addModel()
                .end();
        for (var entry : EnergyCellBlock.JOINED.entrySet()) {
            builder.part()
                    .modelFile(models().getExistingFile(
                            modLoc("block/battery_rim_" + entry.getKey().getSerializedName())))
                    .addModel()
                    .condition(entry.getValue(), false)
                    .end();
        }
        batteryItem(name);
    }

    /** The block as it looks standing alone, rims and all, so the item is not a bare plate. */
    private void batteryItem(String name) {
        var builder = models().withExistingParent("block/" + name + "_item", "block/block")
                .texture("particle", modLoc("block/" + name + "_end"))
                .texture("side", modLoc("block/" + name + "_side_" + EnergyCellBlock.STEPS))
                .texture("end", modLoc("block/" + name + "_end"))
                .texture("rim", modLoc("block/battery_edge"))
                .renderType("minecraft:solid");
        var core = builder.element().from(0, 0, 0).to(16, 16, 16);
        for (Direction face : Direction.values()) {
            core.face(face).uvs(0, 0, 16, 16)
                    .texture(face.getAxis() == Direction.Axis.Y ? "#end" : "#side")
                    .cullface(face).end();
        }
        core.end();

        float thick = 2.0F;
        for (Direction side : Direction.values()) {
            float over = rimOver(side);
            float lo = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16.0F - thick : 0.0F;
            float[] from = {-over, -over, -over};
            float[] to = {16.0F + over, 16.0F + over, 16.0F + over};
            int axis = side.getAxis().ordinal();
            from[axis] = lo;
            to[axis] = lo + thick;

            var rim = builder.element().from(from[0], from[1], from[2]).to(to[0], to[1], to[2]);
            for (Direction face : Direction.values()) {
                if (face.getAxis() == side.getAxis()) {
                    continue;
                }
                rim.face(face).uvs(0, 0, 16, 16).texture("#rim").end();
            }
            rim.end();
        }
    }

    /**
     * How far a rim reaches past the block. Two rims meet at every corner of the cube, and if they
     * came out the same distance their faces would land on the same plane and flicker, so each axis
     * gets its own hair of clearance.
     */
    private static float rimOver(Direction side) {
        return switch (side.getAxis()) {
            case Y -> 0.03F;
            case Z -> 0.02F;
            case X -> 0.01F;
        };
    }

    /** One slab per side, written out by hand so no rotation has to be reasoned about. */
    private void batteryRims() {
        float thick = 2.0F;
        for (Direction side : Direction.values()) {
            float over = rimOver(side);
            float lo = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16.0F - thick : 0.0F;
            float hi = lo + thick;
            float[] from = {-over, -over, -over};
            float[] to = {16.0F + over, 16.0F + over, 16.0F + over};
            int axis = side.getAxis().ordinal();
            from[axis] = lo;
            to[axis] = hi;

            var builder = models().withExistingParent("block/battery_rim_" + side.getSerializedName(), "block/block")
                    .texture("particle", modLoc("block/battery_edge"))
                    .texture("rim", modLoc("block/battery_edge"))
                    .renderType("minecraft:solid");
            var element = builder.element().from(from[0], from[1], from[2]).to(to[0], to[1], to[2]);
            for (Direction face : Direction.values()) {
                if (face.getAxis() == side.getAxis()) {
                    continue;
                }
                element.face(face).uvs(0, 0, 16, 16).texture("#rim").cullface(face).end();
            }
            element.end();
        }
    }

    private void parents() {
        shape(models().withExistingParent("block/cable_core_shape", "block/block")
                .texture("particle", "#core"), CABLE_CORE);
        shape(models().withExistingParent("block/cable_arm_shape", "block/block")
                .texture("particle", "#core"), CABLE_ARM);

        collar(models().withExistingParent("block/cable_extract", "block/block")
                .texture("particle", modLoc("block/cable_extract"))
                .texture("band", modLoc("block/cable_extract")), 3, 0, 13, 2, "#band", 4);
    }

    static void tubeShape(BlockModelBuilder builder, int lo, int near, int hi, int far, String texture, Integer unused) {
        builder.element()
                .from(lo, lo, near).to(hi, hi, far)
                .allFaces((face, uv) -> uv.uvs(0, 0, 16, 16).texture(texture))
                .end();
    }

    static void collar(BlockModelBuilder builder, int lo, int near, int hi, int far, String texture, int span) {
        builder.element()
                .from(lo, lo, near).to(hi, hi, far)
                .face(Direction.NORTH).uvs(0, 0, 16, 16).texture(texture).end()
                .face(Direction.EAST).uvs(0, 0, span, 16).texture(texture).end()
                .face(Direction.WEST).uvs(0, 0, span, 16).texture(texture).end()
                .face(Direction.UP).uvs(0, 0, 16, span).texture(texture).end()
                .face(Direction.DOWN).uvs(0, 0, 16, span).texture(texture).end()
                .end();
    }

    static void sleeve(BlockModelBuilder builder, int lo, int near, int hi, int far, String texture, int inset) {
        builder.element()
                .from(lo, lo, near).to(hi, hi, far)
                .face(Direction.EAST).uvs(0, inset, 16, 16).texture(texture).end()
                .face(Direction.WEST).uvs(0, inset, 16, 16).texture(texture).end()
                .face(Direction.UP).uvs(0, inset, 16, 16).texture(texture).end()
                .face(Direction.DOWN).uvs(0, inset, 16, 16).texture(texture).end()
                .end();
    }

    private void tubes() {
        var tube = models().getBuilder("block/tube")
                .customLoader((parent, helper) -> new LoaderModel<>(UltimateTransport.id("tube"), parent, helper))
                .end();
        var station = models().getBuilder("block/station")
                .customLoader((parent, helper) -> new LoaderModel<>(UltimateTransport.id("station"), parent, helper))
                .end();
        getMultipartBuilder(TubeRegistration.TUBE.get()).part().modelFile(tube).addModel().end();
        getMultipartBuilder(TubeRegistration.STATION.get()).part().modelFile(station).addModel().end();
        getMultipartBuilder(TubeRegistration.STATION_HORIZONTAL.get()).part().modelFile(station).addModel().end();
    }

    private void generator(GeneratorBlock block) {
        String name = block.type().blockName();
        var off = models().orientable("block/" + name, modLoc("block/" + name + "_side"),
                modLoc("block/" + name + "_front"), modLoc("block/" + name + "_top"));
        var on = models().orientable("block/" + name + "_on", modLoc("block/" + name + "_side"),
                modLoc("block/" + name + "_front_on"), modLoc("block/" + name + "_top"));
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(GeneratorBlock.LIT) ? on : off)
                .rotationY(((int) state.getValue(GeneratorBlock.FACING).toYRot() + 180) % 360)
                .build());
    }

    private void cable(CableBlock block) {
        String name = name(block);
        BlockModelBuilder core = models()
                .withExistingParent("block/" + name + "_core", modLoc("block/cable_core_shape"))
                .texture("core", modLoc("block/" + name + "_core"))
                .texture("connector", modLoc("block/" + name + "_connector"))
                .texture("structure", modLoc("block/cable_structure"));
        BlockModelBuilder arm = models()
                .withExistingParent("block/" + name + "_arm", modLoc("block/cable_arm_shape"))
                .texture("core", modLoc("block/" + name + "_core"))
                .texture("connector", modLoc("block/" + name + "_connector"))
                .texture("structure", modLoc("block/cable_structure"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        builder.part().modelFile(core).addModel().end();

        for (Direction direction : Direction.values()) {
            builder.part()
                    .modelFile(arm)
                    .rotationX(rotationX(direction))
                    .rotationY(rotationY(direction))
                    .addModel()
                    .condition(CableBlock.PROPERTY_BY_DIRECTION.get(direction), true)
                    .end();
        }
    }

    private static int rotationX(Direction direction) {
        return switch (direction) {
            case UP -> 270;
            case DOWN -> 90;
            default -> 0;
        };
    }

    private static int rotationY(Direction direction) {
        return switch (direction) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static String name(CableBlock block) {
        return block.type().cableName();
    }
    private static final String[] CABLE_CORE = {
            "4,4,4,12,6,6|down:4,10,12,12:structure|east:10,10,12,12:structure|north:4,10,12,12:structure|south:4,9,12,11:structure|up:4,9,12,11:structure|west:4,10,6,12:structure",
            "4,10,4,12,12,6|down:4,5,12,7:structure|east:10,4,12,6:structure|north:4,4,12,6:structure|south:4,5,12,7:structure|up:4,4,12,6:structure|west:4,4,6,6:structure",
            "4,6,4,6,10,6|down:10,4,12,5:structure|east:9,6,11,10:structure|north:10,6,12,10:structure|south:5,6,7,10:structure|up:10,4,12,5:structure|west:4,6,6,10:structure",
            "10,6,4,12,10,6|down:4,4,6,5:structure|east:10,6,12,10:structure|north:4,6,6,10:structure|south:10,6,12,10:structure|up:4,4,6,5:structure|west:5,6,7,10:structure",
            "4,4,10,12,6,12|down:4,4,12,6:structure|east:4,10,6,12:structure|north:4,9,12,11:structure|south:4,10,12,12:structure|up:4,9,12,11:structure|west:10,10,12,12:structure",
            "4,6,10,6,10,12|down:10,4,12,5:structure|east:9,6,11,10:structure|north:9,6,11,10:structure|south:4,6,6,10:structure|up:10,4,12,5:structure|west:10,6,12,10:structure",
            "4,10,10,12,12,12|down:4,5,12,7:structure|east:4,4,6,6:structure|north:4,5,12,7:structure|south:4,4,12,6:structure|up:4,10,12,12:structure|west:10,4,12,6:structure",
            "10,6,10,12,10,12|down:4,4,6,5:structure|east:4,6,6,10:structure|north:5,6,7,10:structure|south:10,6,12,10:structure|up:4,4,6,5:structure|west:5,6,7,10:structure",
            "4,4,6,6,6,10|down:4,6,6,10:structure|east:6,5,10,7:structure|north:7,7,9,9:structure|south:7,7,9,9:structure|up:9,6,11,10:structure|west:6,10,10,12:structure",
            "4,10,6,6,12,10|down:5,6,7,10:structure|east:6,5,10,7:structure|north:7,7,9,9:structure|south:7,7,9,9:structure|up:4,6,6,10:structure|west:6,4,10,6:structure",
            "10,10,6,12,12,10|down:5,6,7,10:structure|east:6,4,10,6:structure|north:7,7,9,9:structure|south:7,7,9,9:structure|up:10,6,12,10:structure|west:6,5,10,7:structure",
            "10,4,6,12,6,10|down:10,6,12,10:structure|east:6,10,10,12:structure|north:7,7,9,9:structure|south:7,7,9,9:structure|up:5,6,7,10:structure|west:6,5,10,7:structure",
            "5,5,5,11,11,11|down:5,5,11,11:core|east:5,5,11,11:core|north:5,5,11,11:core|south:5,5,11,11:core|up:5,5,11,11:core|west:5,5,11,11:core"
    };
    private static final String[] CABLE_ARM = {
            "4,5,0,5,6,4|down:3,5,4,9:structure|east:0,5,4,6:structure|north:3,5,4,6:structure|south:3,5,4,6:structure|up:3,5,4,9:structure|west:0,5,4,6:structure",
            "4,10,0,5,11,4|down:5,0,6,4:structure|east:6,3,10,4:structure|north:5,0,6,1:structure|south:5,0,6,1:structure|up:5,0,6,4:structure|west:6,3,10,4:structure",
            "11,5,0,12,6,4|down:12,5,13,9:structure|east:12,5,16,6:structure|north:12,5,13,6:structure|south:12,5,13,6:structure|up:12,5,13,9:structure|west:12,5,16,6:structure",
            "11,10,0,12,11,4|down:5,12,6,16:structure|east:5,12,9,13:structure|north:5,12,6,13:structure|south:5,12,6,13:structure|up:5,12,6,16:structure|west:5,12,9,13:structure",
            "10,4,0,12,5,4|down:10,12,12,16:structure|east:8,11,12,12:structure|north:4,15,6,16:structure|south:10,15,12,16:structure|up:10,12,12,16:structure|west:6,12,10,13:structure",
            "10,11,0,12,12,4|down:10,0,12,4:structure|east:8,4,12,5:structure|north:4,0,6,1:structure|south:10,0,12,1:structure|up:10,0,12,4:structure|west:6,3,10,4:structure",
            "4,4,0,6,5,4|down:4,0,6,4:structure|east:6,3,10,4:structure|north:10,0,12,1:structure|south:4,0,6,1:structure|up:4,0,6,4:structure|west:4,4,8,5:structure",
            "4,11,0,6,12,4|down:4,12,6,16:structure|east:6,12,10,13:structure|north:10,15,12,16:structure|south:4,15,6,16:structure|up:4,12,6,16:structure|west:4,11,8,12:structure",
            "5,5,0,11,11,4|down:5,12,11,16:core|east:12,5,16,11:core|north:5,5,11,11:core|south:5,5,11,11:core|up:5,0,11,4:core|west:0,5,4,11:core"
    };
    static final String[] CABLE_ITEM = {
            "3,3,13,13,13,16|down:3,13,13,16:connector|east:13,3,16,13:connector|north:3,3,13,13:connector|south:3,3,13,13:connector|up:3,0,13,3:connector|west:0,3,3,13:connector",
            "3,3,0,13,13,3|down:3,13,13,16:connector|east:13,3,16,13:connector|north:3,3,13,13:connector|south:3,3,13,13:connector|up:3,0,13,3:connector|west:0,3,3,13:connector",
            "5,5,2,11,11,6|down:5,0,11,4:core|east:0,5,4,11:core|north:5,5,11,11:core|south:5,5,11,11:core|up:5,0,11,4:core|west:0,5,4,11:core",
            "5,5,6,11,11,10|down:5,0,11,4:core|east:0,5,4,11:core|north:5,5,11,11:core|south:5,5,11,11:core|up:5,0,11,4:core|west:0,5,4,11:core",
            "5,5,10,11,11,14|down:5,0,11,4:core|east:0,5,4,11:core|north:5,5,11,11:core|south:5,5,11,11:core|up:5,0,11,4:core|west:0,5,4,11:core",
            "4,11,2,6,12,6|down:4,12,6,16:structure|east:6,12,10,13:structure|north:10,15,12,16:structure|south:4,15,6,16:structure|up:4,12,6,16:structure|west:4,11,8,12:structure",
            "4,4,2,6,5,6|down:4,0,6,4:structure|east:6,3,10,4:structure|north:10,0,12,1:structure|south:4,0,6,1:structure|up:4,0,6,4:structure|west:4,4,8,5:structure",
            "10,11,2,12,12,6|down:10,0,12,4:structure|east:8,4,12,5:structure|north:4,0,6,1:structure|south:10,0,12,1:structure|up:10,0,12,4:structure|west:6,3,10,4:structure",
            "10,4,2,12,5,6|down:10,12,12,16:structure|east:8,11,12,12:structure|north:4,15,6,16:structure|south:10,15,12,16:structure|up:10,12,12,16:structure|west:6,12,10,13:structure",
            "11,10,2,12,11,6|down:5,12,6,16:structure|east:5,12,9,13:structure|north:5,12,6,13:structure|south:5,12,6,13:structure|up:5,12,6,16:structure|west:5,12,9,13:structure",
            "11,5,2,12,6,6|down:12,5,13,9:structure|east:12,5,16,6:structure|north:12,5,13,6:structure|south:12,5,13,6:structure|up:12,5,13,9:structure|west:12,5,16,6:structure",
            "4,10,2,5,11,6|down:5,0,6,4:structure|east:6,3,10,4:structure|north:5,0,6,1:structure|south:5,0,6,1:structure|up:5,0,6,4:structure|west:6,3,10,4:structure",
            "4,5,10,5,6,14|down:3,5,4,9:structure|east:0,5,4,6:structure|north:3,5,4,6:structure|south:3,5,4,6:structure|up:3,5,4,9:structure|west:0,5,4,6:structure",
            "4,10,10,5,11,14|down:5,0,6,4:structure|east:6,3,10,4:structure|north:5,0,6,1:structure|south:5,0,6,1:structure|up:5,0,6,4:structure|west:6,3,10,4:structure",
            "11,5,10,12,6,14|down:12,5,13,9:structure|east:12,5,16,6:structure|north:12,5,13,6:structure|south:12,5,13,6:structure|up:12,5,13,9:structure|west:12,5,16,6:structure",
            "11,10,10,12,11,14|down:5,12,6,16:structure|east:5,12,9,13:structure|north:5,12,6,13:structure|south:5,12,6,13:structure|up:5,12,6,16:structure|west:5,12,9,13:structure",
            "10,4,10,12,5,14|down:10,12,12,16:structure|east:8,11,12,12:structure|north:4,15,6,16:structure|south:10,15,12,16:structure|up:10,12,12,16:structure|west:6,12,10,13:structure",
            "10,11,10,12,12,14|down:10,0,12,4:structure|east:8,4,12,5:structure|north:4,0,6,1:structure|south:10,0,12,1:structure|up:10,0,12,4:structure|west:6,3,10,4:structure",
            "4,4,10,6,5,14|down:4,0,6,4:structure|east:6,3,10,4:structure|north:10,0,12,1:structure|south:4,0,6,1:structure|up:4,0,6,4:structure|west:4,4,8,5:structure",
            "4,11,10,6,12,14|down:4,12,6,16:structure|east:6,12,10,13:structure|north:10,15,12,16:structure|south:4,15,6,16:structure|up:4,12,6,16:structure|west:4,11,8,12:structure",
            "4,5,6,5,6,10|down:3,5,4,9:structure|east:0,5,4,6:structure|north:3,5,4,6:structure|south:3,5,4,6:structure|up:3,5,4,9:structure|west:0,5,4,6:structure",
            "4,10,6,5,11,10|down:5,0,6,4:structure|east:6,3,10,4:structure|north:5,0,6,1:structure|south:5,0,6,1:structure|up:5,0,6,4:structure|west:6,3,10,4:structure",
            "11,5,6,12,6,10|down:12,5,13,9:structure|east:12,5,16,6:structure|north:12,5,13,6:structure|south:12,5,13,6:structure|up:12,5,13,9:structure|west:12,5,16,6:structure",
            "11,10,6,12,11,10|down:5,12,6,16:structure|east:5,12,9,13:structure|north:5,12,6,13:structure|south:5,12,6,13:structure|up:5,12,6,16:structure|west:5,12,9,13:structure",
            "10,4,6,12,5,10|down:10,12,12,16:structure|east:8,11,12,12:structure|north:4,15,6,16:structure|south:10,15,12,16:structure|up:10,12,12,16:structure|west:6,12,10,13:structure",
            "10,11,6,12,12,10|down:10,0,12,4:structure|east:8,4,12,5:structure|north:4,0,6,1:structure|south:10,0,12,1:structure|up:10,0,12,4:structure|west:6,3,10,4:structure",
            "4,4,6,6,5,10|down:4,0,6,4:structure|east:6,3,10,4:structure|north:10,0,12,1:structure|south:4,0,6,1:structure|up:4,0,6,4:structure|west:4,4,8,5:structure",
            "4,11,6,6,12,10|down:4,12,6,16:structure|east:6,12,10,13:structure|north:10,15,12,16:structure|south:4,15,6,16:structure|up:4,12,6,16:structure|west:4,11,8,12:structure",
            "4,5,2,5,6,6|down:3,5,4,9:structure|east:0,5,4,6:structure|north:3,5,4,6:structure|south:3,5,4,6:structure|up:3,5,4,9:structure|west:0,5,4,6:structure"
    };

    static void shape(ModelBuilder<?> builder, String[] boxes) {
        for (String box : boxes) {
            String[] parts = box.split("\\|");
            String[] bounds = parts[0].split(",");
            var element = builder.element()
                    .from(Float.parseFloat(bounds[0]), Float.parseFloat(bounds[1]), Float.parseFloat(bounds[2]))
                    .to(Float.parseFloat(bounds[3]), Float.parseFloat(bounds[4]), Float.parseFloat(bounds[5]));
            for (int index = 1; index < parts.length; index++) {
                String[] face = parts[index].split(":");
                String[] uv = face[1].split(",");
                element.face(Direction.byName(face[0]))
                        .uvs(Float.parseFloat(uv[0]), Float.parseFloat(uv[1]),
                                Float.parseFloat(uv[2]), Float.parseFloat(uv[3]))
                        .texture("#" + face[2])
                        .end();
            }
            element.end();
        }
    }
}
