package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import nadiendev.ultimatetransport.generator.GeneratorType;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class UTItemModelProvider extends ItemModelProvider {

    public UTItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, UltimateTransport.MODID, helper);
    }

    @Override
    protected void registerModels() {
        parents();

        for (UpgradeTier tier : UpgradeTier.VALUES) {
            if (tier != UpgradeTier.NONE) {
                basicItem(modLoc(tier.itemName()));
            }
        }
        basicItem(modLoc("configurator"));
        basicItem(modLoc("facade"));
        basicItem(modLoc("destination_tool"));
        for (EnergyCellTier tier : EnergyCellTier.VALUES) {
            withExistingParent(tier.blockName(), modLoc("block/" + tier.blockName() + "_item"));
        }
        for (GeneratorType type : GeneratorType.VALUES) {
            withExistingParent(type.blockName(), modLoc("block/" + type.blockName()));
        }
        withExistingParent("guide_book", "item/generated").texture("layer0", modLoc("item/book"));

        UTBlockStateProvider.shape(withExistingParent("item/cable_shape", "block/block")
                .texture("particle", "#core"), UTBlockStateProvider.CABLE_ITEM);

        for (TransferType type : TransferType.values()) {
            String name = type.cableName();
            withExistingParent(name, modLoc("item/cable_shape"))
                    .texture("core", modLoc("block/" + name + "_core"))
                    .texture("connector", modLoc("block/" + name + "_connector"))
                    .texture("structure", modLoc("block/cable_structure"));
        }

        withExistingParent("tube", "block/cube_all").texture("all", modLoc("block/tube_nodir"));
        withExistingParent("station", "block/cube_all").texture("all", modLoc("block/station_side2"));
        String[] directions = {"down", "up", "north", "south", "east", "west"};
        for (int index = 0; index < directions.length; index++) {
            withExistingParent("tube_" + directions[index], "block/cube_all")
                    .texture("all", modLoc("block/tube" + index + "/0"));
        }
    }

    private void parents() {
    }

    private static void segment(ItemModelBuilder builder, int lo, int hi, int span, String core, String connector) {
        int collar = 16 - hi;
        builder.element()
                .from(lo, lo, lo).to(hi, hi, hi)
                .allFaces((face, uv) -> uv.uvs(0, 0, 16, 16).texture(core))
                .end();
        capped(builder, lo, hi, span, connector, 0, collar);
        capped(builder, lo, hi, span, connector, 16 - collar, 16);
    }

    private static void capped(ItemModelBuilder builder, int lo, int hi, int span, String texture, int near, int far) {
        Direction cap = near == 0 ? Direction.NORTH : Direction.SOUTH;
        builder.element()
                .from(lo, lo, near).to(hi, hi, far)
                .face(cap).uvs(0, 0, 16, 16).texture(texture).end()
                .face(Direction.EAST).uvs(0, 0, span, 16).texture(texture).end()
                .face(Direction.WEST).uvs(0, 0, span, 16).texture(texture).end()
                .face(Direction.UP).uvs(0, 0, 16, span).texture(texture).end()
                .face(Direction.DOWN).uvs(0, 0, 16, span).texture(texture).end()
                .end();
    }

}
