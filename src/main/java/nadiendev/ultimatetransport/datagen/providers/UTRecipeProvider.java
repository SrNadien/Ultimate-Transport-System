package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import nadiendev.ultimatetransport.generator.GeneratorFuelRecipe;
import nadiendev.ultimatetransport.generator.GeneratorType;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import nadiendev.ultimatetransport.facade.FacadeCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.DeferredItem;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.compat.ModIds;
import nadiendev.ultimatetransport.compat.patchouli.GuideBook;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import nadiendev.ultimatetransport.tube.item.ItemTube;
import java.util.List;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

/**
 * Cables, upgrades and the two tools follow Pipez's recipes, so a pack that already taught players
 * those shapes does not have to teach them again.
 */
public class UTRecipeProvider extends RecipeProvider {

    public UTRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        cables(output);
        tubes(output);
        generatorFuels(output);
        SpecialRecipeBuilder.special(FacadeCraftingRecipe::new)
                .save(output, UltimateTransport.id("facade_from_block").toString());
        optional(output);
        upgrades(output);
        tools(output);
        cells(output);
        generators(output);
    }

    private void tubes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, TubeRegistration.TUBE_ITEM.get(), 16)
                .pattern("SGS")
                .pattern("GEG")
                .pattern("SGS")
                .define('S', Items.STONE)
                .define('G', Items.GLASS)
                .define('E', Items.ENDER_PEARL)
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, TubeRegistration.STATION_ITEM.get())
                .pattern("SLS")
                .pattern("S S")
                .pattern("SLS")
                .define('S', Items.STONE)
                .define('L', Ingredient.of(Items.SMOOTH_STONE_SLAB, Items.STONE_SLAB))
                .unlockedBy("has_stone", has(Items.STONE))
                .save(output);

        List<DeferredItem<ItemTube>> ring = List.of(
                TubeRegistration.TUBE_ITEM, TubeRegistration.TUBE_DOWN, TubeRegistration.TUBE_UP,
                TubeRegistration.TUBE_NORTH, TubeRegistration.TUBE_SOUTH, TubeRegistration.TUBE_EAST,
                TubeRegistration.TUBE_WEST);
        for (int index = 0; index < ring.size(); index++) {
            DeferredItem<ItemTube> from = ring.get(index);
            DeferredItem<ItemTube> to = ring.get((index + 1) % ring.size());
            for (int count : new int[]{1, 4, 9}) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, to.get(), count)
                        .requires(from.get(), count)
                        .unlockedBy("has_tube", has(TubeRegistration.TUBE_ITEM.get()))
                        .save(output, UltimateTransport.id(
                                from.getId().getPath() + "_to_" + to.getId().getPath() + "_" + count));
            }
        }
    }

    private void generatorFuels(RecipeOutput output) {
        fuel(output, GeneratorType.LAVA, Ingredient.of(Items.LAVA_BUCKET), 1_400);
        fuel(output, GeneratorType.NETHER_STAR, Ingredient.of(Items.NETHER_STAR), 20_000);
    }

    private void fuel(RecipeOutput output, GeneratorType generator, Ingredient fuel, int burnTime) {
        output.accept(UltimateTransport.id("fuel/" + generator.getSerializedName()),
                new GeneratorFuelRecipe(generator, fuel, burnTime, 0), null);
    }

    private void optional(RecipeOutput output) {
        RecipeOutput mekanism = output.withConditions(new ModLoadedCondition(ModIds.MEKANISM));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTBlocks.GAS_CABLE.get(), 16)
                .pattern("III")
                .pattern("CRC")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', foreign("mekanism:ultimate_control_circuit"))
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(mekanism, UltimateTransport.id("gas_cable"));

        RecipeOutput ars = output.withConditions(new ModLoadedCondition(ModIds.ARS_NOUVEAU));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTBlocks.SOURCE_CABLE.get(), 16)
                .pattern("III")
                .pattern("SRS")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('S', foreign("ars_nouveau:source_gem"))
                .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
                .save(ars, UltimateTransport.id("source_cable"));

        RecipeOutput avaritia = output.withConditions(
                new ModLoadedCondition(ModIds.ULTIMATE_AVARITIA), new ModLoadedCondition("avaritia"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTItems.UPGRADES.get(UpgradeTier.INFINITY).get(), 4)
                .pattern("III")
                .pattern("ICI")
                .pattern("III")
                .define('I', foreign("avaritia:infinity_catalyst"))
                .define('C', foreign("avaritia:infinity"))
                .unlockedBy("has_catalyst", has(foreign("avaritia:infinity_catalyst")))
                .save(avaritia, UltimateTransport.id("infinity_upgrade_avaritia"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTItems.CELLS.get(EnergyCellTier.CREATIVE).get())
                .pattern("ICI")
                .pattern("CTC")
                .pattern("ICI")
                .define('I', foreign("avaritia:infinity_catalyst"))
                .define('C', foreign("avaritia:infinity"))
                .define('T', UTItems.CELLS.get(EnergyCellTier.TIER_11).get())
                .unlockedBy("has_catalyst", has(foreign("avaritia:infinity_catalyst")))
                .save(avaritia, UltimateTransport.id("creative_energy_cell_avaritia"));

        RecipeOutput patchouli = output.withConditions(new ModLoadedCondition(ModIds.PATCHOULI));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, GuideBook.recipeResult())
                .requires(Items.BOOK)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .requires(Tags.Items.INGOTS_IRON)
                .unlockedBy("has_book", has(Items.BOOK))
                .save(patchouli, UltimateTransport.id("guide_book"));
    }

    private static Item foreign(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        if (item == Items.AIR) {
            throw new IllegalStateException("Data generation needs " + id + " on the classpath");
        }
        return item;
    }

    // ------------------------------------------------------------------ cables

    private void cables(RecipeOutput output) {
        cable(output, UTBlocks.ENERGY_CABLE.get(), Tags.Items.STORAGE_BLOCKS_REDSTONE);
        cable(output, UTBlocks.FLUID_CABLE.get(), Items.BUCKET);
        cable(output, UTBlocks.ITEM_CABLE.get(), Items.DROPPER);

        // The gas cable recipe is hand written in data/ultimatetransport/recipe/gas_cable.json: its
        // core is a Mekanism ultimate control circuit, and naming a foreign item here would tie data
        // generation to whether Mekanism happened to be on the classpath.

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTBlocks.UNIVERSAL_CABLE.get(), 6)
                .pattern("IEF")
                .pattern("MRM")
                .pattern("IEF")
                .define('I', UTBlocks.ITEM_CABLE.get())
                .define('E', UTBlocks.ENERGY_CABLE.get())
                .define('F', UTBlocks.FLUID_CABLE.get())
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('M', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_cable", has(UTBlocks.ENERGY_CABLE.get()))
                .save(output);
    }

    /** Iron frame, redstone core, two of whatever the cable is meant to move. */
    private void cable(RecipeOutput output, ItemLike result, TagKey<Item> core) {
        cableShape(result, 16).define('C', core).save(output);
    }

    private void cable(RecipeOutput output, ItemLike result, Item core) {
        cableShape(result, 16).define('C', core).save(output);
    }

    private ShapedRecipeBuilder cableShape(ItemLike result, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result, count)
                .pattern("III")
                .pattern("CRC")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON));
    }

    // ------------------------------------------------------------------ upgrades

    private void upgrades(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTItems.UPGRADES.get(UpgradeTier.BASIC).get())
                .pattern("INI")
                .pattern("NRN")
                .pattern("INI")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        upgrade(output, UpgradeTier.IMPROVED, UpgradeTier.BASIC, Tags.Items.INGOTS_GOLD, Tags.Items.DUSTS_REDSTONE);
        upgrade(output, UpgradeTier.ADVANCED, UpgradeTier.IMPROVED, Tags.Items.GEMS_DIAMOND, Tags.Items.STORAGE_BLOCKS_REDSTONE);
        upgrade(output, UpgradeTier.ULTIMATE, UpgradeTier.ADVANCED, Tags.Items.INGOTS_NETHERITE, Tags.Items.STORAGE_BLOCKS_REDSTONE);
    }

    private void upgrade(RecipeOutput output, UpgradeTier tier, UpgradeTier below,
                         TagKey<Item> corner, TagKey<Item> edge) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTItems.UPGRADES.get(tier).get())
                .pattern("CEC")
                .pattern("EUE")
                .pattern("CEC")
                .define('C', corner)
                .define('E', edge)
                .define('U', UTItems.UPGRADES.get(below).get())
                .unlockedBy("has_below", has(UTItems.UPGRADES.get(below).get()))
                .save(output);
    }

    // ------------------------------------------------------------------ tools

    private void tools(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, UTItems.CONFIGURATOR.get())
                .pattern(" F ")
                .pattern(" SF")
                .pattern("S  ")
                .define('F', Items.FLINT)
                .define('S', Tags.Items.RODS)
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, UTItems.DESTINATION_TOOL.get())
                .pattern("III")
                .pattern("RGR")
                .pattern("IBI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('G', Tags.Items.GLASS_PANES)
                .define('B', ItemTags.BUTTONS)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, UTItems.FACADE.get(), 4)
                .pattern("NNN")
                .pattern("N N")
                .pattern("NNN")
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);
    }

    // ------------------------------------------------------------------ cells

    private void cells(RecipeOutput output) {
        Item[] materials = {
                Items.COPPER_INGOT, Items.IRON_INGOT, Items.GOLD_INGOT, Items.REDSTONE_BLOCK, Items.DIAMOND,
                Items.EMERALD, Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT, Items.ECHO_SHARD, Items.NETHER_STAR
        };
        for (EnergyCellTier tier : EnergyCellTier.VALUES) {
            if (tier.creative() || tier.level() > materials.length) {
                continue;
            }
            ItemLike centre = tier.level() == 1
                    ? Items.REDSTONE_BLOCK
                    : UTItems.CELLS.get(EnergyCellTier.byLevel(tier.level() - 1)).get();
            cell(output, tier, materials[tier.level() - 1], Items.REDSTONE, centre);
        }

        // The bottomless cell: the top craftable one wrapped in the two rarest things in the game.
        cell(output, EnergyCellTier.TIER_11, Items.NETHER_STAR, Items.ECHO_SHARD,
                UTItems.CELLS.get(EnergyCellTier.TIER_10).get());
    }

    private void generators(RecipeOutput output) {
        generator(output, GeneratorType.FUEL, Items.FURNACE, Items.COPPER_INGOT);
        generator(output, GeneratorType.LAVA, Items.BUCKET, Items.NETHERITE_SCRAP);
        generator(output, GeneratorType.SOLAR, Items.DAYLIGHT_DETECTOR, Items.GOLD_INGOT);
        generator(output, GeneratorType.NETHER_STAR, Items.BEACON, Items.NETHERITE_INGOT);
    }

    private void generator(RecipeOutput output, GeneratorType type, Item centre, Item frame) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTBlocks.generators().get(type.ordinal()).get())
                .pattern("FFF")
                .pattern("FCF")
                .pattern("FRF")
                .define('F', frame)
                .define('C', centre)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_centre", has(centre))
                .save(output);
    }

    private void cell(RecipeOutput output, EnergyCellTier tier, Item material, Item edge, ItemLike centre) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, UTItems.CELLS.get(tier).get())
                .pattern("RMR")
                .pattern("MCM")
                .pattern("RMR")
                .define('R', edge)
                .define('M', material)
                .define('C', centre)
                .unlockedBy("has_material", has(material))
                .save(output);
    }
}
